package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.fbc.And;
import org.sbml.jsbml.ext.fbc.Association;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.fbc.FBCModelPlugin;
import org.sbml.jsbml.ext.fbc.FBCReactionPlugin;
import org.sbml.jsbml.ext.fbc.FBCSpeciesPlugin;
import org.sbml.jsbml.ext.fbc.FluxObjective;
import org.sbml.jsbml.ext.fbc.GeneProduct;
import org.sbml.jsbml.ext.fbc.GeneProductAssociation;
import org.sbml.jsbml.ext.fbc.GeneProductRef;
import org.sbml.jsbml.ext.fbc.Objective;
import org.sbml.jsbml.ext.fbc.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.JSBMLIOPlugin;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.AbstractSyntaxTree;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.AbstractSyntaxTreeNode;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.DataTypeEnum;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.IValue;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.Variable;

public class JSBMLFBCInformation implements JSBMLIOPlugin<Object>{


	private static final Logger logger = LoggerFactory.getLogger(JSBMLFBCInformation.class);
	@Override
	public String getName() {
		return FBCConstants.shortLabel;
	}

	@Override
	public Object read(Model sbmlModel, Container container, Collection<String> warnings) {

		FBCModelPlugin modelPlugin = (FBCModelPlugin) sbmlModel.getPlugin(FBCConstants.shortLabel);
		if(modelPlugin!=null){
			readGenes(modelPlugin.getListOfGeneProducts(), container.getGenes());

			Objective obj = modelPlugin.getActiveObjectiveInstance();
			if(obj != null && obj.getListOfFluxObjectives().size() ==1)
				container.setBiomassId(obj.getListOfFluxObjectives().get(0).getReaction());

		}
		readMetabolitesInformation(sbmlModel, container);
		readReactionsInformation(sbmlModel, container);



		return null;
	}


	@Override
	public void write(Model sbmlModel, Container container, Object pluginInfo,  SBMLDocument doc) {
		FBCModelPlugin modelPlugin = (FBCModelPlugin) sbmlModel.getPlugin(FBCConstants.shortLabel);
		modelPlugin.setStrict(true);
		createObjective(modelPlugin,sbmlModel, container);
		createGenes(modelPlugin, container);
		createMetabolitesFBCInfo(modelPlugin, sbmlModel, container);
		createReactionsFBCInformation(modelPlugin, sbmlModel, container);
	}



	private void createReactionsFBCInformation(FBCModelPlugin modelPlugin, Model sbmlModel, Container container) {

		//		HashMap<String, GeneProductRef> refs = new HashMap<>();
		for(Entry<String, ReactionCI> entry : container.getReactions().entrySet()){
			Reaction s = sbmlModel.getReaction(entry.getKey());
			FBCReactionPlugin rp = (FBCReactionPlugin)s.getPlugin(FBCConstants.shortLabel);
			createGPR(rp, entry.getValue(), sbmlModel, modelPlugin/*,refs*/);
			associateBounds(entry.getValue(), s, modelPlugin, sbmlModel,rp, container);

		}

	}


	private void associateBounds(ReactionCI ogreaction, Reaction s, FBCModelPlugin modelPlugin, Model sbmlModel,
			FBCReactionPlugin rp, Container container) {
		Double up = Double.POSITIVE_INFINITY;
		Double lb = Double.NEGATIVE_INFINITY;

		ReactionConstraintCI reactionConstraintCI = container.getDefaultEC().get(ogreaction.getId()); 
		
		if(ogreaction.isReversible())
			lb = 0.0;
		if(reactionConstraintCI != null){
			up = reactionConstraintCI.getUpperLimit();
			lb = reactionConstraintCI.getLowerLimit();
		}


		Parameter upp = createIfNotExists(sbmlModel, up);
		rp.setUpperFluxBound(upp);
		upp.setConstant(true);
		
		
		Parameter lbp = createIfNotExists(sbmlModel, lb);
		rp.setLowerFluxBound(lbp);
		lbp.setConstant(true);

	}




	private Parameter createIfNotExists(Model sbmlModel, Double up) {

		String id = createBoundParamUId(up);

		Parameter p = sbmlModel.getParameter(id);
		if(p == null){
			p = sbmlModel.createParameter(id);
			p.setUnits(JBMLBaseInformation.FLUX_UNIT_ID);
			p.setValue(up);
		}

		return p;
	}

	private String createBoundParamUId(Double up) {
		String id = "BOUND_";
		id += (up < 0)?"NEG_" + (Math.abs(up)+"").replace(".", "_"): (Math.abs(up)+"").replace(".", "_"); 
		return id;
	}

	private void createGPR(FBCReactionPlugin rp, ReactionCI value, Model sbmlModel, FBCModelPlugin modelPlugin) {
		AbstractSyntaxTree<DataTypeEnum, IValue> rule = value.getGeneRule();


		if(rule !=null){
			AbstractSyntaxTreeNode<DataTypeEnum, IValue> rootNode = rule.getRootNode();
			if(rootNode != null){

				Association ass = createSBMLGPR(rootNode, sbmlModel);
				GeneProductAssociation gpa = rp.createGeneProductAssociation();
				gpa.setAssociation(ass);
			}
		}

	}

	private Association createSBMLGPR(AbstractSyntaxTreeNode<DataTypeEnum, IValue> node, Model sbmlModel) {
		Association ret = null;

		if(node instanceof pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.And){

			pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.And andNode = 
					(pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.And) node;

			List<Association> ass = convertChilds(andNode, sbmlModel);
			And and = new And(sbmlModel.getLevel(), sbmlModel.getVersion());
			and.addAllAssociations(ass);
			ret = and;

		}else if(node instanceof pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.Or){
			pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.Or orNode = 
					(pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.Or) node;

			List<Association> ass = convertChilds(orNode, sbmlModel);
			Or or = new Or(sbmlModel.getLevel(), sbmlModel.getVersion());
			or.addAllAssociations(ass);
			ret = or;

		}else if (node instanceof Variable){

			Variable v = (Variable) node;
			GeneProductRef modelRef = new GeneProductRef(sbmlModel.getLevel(), sbmlModel.getVersion());
			modelRef.setGeneProduct(v+"");
			ret =modelRef;
		}else{
			logger.error("AbstractSyntaxTreeNode node problem {}", node);
		}

		return ret;
	}

	List<Association> convertChilds(AbstractSyntaxTreeNode node,Model sbmlModel){
		List<Association> associations = new ArrayList<>();
		for(int i =0; i < node.getNumberOfChildren(); i++)
			associations.add(createSBMLGPR(node.getChildAt(i), sbmlModel));
		return associations;
	}

	private void createMetabolitesFBCInfo(FBCModelPlugin modelPlugin, Model sbmlModel, Container container) {

		for(Entry<String, MetaboliteCI> entry : container.getMetabolites().entrySet()){
			Species s = sbmlModel.getSpecies(entry.getKey());
			FBCSpeciesPlugin sp = (FBCSpeciesPlugin)s.getPlugin(FBCConstants.shortLabel);
			String formula = entry.getValue().getFormula();
			Integer charge = entry.getValue().getCharge();
			if(formula!=null) sp.setChemicalFormula(formula);
			if(charge!=null) sp.setCharge(charge);
		}

	}

	private void createGenes(FBCModelPlugin modelPlugin, Container container) {

		for(GeneCI g : container.getGenes().values()){
			GeneProduct gp = modelPlugin.createGeneProduct(g.getGeneId());
			gp.setLabel(g.getGeneName());
		}

	}

	private void createObjective(FBCModelPlugin modelPlugin, Model sbmlModel, Container container) {

		
		
		Objective obj = modelPlugin.createObjective("biomass", Objective.Type.MAXIMIZE);
		FluxObjective fluxObjective = obj.createFluxObjective();
		fluxObjective.setReaction(sbmlModel.getReaction(container.getBiomassId()));
		fluxObjective.setCoefficient(1.0);
		
		
		modelPlugin.setActiveObjective(obj);
		
//		FluxObjective fluxObjective = obj.createFluxObjective("bio", "biomass", 1, sbmlModel.getReaction(container.getBiomassId()));
//		//		obj.addFluxObjective(fluxObjective);

	}

	private void readReactionsInformation(Model sbmlModel, Container container) {
		for(Reaction reaction : sbmlModel.getListOfReactions()){
			String id = reaction.getId();
			FBCReactionPlugin reactionPlugin = (FBCReactionPlugin) reaction.getPlugin(FBCConstants.shortLabel);
			if(reactionPlugin!=null){
				try{
					Parameter lb = reactionPlugin.getLowerFluxBoundInstance();
					Double lowerBound = lb.getValue();
					Parameter ub = reactionPlugin.getUpperFluxBoundInstance();
					Double upperBound = ub.getValue();
					container.getDefaultEC().put(id, new ReactionConstraintCI(lowerBound, upperBound));
				}catch (Exception e) {

				}
			}

			GeneProductAssociation gpr = reactionPlugin.getGeneProductAssociation();
			addGPR(gpr, container.getReaction(id));
		}
	}


	void addGPR(GeneProductAssociation gpr, ReactionCI reactionCI){

		if(gpr != null){
			AbstractSyntaxTree<DataTypeEnum, IValue> ret = new AbstractSyntaxTree<>();
			ret.setRootNode(convert(gpr.getAssociation()));
			reactionCI.setGeneRule(ret);
		}

	}

	private AbstractSyntaxTreeNode<DataTypeEnum, IValue> convert(Association association) {

		AbstractSyntaxTreeNode<DataTypeEnum, IValue> ret = null;

		if(association instanceof And){
			List<Association> associations = ((And)association).getListOfAssociations();
			Association ass = associations.get(0);
			AbstractSyntaxTreeNode<DataTypeEnum, IValue> left = convert(ass);

			for(int i =1 ; i < associations.size(); i++){
				AbstractSyntaxTreeNode<DataTypeEnum, IValue> right = convert(associations.get(i));
				pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.And node = new pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.And(left, right);
				left = node;
			}
			ret = left;
		}else if(association instanceof Or){
			List<Association> associations = ((Or)association).getListOfAssociations();
			Association ass = associations.get(0);
			AbstractSyntaxTreeNode<DataTypeEnum, IValue> left = convert(ass);
			for(int i =1 ; i < associations.size(); i++){
				AbstractSyntaxTreeNode<DataTypeEnum, IValue> right = convert(associations.get(i));
				pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.Or node = new pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.node.Or(left, right);
				left = node;
			}
			ret = left;
		}else {
			String geneId = ((GeneProductRef) association).getGeneProduct();
			Variable v = new Variable(geneId);
			ret = v ;
		}


		return ret;
	}


	private void readMetabolitesInformation(Model sbmlModel, Container container) {

		for(Species s : sbmlModel.getListOfSpecies()){
			String id = s.getId();
			FBCSpeciesPlugin fbc = (FBCSpeciesPlugin) s.getPlugin(FBCConstants.shortLabel);

			if(fbc!=null){
				try {
					container.getMetabolite(id).setCharge(fbc.getCharge());
				} catch (Exception e) {
				}
				try {
					container.getMetabolite(id).setFormula(fbc.getChemicalFormula());
				} catch (Exception e) {
				}

			}
		}

	}

	private void readGenes(ListOf<GeneProduct> listOfGeneProducts, Map<String, GeneCI> genes) {

		for(GeneProduct gp : listOfGeneProducts){
			String id = gp.getId();
			String name = gp.getLabel();
			genes.put(id, new GeneCI(id, name));
		}


	}

	public static void main(String[] args) {

		Double d =Double.POSITIVE_INFINITY;
		Double dd =Double.NEGATIVE_INFINITY;


		System.out.println(d);
		System.out.println(dd);


		System.out.println(Math.abs(dd));
	}

}
