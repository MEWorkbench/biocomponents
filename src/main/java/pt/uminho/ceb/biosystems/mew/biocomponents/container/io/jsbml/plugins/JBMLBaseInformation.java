package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit.Kind;
import org.sbml.jsbml.UnitDefinition;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.InvalidBooleanRuleException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionTypeEnum;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.JSBMLIOPlugin;



public class JBMLBaseInformation implements JSBMLIOPlugin<Object>{
	
	
	public static final String VOLUME_UNIT_ID = "volume";
	public static final String SUBSTANCE_UNIT_ID = "substance";
	public static final String TIME_UNIT_ID = "time";
	public static final String LENGTH_UNIT_ID = "lenght";
	public static final String AREA_UNIT_ID = "area";
	public static final String FLUX_UNIT_ID = "mmol_per_gDW_per_hr";
	public static final String LOCAL_LOWER_BOUND = "LOWER_BOUND";
	private static final String LOCAL_UPPER_BOUND = "UPPER_BOUND";
	
	private Double defaultLB = 0.0;
	private Double defaultUB = 100000.0;
	
	@Override
	public String getName() {
		return "base.information";
	}

	@Override
	public Object read(Model sbmlModel, Container container, Collection<String> warnings) {
		
		readCompartments(sbmlModel, container.getCompartments());
		Map<String, String> metComp = readMetabolites(sbmlModel, container.getMetabolites());
		readReactions(sbmlModel, container.getReactions(), container.getDefaultEC(), metComp);
		return null;
	}
	
	@Override
	public void write(Model sbmlModel, Container container, Object pluginInfo,  SBMLDocument doc) {
		
		sbmlModel.setId(container.getModelName());
		
		buildUnits(sbmlModel);
		writeCompartments(sbmlModel, container);
		Map<String, String> mapMetaboliteToComp = buildReactions(sbmlModel, container);
		buildMetabolites(sbmlModel, container, mapMetaboliteToComp);
	}
	
	private Map<String, String> buildReactions(Model sbmlModel, Container container) {
		
		Map<String, String> metabolitesInCompartment = new HashMap<>();
		for(String rId : container.getReactions().keySet()){
			ReactionCI ogreaction = container.getReactions().get(rId);
			Reaction sbmlReaction = sbmlModel.createReaction();
			sbmlReaction.setId(ogreaction.getId());
			sbmlReaction.setName(ogreaction.getName());
			sbmlReaction.setReversible(ogreaction.isReversible());
			sbmlReaction.setFast(false);
			
			
			sbmlReaction.setListOfReactants(createStoichiometry(sbmlModel, ogreaction.getReactants(), metabolitesInCompartment));
			sbmlReaction.setListOfProducts(createStoichiometry(sbmlModel, ogreaction.getProducts(), metabolitesInCompartment));
			
//			addReactionBoundsInKineticLaw(sbmlReaction,ogreaction, container.getDefaultEC().get(rId));
		}
		
		return metabolitesInCompartment;
	}

	private void addReactionBoundsInKineticLaw(Reaction sbmlReaction, ReactionCI ogreaction,
			ReactionConstraintCI reactionConstraintCI) {
		
		Double up = defaultUB;
		Double lb = defaultUB;
		
		if(ogreaction.isReversible())
			lb = 0.0;
		if(reactionConstraintCI != null){
			up = reactionConstraintCI.getLowerLimit();
			lb = reactionConstraintCI.getUpperLimit();
		}
		KineticLaw kl = sbmlReaction.createKineticLaw();
		LocalParameter p1 = kl.createLocalParameter(LOCAL_LOWER_BOUND);
		p1.setValue(lb);
		kl.createLocalParameter(LOCAL_UPPER_BOUND).setValue(up);
	}

	private ListOf<SpeciesReference> createStoichiometry(Model sbmlModel, Map<String, StoichiometryValueCI> reactants,
			Map<String, String> metabolitesInCompartment) {
		
		ListOf<SpeciesReference> ret = new ListOf<SpeciesReference>(sbmlModel.getLevel(), sbmlModel.getVersion());
		for(StoichiometryValueCI s : reactants.values()){
			String sId = s.getMetaboliteId();
			Species species = sbmlModel.getSpecies(sId);
			
			if(species == null){
				species = sbmlModel.createSpecies(s.getMetaboliteId());
			}
			
			SpeciesReference sr = new SpeciesReference(species);
			sr.setStoichiometry(s.getStoichiometryValue());
			metabolitesInCompartment.put(s.getMetaboliteId(), s.getCompartmentId());
			sr.setConstant(true);
			
			ret.add(sr);
		}
		
		return ret;
	}

	private void writeCompartments(Model sbmlModel, Container container) {
		for(CompartmentCI c : container.getCompartments().values()){
			Compartment comp = sbmlModel.createCompartment(c.getId());
			comp.setName(c.getName());
			comp.setConstant(true);
		}
		
	}

	private void buildMetabolites(Model sbmlModel, Container container, Map<String, String> mapMetaboliteToComp) {
		for(MetaboliteCI m : container.getMetabolites().values()){
			Species s = sbmlModel.getSpecies(m.getId());
			s.setCompartment(mapMetaboliteToComp.get(m.getId()));
			s.setName(m.getName());
			s.setHasOnlySubstanceUnits(false);
			s.setBoundaryCondition(false);
			s.setConstant(false);
		}
		
	}

	private void buildUnits(Model sbmlModel) {
		UnitDefinition ud = sbmlModel.createUnitDefinition(FLUX_UNIT_ID);
		org.sbml.jsbml.util.ModelBuilder.buildUnit(ud, 1, -3, Kind.MOLE, 1);
		org.sbml.jsbml.util.ModelBuilder.buildUnit(ud, 1, 0, Kind.GRAM,- 1);
		org.sbml.jsbml.util.ModelBuilder.buildUnit(ud, 3600, 0, Kind.SECOND, -1);
	}

	public void readCompartments(Model jsbmlmodel, Map<String, CompartmentCI> compartmentList) {		
		ListOf<Compartment> sbmllistofcomps = jsbmlmodel.getListOfCompartments();
		for (int i = 0; i < sbmllistofcomps.size(); i++) {
			Compartment comp = sbmllistofcomps.get(i);
			CompartmentCI ogcomp = new CompartmentCI(comp.getId(), comp.getName(), comp.getOutside());
			compartmentList.put(comp.getId(), ogcomp);
		}
	}
	
	
	public Map<String, String> readMetabolites(Model jsbmlmodel, Map<String, MetaboliteCI> metaboliteList) {
		
		HashMap<String, String> mapMetaboliteIdCompartment = new HashMap<>();
		ListOf<Species> sbmlspecies = jsbmlmodel.getListOfSpecies();
		for (int i = 0; i < sbmlspecies.size(); i++) {
			Species species = sbmlspecies.get(i);
			String idInModel = species.getId();
			String nameInModel = species.getName();
			MetaboliteCI ogspecies = new MetaboliteCI(idInModel, nameInModel);
			metaboliteList.put(idInModel, ogspecies);
			
			mapMetaboliteIdCompartment.put(idInModel, species.getCompartment());
		}
		
		return mapMetaboliteIdCompartment;
	}

	public void readReactions(Model jsbmlmodel, Map<String, ReactionCI>reactions,Map<String, ReactionConstraintCI> defaultEC, Map<String, String> speciesToCompartment) throws InvalidBooleanRuleException {
		Set<String> speciesInReactions = new TreeSet<String>();
		long maxMetabInReaction = 0;

		ListOf<Reaction> sbmlreactions = jsbmlmodel.getListOfReactions();
		String biomassId = null;
		
		for (int i = 0; i < sbmlreactions.size(); i++) {

			Reaction sbmlreaction = sbmlreactions.get(i);
			String reactionId = sbmlreaction.getId();

			ListOf<SpeciesReference> products = sbmlreaction.getListOfProducts();
			ListOf<SpeciesReference> reactants = sbmlreaction.getListOfReactants();

			/** add mappings for products */
			Map<String, StoichiometryValueCI> productsCI = addMapping(products, reactionId, speciesInReactions, speciesToCompartment);

			/** add mappings for reactants */
			Map<String, StoichiometryValueCI> reactantsCI = addMapping(reactants, reactionId, speciesInReactions, speciesToCompartment);

			boolean isReversible = sbmlreaction.getReversible();

			int pS = products.size();
			int rS = reactants.size();
			
			if(maxMetabInReaction < (rS + pS)){
				biomassId = reactionId;
				maxMetabInReaction = (rS + pS);
			}
			
			kinetic(sbmlreaction, isReversible,defaultEC);

			ReactionCI ogreaction = new ReactionCI(sbmlreaction.getId(), sbmlreaction.getName(), isReversible,
					reactantsCI, productsCI);

			if (pS == 0 || rS == 0) {
				ogreaction.setType(ReactionTypeEnum.Drain);
			} else {
				ogreaction.setType(ReactionTypeEnum.Internal);
			}
			//add reaction
			reactions.put(reactionId, ogreaction);

		}
//		reactions.get(biomassId).setType(ReactionTypeEnum.Biomass);
		
	}
	
	/**
	 * This method handles with the kinetic law of the reaction, if it exists
	 * @param sbmlreaction The reaction
	 * @param isReversible The reaction reversibility
	 * @param reactantsCI The reactants
	 * @param productsCI The products
	 */
	public void kinetic(Reaction sbmlreaction, boolean isReversible, Map<String, ReactionConstraintCI> defaultEC){
		KineticLaw kineticlaw = sbmlreaction.getKineticLaw();
		
		double lower = defaultLB;
		double upper = defaultUB;
		
		boolean haskinetic = false;
		if(kineticlaw!=null){
			ListOf<LocalParameter> params = kineticlaw.getListOfLocalParameters();// getListOfParameters();
								
			if(params!=null && params.size()>0){
				for(int j = 0; j< params.size();j++){
					LocalParameter p = params.get(j);
					if(p.getId().equalsIgnoreCase("LOWER_BOUND")){
						lower = p.getValue();
						haskinetic = true;
					}else if(p.getId().equalsIgnoreCase("UPPER_BOUND")){
						upper = p.getValue();
						haskinetic = true;
					}
				}
			}
		}
		
		if(!isReversible && !haskinetic){
			lower = 0.0;
		}
		
		
			
		if(haskinetic)
			defaultEC.put(sbmlreaction.getId(), new ReactionConstraintCI(lower,upper));
		
	}
	
	
	
	/**
	 * This method adds mapping to reactants and products
	 * @param list List of reactants or products
	 * @param reactionId The reaction ID
	 * @param speciesInReactions A set with all the metabolites that participate in some reaction
	 * @return The mapping of reactants or products
	 */
	public Map<String, StoichiometryValueCI> addMapping(ListOf<SpeciesReference> list, String reactionId, Set<String> speciesInReactions, Map<String, String> mapMetaboliteIdCompartment){
		Map<String, StoichiometryValueCI> result = new HashMap<String, StoichiometryValueCI>();
		for(int l = 0;l<list.size();l++){
			SpeciesReference ref = (SpeciesReference)list.get(l);
			String idInModel = ref.getSpecies();
				
			result.put(idInModel,new StoichiometryValueCI(idInModel,ref.getStoichiometry(), mapMetaboliteIdCompartment.get(idInModel)));

			speciesInReactions.add(idInModel);
		}
		
		return result;
	}
}
