package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.fbc.And;
import org.sbml.jsbml.ext.fbc.Association;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.fbc.FBCModelPlugin;
import org.sbml.jsbml.ext.fbc.FBCReactionPlugin;
import org.sbml.jsbml.ext.fbc.FBCSpeciesPlugin;
import org.sbml.jsbml.ext.fbc.GeneProduct;
import org.sbml.jsbml.ext.fbc.GeneProductAssociation;
import org.sbml.jsbml.ext.fbc.GeneProductRef;
import org.sbml.jsbml.ext.fbc.Or;
import org.sbml.jsbml.xml.parsers.MathMLStaxParser;
import org.sbml.jsbml.xml.parsers.SBMLCoreParser;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.InvalidBooleanRuleException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionTypeEnum;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.interfaces.IContainerBuilder;

public class JSBMLLevel3Reader implements IContainerBuilder{

	   static {
//	    	Logger.getLogger(JSBML.class).setLevel(Level.OFF);
	    	Logger.getLogger(SBMLCoreParser.class).setLevel(Level.ERROR);
			Logger.getLogger(MathMLStaxParser.class).setLevel(Level.OFF);
			Logger.getLogger(SBMLCoreParser.class).setLevel(Level.ERROR);
	    }
	private static final long serialVersionUID = 1L;
	private Model jsbmlmodel;
	private String biomassId;
	private String organismName;

	protected HashMap<String, ReactionConstraintCI> defaultEC = null;
	protected HashMap<String, CompartmentCI> compartmentList = null;
	protected HashMap<String, MetaboliteCI> metaboliteList = null;
	protected HashMap<String, ReactionCI> reactionList = null;
	protected HashMap<String, GeneCI> genes = null;
	protected Map<String, Map<String, String>> metabolitesExtraInfo = null;
	protected Map<String, Map<String, String>> reactionsExtraInfo = null;
	protected HashMap<String, String> mapMetaboliteIdCompartment = null;
	protected HashMap<String, Double> boundsParameters=null; 
	
	protected SBMLDocument document;

	protected ArrayList<String> warnings;
	protected boolean checkConsistency;
	
	public JSBMLLevel3Reader(String filePath, String organismName) throws Exception {
		this(new FileInputStream(filePath), organismName, true);
	}

	public JSBMLLevel3Reader(String filePath, String organismName, boolean checkConsistency) throws Exception {
		this(new FileInputStream(filePath), organismName, checkConsistency);
	}

	public JSBMLLevel3Reader(InputStream data, String organismName, boolean checkConsistency) throws Exception {
		this.checkConsistency = checkConsistency;
		SBMLReader reader = new SBMLReader();
		document = reader.readSBMLFromStream(data);

		jsbmlmodel = document.getModel();
		
		warnings = new ArrayList<String>();
		populateInformation();
	}

	private void populateInformation() throws InvalidBooleanRuleException {
		compartmentList = new HashMap<String, CompartmentCI>();
		genes = new HashMap<String, GeneCI>();
		metaboliteList = new HashMap<String, MetaboliteCI>();
		defaultEC = new HashMap<String, ReactionConstraintCI>();
		reactionList = new HashMap<String, ReactionCI>();
		mapMetaboliteIdCompartment = new HashMap<String, String>();
		metabolitesExtraInfo = new HashMap<String, Map<String, String>>(); 
		reactionsExtraInfo = new HashMap<String, Map<String, String>>();
		readCompartments();
		readGenes();
		readMetabolites();
		readReactions();
	}
	
	
	public void readCompartments() {
		ListOf<Compartment> sbmllistofcomps = jsbmlmodel.getListOfCompartments();
		for (int i = 0; i < sbmllistofcomps.size(); i++) {
			Compartment comp = sbmllistofcomps.get(i);
			CompartmentCI ogcomp = new CompartmentCI(comp.getId(), comp.getName(), comp.getOutside());
			compartmentList.put(comp.getId(), ogcomp);
		}
	}
	

	public void readGenes(){
		FBCModelPlugin fbcModel = (FBCModelPlugin) jsbmlmodel.getExtension(FBCConstants.namespaceURI);
		ListOf<GeneProduct> genesModel = fbcModel.getListOfGeneProducts();
		for(int i=0; i< genesModel.size(); i++){
			GeneProduct gene = genesModel.get(i);
			//SGC 
			String id = gene.getId();
			String label = gene.getLabel().replace("__SBML_DOT__", ".");
			genes.put(id, new GeneCI(id, label));	
		}
	}

	public void readMetabolites() {
		ListOf<Species> sbmlspecies = jsbmlmodel.getListOfSpecies();
		for (int i = 0; i < sbmlspecies.size(); i++) {
			Species species = sbmlspecies.get(i);
			String idInModel = species.getId();
			String nameInModel = species.getName();
			String compartmentId = species.getCompartment();
			compartmentList.get(compartmentId).addMetaboliteInCompartment(idInModel);

			mapMetaboliteIdCompartment.put(idInModel, compartmentId);
			MetaboliteCI ogspecies = new MetaboliteCI(idInModel, nameInModel);
			//set Formula
			FBCSpeciesPlugin fbcMeta = (FBCSpeciesPlugin) species.getExtension(FBCConstants.namespaceURI);			
			if(fbcMeta!=null)
			ogspecies.setFormula(fbcMeta.getChemicalFormula());

			metaboliteList.put(idInModel, ogspecies);
		}
	}


	public void readReactions() throws InvalidBooleanRuleException {
		Set<String> speciesInReactions = new TreeSet<String>();
		long maxMetabInReaction = 0;

		ListOf<Reaction> sbmlreactions = jsbmlmodel.getListOfReactions();
		for (int i = 0; i < sbmlreactions.size(); i++) {

			Reaction sbmlreaction = sbmlreactions.get(i);
			String reactionId = sbmlreaction.getId();

			ListOf<SpeciesReference> products = sbmlreaction.getListOfProducts();
			ListOf<SpeciesReference> reactants = sbmlreaction.getListOfReactants();

			/** add mappings for products */
			Map<String, StoichiometryValueCI> productsCI = addMapping(products, reactionId, speciesInReactions);

			/** add mappings for reactants */
			Map<String, StoichiometryValueCI> reactantsCI = addMapping(reactants, reactionId, speciesInReactions);

			boolean isReversible = sbmlreaction.getReversible();

			maxMetabInReaction = kinetic(sbmlreaction, isReversible, reactantsCI, productsCI, maxMetabInReaction);

			ReactionCI ogreaction = new ReactionCI(sbmlreaction.getId(), sbmlreaction.getName(), isReversible,
					reactantsCI, productsCI);

			if (products.size() == 0 || reactants.size() == 0) {
				ogreaction.setType(ReactionTypeEnum.Drain);
			} else {
				ogreaction.setType(ReactionTypeEnum.Internal);
			}
			//add reaction
			reactionList.put(reactionId, ogreaction);

			//parse the gene rule
			ogreaction.setGeneRule(getGeneRule(sbmlreaction));
			
		}
		reactionList.get(biomassId).setType(ReactionTypeEnum.Biomass);
		
		removeSpeciesNonAssociatedToReactions(speciesInReactions);
	}

	//
	// LIST OF MODIFIERS GENE RULE
	
	private String getGeneRule(Reaction sbmlReaction) {
		String rule = null;
		FBCReactionPlugin fbcReac = (FBCReactionPlugin) sbmlReaction.getExtension(FBCConstants.namespaceURI);	
	
		GeneProductAssociation gpr = fbcReac.getGeneProductAssociation();
		if(gpr!=null){
			rule= getRule(gpr.getAssociation());
//			System.out.println(rule);		
		}
		
		return rule;
	}
		
	private String getRule(Association association){
		String res="";
	if(association instanceof And){
		List<Association> associations = ((And)association).getListOfAssociations();
		res = "( ";
		for(int i =0; i< associations.size(); i++){
			res = res + getRule(associations.get(i)) + " AND ";
		}
		res = res.substring(0, res.length()-5);
		
		res = res+")";
	}else if(association instanceof Or){
		List<Association> associations = ((Or)association).getListOfAssociations();
		res = "(";
		for(int i =0; i< associations.size(); i++){
			res = res + getRule(associations.get(i)) + " OR ";
		}
		res= res.substring(0, res.length()-4);
		
		res = res+")";
	}else{
		res = ((GeneProductRef) association).getGeneProduct();
	}
		return res;
	}
	/**
	 * This method adds mapping to reactants and products
	 * 
	 * @param list
	 *            List of reactants or products
	 * @param reactionId
	 *            The reaction ID
	 * @param speciesInReactions
	 *            A set with all the metabolites that participate in some
	 *            reaction
	 * @return The mapping of reactants or products
	 */
	public Map<String, StoichiometryValueCI> addMapping(ListOf<SpeciesReference> list, String reactionId,
			Set<String> speciesInReactions) {
		Map<String, StoichiometryValueCI> result = new HashMap<String, StoichiometryValueCI>();
		for (int l = 0; l < list.size(); l++) {
			SpeciesReference ref = (SpeciesReference) list.get(l);
			String idInModel = ref.getSpecies();
			result.put(idInModel, new StoichiometryValueCI(idInModel, ref.getStoichiometry(),
					mapMetaboliteIdCompartment.get(idInModel)));
//			System.out.println(idInModel);
			metaboliteList.get(idInModel).addReaction(reactionId);
			speciesInReactions.add(idInModel);
		}

		return result;
	}

	public long kinetic(Reaction sbmlreaction, boolean isReversible, Map<String, StoichiometryValueCI> reactantsCI,
			Map<String, StoichiometryValueCI> productsCI, long maxMetabInReaction) {
		
		FBCReactionPlugin fbcReac = (FBCReactionPlugin) sbmlreaction.getExtension(FBCConstants.namespaceURI);	
		
//		System.out.println(sbmlreaction.getId());
		double lower = fbcReac.getLowerFluxBoundInstance().getValue();
		double upper = fbcReac.getUpperFluxBoundInstance().getValue();
		
				long nMetabolitesInReaction = reactantsCI.size() + productsCI.size();
		if (nMetabolitesInReaction > maxMetabInReaction) {
			maxMetabInReaction = nMetabolitesInReaction;
			biomassId = sbmlreaction.getId();
		}

		defaultEC.put(sbmlreaction.getId(), new ReactionConstraintCI(lower, upper));

		return maxMetabInReaction;
	}

	/**
	 * This method removes the species that aren't associated with any reaction
	 * 
	 * @param speciesInReactions
	 *            A set with all the species that are associated to one ore more
	 *            reactions
	 */
	private void removeSpeciesNonAssociatedToReactions(Set<String> speciesInReactions) {

		List<String> toRemove = new ArrayList<String>();

		for (String metId : metaboliteList.keySet())
			if (!speciesInReactions.contains(metId))
				toRemove.add(metId);

		for (String metId : toRemove) {
			metaboliteList.remove(metId);
		}
	}

	// SGC outside dont exist in level 3
	
	public String getExternalCompartment() {
		String toReturn = "e";
		return toReturn;
	}

	/**
	 * This method parses the notes
	 * 
	 * @param notes
	 *            A String with the notes
	 * @param ogreaction
	 *            A ReactionCI object
	 * @throws InvalidBooleanRuleException
	 * @throws ParseException
	 * @throws utilities.math.language.mathboolean.parser.ParseException
	 */

	/**
	 * @return The biomass ID
	 */
	public String getBiomassId() {
		return biomassId;
	}

	/**
	 * @return A map with the environmental conditions
	 */
	@Override
	public HashMap<String, ReactionConstraintCI> getDefaultEC() {
		return defaultEC;
	}

	/**
	 * @return A map with the compartments
	 */
	@Override
	public HashMap<String, CompartmentCI> getCompartments() {
		return compartmentList;
	}

	/**
	 * @return A map with the metabolites
	 */
	@Override
	public HashMap<String, MetaboliteCI> getMetabolites() {
		return metaboliteList;
	}

	/**
	 * @return A map with the reactions
	 */
	@Override
	public HashMap<String, ReactionCI> getReactions() {
		return reactionList;
	}

	/**
	 * @return A map with the genes
	 */
	@Override
	public Map<String, GeneCI> getGenes() {
		return genes;
	}

	/**
	 * @return
	 */
	public HashMap<String, String> getMetaboliteIdToSpecieTermId() {
		return null;
	}

	/**
	 * @return The external compartment ID
	 */
	@Override
	public String getExternalCompartmentId() {
		String ret = null;
		String altrn = null;
		for (CompartmentCI comp : compartmentList.values()) {
			if (comp.getOutside() == null || comp.getOutside().equals("")) {
				ret = comp.getId();
				break;
			}

			if (comp.getId().startsWith("e") || comp.getId().startsWith("E"))
				altrn = comp.getId();
		}

		if (ret == null)
			ret = altrn;
		return ret;
	}

	/**
	 * @return A map with the metabolites extra info
	 */
	@Override
	public Map<String, Map<String, String>> getMetabolitesExtraInfo() {
		return metabolitesExtraInfo;
	}

	/**
	 * @return A map with the reactions extra info
	 */
	@Override
	public Map<String, Map<String, String>> getReactionsExtraInfo() {
		return reactionsExtraInfo;
	}

	public boolean hasWarnings() {
		return !warnings.isEmpty();
	}

	public List<String> getWarnings() {
		return warnings;
	}

	@Override
	public String getModelName() {
		return jsbmlmodel.getId();
	}

	@Override
	public String getNotes() {
		try {
			return jsbmlmodel.getNotesString();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * @return The organism name
	 */
	public String getOrganismName() {
		return organismName;
	}

	/**
	 * @param organismName
	 *            The organism name to be set
	 */
	public void setOrganismName(String organismName) {
		this.organismName = organismName;
	}

	/**
	 * @return The model version
	 */
	public Integer getVersion() {
		return Integer.valueOf((int) jsbmlmodel.getVersion());
	}
}
