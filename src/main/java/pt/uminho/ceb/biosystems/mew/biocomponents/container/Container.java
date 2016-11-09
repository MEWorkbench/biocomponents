package pt.uminho.ceb.biosystems.mew.biocomponents.container;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ContainerException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionTypeEnum;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.interfaces.IContainerBuilder;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions.EntityDoesNotExistsException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions.ExceptionProperties;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions.MetaboliteDoesNotExistsException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions.ReactionAlreadyExistsException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions.ReactionDoesNotExistsException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions.StoichiometryDoesNotExistsException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;


public class Container implements Serializable, Cloneable/*
														 * implements
														 * IContainerBuilder
														 */{
	public static boolean debug = true;

	private static final long serialVersionUID = 1L;
	public static String NOPATHWAY = "NO_PATHWAY";
	protected String name;
	protected String organism;
	protected String notes;
	protected Integer version;

	protected Map<String, ReactionCI> reactions;
	protected Map<String, MetaboliteCI> metabolites;
	protected Map<String, CompartmentCI> compartments;
	protected Map<String, GeneCI> genes;

	protected Map<String, ReactionConstraintCI> defaultEC;
	protected String biomassId;
	private String ext_compartment = null;

	/*
	 * Information data
	 */
	protected Boolean hasUnicIdnt;
	protected Map<String, String> metaboliteToDrain;
	protected Map<String, String> drainToMetabolite;
	protected Set<String> drains;

	protected Map<String, Map<String, String>> reactionsExtraInfo;
	protected Map<String, Map<String, String>> metabolitesExtraInfo;

	private MetaboliteCI validateIfExists;

	public Container(IContainerBuilder builder, boolean throwerros) throws IOException {
		populateContainer(builder);
		verifyDepBetweenClass(throwerros);
	}
	
	public Container(IContainerBuilder builder) throws IOException {
		this(builder, true);
	}
	

	public Container() {
		reactions = new HashMap<String, ReactionCI>();
		metabolites = new HashMap<String, MetaboliteCI>();
		compartments = new HashMap<String, CompartmentCI>();
		genes = new HashMap<String, GeneCI>();
		defaultEC = new HashMap<String, ReactionConstraintCI>();
		metabolitesExtraInfo = new HashMap<String, Map<String, String>>();
		reactionsExtraInfo = new HashMap<String, Map<String, String>>();
	}

	public Container(Container container) {
		this.name = container.name;
		this.organism = container.organism;
		this.notes = container.notes;
		this.version = container.version;
		this.biomassId = container.biomassId;
		this.ext_compartment = container.ext_compartment;

		this.reactions = new HashMap<String, ReactionCI>();
		for (String reactionId : container.reactions.keySet())
			this.reactions.put(reactionId, container.getReaction(reactionId).clone());

		this.metabolites = new HashMap<String, MetaboliteCI>();
		for (String metaboliteId : container.metabolites.keySet())
			this.metabolites.put(metaboliteId, container.getMetabolite(metaboliteId).clone());

		this.compartments = new HashMap<String, CompartmentCI>();
		for (String compartmentId : container.compartments.keySet())
			this.compartments.put(compartmentId, container.getCompartment(compartmentId).clone());

		this.genes = new HashMap<String, GeneCI>(container.genes);
		for (String geneId : container.genes.keySet())
			this.genes.put(geneId, container.getGene(geneId).clone());

		this.defaultEC = new HashMap<String, ReactionConstraintCI>();
		for(String id : container.defaultEC.keySet()){
			this.defaultEC.put(id, container.defaultEC.get(id).clone());
		}

		this.metabolitesExtraInfo = new HashMap<String, Map<String, String>>();
		for (String extraInfo : container.getMetabolitesExtraInfo().keySet()) {
			this.metabolitesExtraInfo.put(extraInfo, new HashMap<String, String>());
			for (String metID : container.getMetabolitesExtraInfo().get(extraInfo).keySet()) {
				this.metabolitesExtraInfo.get(extraInfo).put(metID,
						container.getMetabolitesExtraInfo().get(extraInfo).get(metID));
			}
		}

		this.reactionsExtraInfo = new HashMap<String, Map<String, String>>();
		for (String extraInfo : container.getReactionsExtraInfo().keySet()) {
			this.reactionsExtraInfo.put(extraInfo, new HashMap<String, String>());
			for (String reacID : container.getReactionsExtraInfo().get(extraInfo).keySet()) {
				this.reactionsExtraInfo.get(extraInfo).put(reacID,
						container.getReactionsExtraInfo().get(extraInfo).get(reacID));
			}
		}
	}

	@Override
	public Container clone() {
		return new Container(this);
	}

	public void populateContainer(IContainerBuilder reader) {
		name = reader.getModelName();
		organism = reader.getOrganismName();
		notes = reader.getNotes();
		version = reader.getVersion();

		reactions = reader.getReactions();
		metabolites = reader.getMetabolites();
		compartments = reader.getCompartments();

		metabolitesExtraInfo = reader.getMetabolitesExtraInfo();
		if (metabolitesExtraInfo == null)
			metabolitesExtraInfo = new HashMap<String, Map<String, String>>();

		reactionsExtraInfo = reader.getReactionsExtraInfo();
		if (reactionsExtraInfo == null)
			reactionsExtraInfo = new HashMap<String, Map<String, String>>();

		ext_compartment = reader.getExternalCompartmentId();
		genes = reader.getGenes();
		if (genes == null)
			genes = new HashMap<String, GeneCI>();

		defaultEC = reader.getDefaultEC();
		if (defaultEC == null)
			defaultEC = new HashMap<String, ReactionConstraintCI>();
		biomassId = reader.getBiomassId();

		_defineTransportReactions();
		_defineDrainReactions();

	}

	public boolean hasUnicIds(){
//		if(hasUnicIdnt == null){
			hasUnicIdnt = identifyIfHasUniqueMetaboliteIds();
//		}
		return hasUnicIdnt;
	}

	public void clearInfoElements() {
		hasUnicIdnt = null;
		metaboliteToDrain = null;
		drainToMetabolite = null;
		drains = null;

	}

	public void checkContainer() throws Exception {
		Set<String> invalidStoi = identiyReactionsWithInvalidStoiquiometry();
		if (invalidStoi.size() > 0)
			throw new Exception("The following reactions have wrong stoichiometries " + invalidStoi);

		Map<String, Set<String>> duplicateReactions = identifyDuplicateReactionsByStequiometry(true);
		if (duplicateReactions.size() > 0)
			throw new Exception("Container contains duplicate reactions");
	}

	public String getBiomassFluxFromSizeHeuristic() {
		int maxsize = 0;
		String maxid = "";

		for (ReactionCI reaction : this.getReactions().values()) {
			int isize = reaction.getProducts().values().size() + reaction.getReactants().values().size();
			if (isize > maxsize) {
				maxsize = isize;
				maxid = reaction.getId();
			}
		}
		return maxid;
	}

	public Container standardizeContainerIds() throws Exception {
		Container ret = this.clone();
		ret.useUniqueIds();
		return ret;
	}

	public void standardizeMetaboliteIdsByCompartment() {
		Set<String> metaboliteIds = new HashSet<String>(metabolites.keySet());
		for (String mid : metaboliteIds) {
			MetaboliteCI m = metabolites.remove(mid);
			Set<String> reactionIds = m.getReactionsId();
			for (String rid : reactionIds) {
				ReactionCI r = reactions.get(rid);
				if (r.getProducts().containsKey(mid)) {
					StoichiometryValueCI stoich = r.getProducts().remove(mid);
					String new_mid = mid;
					if (!mid.endsWith("_" + stoich.getCompartmentId()))
						new_mid += "_" + stoich.getCompartmentId();

					stoich.setMetaboliteId(new_mid);
					r.getProducts().put(new_mid, stoich);
					if (!metabolites.containsKey(new_mid)) {
						compartments.get(stoich.getCompartmentId()).addMetaboliteInCompartment(new_mid);
						MetaboliteCI new_m = m.clone();
						new_m.setId(new_mid);
						metabolites.put(new_mid, new_m);
					}
				}
				if (r.getReactants().containsKey(mid)) {
					StoichiometryValueCI stoich = r.getReactants().remove(mid);
					String new_mid = mid;
					if (!mid.endsWith("_" + stoich.getCompartmentId()))
						new_mid += "_" + stoich.getCompartmentId();

					stoich.setMetaboliteId(new_mid);
					r.getReactants().put(new_mid, stoich);
					if (!metabolites.containsKey(new_mid)) {
						compartments.get(stoich.getCompartmentId()).addMetaboliteInCompartment(new_mid);
						MetaboliteCI new_m = m.clone();
						new_m.setId(new_mid);
						metabolites.put(new_mid, new_m);
					}
				}
				for (CompartmentCI c : compartments.values())
					c.getMetabolitesInCompartmentID().removeAll(metaboliteIds);

			}

		}
	}

	private Map<String, String> standardizeReactionIds() {
		Map<String, String> map = new HashMap<String, String>();
		for (String oldId : reactions.keySet()) {

			if (!oldId.startsWith("R_") && !oldId.startsWith("t_")) {
				ReactionCI r = reactions.get(oldId);

				String newId = null;
				if (r.getType().equals(ReactionTypeEnum.Transport)) {
					newId = "t_" + oldId;
					map.put(oldId, newId);
				} else {
					newId = "R_" + oldId;
					map.put(oldId, newId);
				}

				if (biomassId != null && biomassId.equals(oldId)) {
					biomassId = map.get(oldId);
				}

				ReactionConstraintCI cont = defaultEC.get(oldId);
				if (cont != null) {
					defaultEC.remove(oldId);
					defaultEC.put(newId, cont);
				}
			}
		}
		return map;
	}

	private Map<String, String> standardizeMetaboliteIds() {

		Map<String, String> map = new HashMap<String, String>();
		for (String oldId : metabolites.keySet()) {
			if (!oldId.startsWith("M_"))
				map.put(oldId, "M_" + oldId);
		}

		return map;
	}

	// COMPARTMENTS
	public void addCompartment(CompartmentCI comp) throws ContainerException {
		if (this.compartments.containsKey(comp.getId()))
			throw new ContainerException("The compartment id[" + comp.getId() + "] already exits");
		compartments.put(comp.getId(), comp);
	}

	public void addExternalCompartment(String compartmentId, String CompartmentName) {
		CompartmentCI newCompartment = new CompartmentCI(compartmentId, compartmentId, null);
		CompartmentCI extComp = getExternalCompartment();
		extComp.setOutside(compartmentId);
		compartments.put(compartmentId, newCompartment);
	}

	public void moveMetabolitesToCompartment(Collection<String> metabolitesId, String newComp) throws Exception {

		for (String metId : metabolitesId) {
			List<String> comps = new ArrayList<String>();
			for (CompartmentCI comp : compartments.values()) {
				if (comp.getMetabolitesInCompartmentID().contains(metId))
					comps.add(comp.getId());
			}
			for (String compId : comps) {
				moveMetabolitesToCompartment(metId, compId, newComp);
			}
		}

	}

	public void moveMetabolitesToCompartment(String metId, String oldCompartmentPosi, String newCompartmentPosi)
			throws Exception {
		if (!compartments.containsKey(oldCompartmentPosi) || !compartments.containsKey(newCompartmentPosi))
			throw new Exception("Container does not have the compartment id");

		if (!compartments.get(oldCompartmentPosi).getMetabolitesInCompartmentID().contains(metId))
			throw new Exception("Metabolite does not exist in oldCompartmentPosi " + metId);

		compartments.get(oldCompartmentPosi).getMetabolitesInCompartmentID().remove(metId);
		Set<String> reactionsList = metabolites.get(metId).getReactionsId();

		for (String reactionId : reactionsList) {
			Map<String, StoichiometryValueCI> reactants = reactions.get(reactionId).getReactants();
			Map<String, StoichiometryValueCI> products = reactions.get(reactionId).getProducts();
			changeCompartmentInMetaboliteStoichiometry(metId, oldCompartmentPosi, newCompartmentPosi, reactants);
			changeCompartmentInMetaboliteStoichiometry(metId, oldCompartmentPosi, newCompartmentPosi, products);
		}
		compartments.get(newCompartmentPosi).getMetabolitesInCompartmentID().add(metId);
	}

	public void changeCompartmentInMetaboliteStoichiometry(String metabId, String oldComp, String newComp,
			Map<String, StoichiometryValueCI> stoic) {

		for (StoichiometryValueCI value : stoic.values()) {

			String metIdValue = value.getMetaboliteId();
			String compValue = value.getCompartmentId();
			if (metabId.equals(metIdValue) && compValue.equals(oldComp)) {
				value.setCompartmentId(newComp);
			}
		}
	}

	
	public void changeCompartmentIds(Map<String, String> oldNewIds) throws IOException{
		for(String id : oldNewIds.keySet()){
			changeCompartmentId(id, oldNewIds.get(id), false);
		}
		verifyDepBetweenClass();
	}
	
	public void changeCompartmentId(String oldId, String newId){
		changeCompartmentId(oldId, newId, true);
	}
	
	public void changeCompartmentId(String oldId, String newId, boolean verify) {
		CompartmentCI c = compartments.remove(oldId);
		c.setId(newId);
		compartments.put(newId, c);
		for (String cId : compartments.keySet()) {
			if (compartments.get(cId).getOutside() != null && compartments.get(cId).getOutside().equals(oldId))
				compartments.get(cId).setOutside(newId);
		}

		Set<String> metIds = c.getMetabolitesInCompartmentID();
		Set<String> reactionIds = new HashSet<String>();

		for (String met : metIds) {
			MetaboliteCI m = metabolites.get(met);
			reactionIds.addAll(m.getReactionsId());
		}

		for (String reactionId : reactionIds)
			changeCompartmentInReaction(reactionId, oldId, newId);
		
		if(verify)
			try {
				verifyDepBetweenClass();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	}

	public void changeCompartmentInReaction(String reactionId, String oldId, String newId) {
		ReactionCI r = reactions.get(reactionId);
		changeCompartmentInStoichiometry(r.getReactants(), oldId, newId);
		changeCompartmentInStoichiometry(r.getProducts(), oldId, newId);
	}

	public void changeCompartmentInStoichiometry(Map<String, StoichiometryValueCI> stoichiometries, String oldId,
			String newId) {
		for (String mId : stoichiometries.keySet()) {
			StoichiometryValueCI svci = stoichiometries.get(mId);
			if (svci.getCompartmentId().equals(oldId))
				svci.setCompartmentId(newId);
		}
	}

	public void removeCompartmetAndReactions(String comp){
		Set<String> remMet = compartments.get(comp).getMetabolitesInCompartmentID();
//		removeMetaboliteAndItsReactions(remMet);
		
		Set<String> reactions = new HashSet<>();
		for(String metId : remMet)
			reactions.addAll(getReactionsByMetaboliteAndCompartment(metId, comp));
			
		removeReactions(reactions);
	}
	
	
	
	public Set<String> getReactionsByMetaboliteAndCompartment(
			String metId, String comp) {
		
		Set<String> rs = new HashSet<String>();
		for(String rId : getMetabolite(metId).getReactionsId()){
			if(getReaction(rId).containsMetabolite(metId, comp))
				rs.add(rId);
		}
		
		return rs;
	}

	//	FIXME this removeCompartment desbalancing reactions
	@Deprecated
	public void removeCompartmet(String comp) {
		Set<String> remMet = compartments.get(comp).getMetabolitesInCompartmentID();
		Set<String> reacIdsSet = new HashSet<String>();
		for (String metId : remMet) {
			reacIdsSet.addAll(metabolites.get(metId).getReactionsId());
		}

		for (String reacId : reacIdsSet) {
			removeMetInStoic(reactions.get(reacId).getProducts(), remMet, comp);
			removeMetInStoic(reactions.get(reacId).getReactants(), remMet, comp);
		}
		compartments.remove(comp);
	}

	public void changeMetaboliteIds(Map<String, String> dicOldNew) throws Exception{
		changeMetaboliteIds(dicOldNew, false);
	}
	
	
	// METABOLITES
	public void changeMetaboliteIds(Map<String, String> dicOldNew, boolean agregateMet) throws Exception{
		
		Set<String> existingMet = CollectionUtils.getIntersectionValues(dicOldNew.keySet(), metabolites.keySet());
		for(String id : existingMet){
			changeMetaboliteId(id,  dicOldNew.get(id), agregateMet);
		}
	}
	
	public Set<String> getMetabolitesWithoutPathway() {
		Set<String> metabolitesWithoutPathways = new TreeSet<String>();
		Set<String> metabolitesOfReactionsWithoutPathways = colectMetaboliteReactionNoPathways();
		boolean havePathway;

		for (String m : metabolitesOfReactionsWithoutPathways) {
			havePathway = false;
			for (String reacId : metabolites.get(m).getReactionsId()) {
				ReactionCI r = reactions.get(reacId);
				if (r.getSubsystem() != null && !r.getSubsystem().equals("")) {
					havePathway = true;
					break;
				}
			}
			if (!havePathway)
				metabolitesWithoutPathways.add(m);
		}
		return metabolitesWithoutPathways;
	}

	public Set<String> colectMetaboliteReactionNoPathways() {
		Set<String> reactionsWithoutPathways = getReactionsWithoutPathway();
		Set<String> metabolitesOfReactionsWithoutPathays = new TreeSet<String>();

		for (String r : reactionsWithoutPathways) {
			metabolitesOfReactionsWithoutPathays.addAll(reactions.get(r).getMetaboliteSetIds());
		}
		return metabolitesOfReactionsWithoutPathays;
	}

	public Set<String> removeMetaboliteAndItsReactions(Set<String> metaboliteIds) {
		Set<String> removedReactions = new HashSet<String>();
		for (String metId : metaboliteIds) {
			removedReactions.addAll(removeMetaboliteAndItsReactions(metId, false));
		}
		try {
			verifyDepBetweenClass();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return removedReactions;
	}

	public Set<String> removeMetaboliteAndItsReactions(String metaboliteId) {
		return removeMetaboliteAndItsReactions(metaboliteId, true);
	}

	protected Set<String> removeMetaboliteAndItsReactions(String metaboliteId, boolean verify) {
		Set<String> reactions = new TreeSet<String>(metabolites.get(metaboliteId).getReactionsId());
		for (String reactionId : reactions) {
			_removeReaction(reactionId);
		}
		_removeMetabolite(metaboliteId);

		if (verify)
			try {
				verifyDepBetweenClass();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return reactions;
	}

	public void removeMetabolites(Collection<String> metabIDs) {
		for (String metId : metabIDs) {
			Set<String> reactions = metabolites.get(metId).getReactionsId();
			metabolites.remove(metId);

			for (String reactionId : reactions) {
				this.reactions.get(reactionId).getProducts().remove(metId);
				this.reactions.get(reactionId).getReactants().remove(metId);
			}
			_removeFromMetaboliteExtraInfo(metId);
			removeMetaboliteFromAllCompartments(metId);
		}
		_defineDrainReactions();
		try {
			verifyDepBetweenClass();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean metabolitesHasFormula() {
		for (MetaboliteCI m : metabolites.values()) {
			if (m.getFormula() != null && !m.getFormula().equals(""))
				return true;
		}
		return false;
	}

	public MetaboliteCI searchMetaboliteByName(String metaboliteName) {
		MetaboliteCI result = null;
		for (MetaboliteCI met : metabolites.values()) {
			if (met.getName().equals(metaboliteName)) {
				result = met;
				break;
			}
		}
		return result;
	}

	public Set<String> getMetaboliteCompartments(String metId) {
		Set<String> ret = new HashSet<String>();
		for (CompartmentCI c : getCompartments().values()) {
			if (c.getMetabolitesInCompartmentID().contains(metId))
				ret.add(c.getId());
		}
		return ret;
	}

	public void useUniqueIds() {
		HashMap<String, MetaboliteCI> newMetabolites = new HashMap<String, MetaboliteCI>();

		Map<String, Set<String>> oldToNew = new HashMap<String, Set<String>>();
		for (String comp : compartments.keySet()) {
			Set<String> metsInComp = new TreeSet<String>();

			for (String oldMetId : metabolites.keySet()) {
				MetaboliteCI met = metabolites.get(oldMetId);
				String newId = oldMetId + "_" + comp;
				MetaboliteCI newMet = met.clone();
				newMet.setId(newId);
				metsInComp.add(newId);
				newMetabolites.put(newId, newMet);
				
				Set<String> newIds = oldToNew.get(oldMetId);
				if(newIds == null)	newIds = new HashSet<String>();
				newIds.add(newId);
				oldToNew.put(oldMetId, newIds);
			}
			compartments.get(comp).setMetabolitesInCompartmentID(metsInComp);
		}

		for (ReactionCI r : reactions.values()) {
			Map<String, StoichiometryValueCI> newReactants = useMetaUniqueIdsStoic(r.getReactants());
			Map<String, StoichiometryValueCI> newProducts = useMetaUniqueIdsStoic(r.getProducts());
			r.setReactants(newReactants);
			r.setProducts(newProducts);
		}
		metabolites = newMetabolites;
		changeAllIdInExtraInfo(oldToNew, metabolitesExtraInfo);
		try {
			verifyDepBetweenClass();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private void changeAllIdInExtraInfo(Map<String, Set<String>> oldToNew,  Map<String, Map<String, String>> extraInfo){
		
		for(String old : oldToNew.keySet()){
			Set<String> newIds = oldToNew.get(old);
			changeIdInExtraInfo(old, newIds, extraInfo);
		}
	}
	

	private void changeIdInExtraInfo(String oldId, String newId, Map<String, Map<String, String>> extraInfo){
		for(Map<String,String> extra : extraInfo.values()){
			String info = extra.remove(oldId);
			if(info!=null)
				extra.put(newId, info);
		}
	}
	
	private void changeIdInExtraInfo(String old, Set<String> newIds, Map<String, Map<String, String>> extraInfo){
		
		for(Map<String, String> toChange : metabolitesExtraInfo.values()){
			String value = toChange.remove(old);
			if(value != null)
			for(String newId : newIds)
				toChange.put(newId, value);
		}
	}

	private Map<String, StoichiometryValueCI> useMetaUniqueIdsStoic(Map<String, StoichiometryValueCI> stoicMap) {
		Map<String, StoichiometryValueCI> newStoicMap = new HashMap<String, StoichiometryValueCI>();
		for (String metID : stoicMap.keySet()) {
			StoichiometryValueCI stoich = stoicMap.get(metID);
			String newId = metID + "_" + stoich.getCompartmentId();
			StoichiometryValueCI newStoic = stoich.clone();
			newStoic.setMetaboliteId(newId);
			newStoicMap.put(newId, newStoic);
		}
		return newStoicMap;
	}

	public Map<String, String> stripDuplicateMetabolitesInfoById(Pattern metaboliteIDpattern) throws Exception {
		Map<String, String> oldToNewId = new HashMap<String, String>();
		Map<String, MetaboliteCI> newMetabolites = new HashMap<String, MetaboliteCI>();

		for (String oldId : metabolites.keySet()) {
			MetaboliteCI metaboliteInfo = metabolites.get(oldId);
			Matcher matcher = metaboliteIDpattern.matcher(oldId);

			if (matcher.matches()) {
				String newId = matcher.group(1);
				oldToNewId.put(oldId, newId);
				metaboliteInfo.setId(newId);
				if (!newMetabolites.containsKey(newId))
					newMetabolites.put(newId, metaboliteInfo);
				changeIdInExtraInfo(oldId, newId, metabolitesExtraInfo);
			} else
				throw new Exception("Metabolite [" + oldId + "] does not matche to pattern");
		}

		this.metabolites = newMetabolites;

		for (ReactionCI reaction : reactions.values()) {
			changeMetabolitesIdInReaction(reaction, oldToNewId);
		}
		verifyDepBetweenClass();
		return oldToNewId;
	}

	public Map<String, String> stripDuplicateMetabolitesInfoByName(Pattern metaboliteNamePattern) throws Exception {
		Map<String, String> oldToNewId = new HashMap<String, String>();
		Map<String, MetaboliteCI> newMetabolites = new HashMap<String, MetaboliteCI>();
		Map<String, String> nameToId = new HashMap<String, String>();

		for (String oldId : metabolites.keySet()) {
			MetaboliteCI metaboliteInfo = metabolites.get(oldId);
			String oldMetaboliteName = metaboliteInfo.getName();
			Matcher matcher = metaboliteNamePattern.matcher(oldMetaboliteName);

			if (matcher.matches()) {
				String newMetaboliteName = matcher.group(1);
				String newId = null;
				if (nameToId.containsKey(newMetaboliteName)) {
					newId = nameToId.get(newMetaboliteName);
				} else {
					nameToId.put(newMetaboliteName, oldId);
					newId = oldId;
					metaboliteInfo.setName(newMetaboliteName);
				}
				oldToNewId.put(oldId, newId);
				metaboliteInfo.setId(newId);

				if (!newMetabolites.containsKey(newId))
					newMetabolites.put(newId, metaboliteInfo);
			} else
				throw new Exception("Metabolite name does not match to pattern");
		}

		this.metabolites = newMetabolites;

		for (ReactionCI reaction : reactions.values()) {
			changeMetabolitesIdInReaction(reaction, oldToNewId);
		}

		verifyDepBetweenClass();
		return oldToNewId;
	}

	public void changeMetabolitesIdInReaction(ReactionCI reaction, Map<String, String> metDictionary) {
		Map<String, StoichiometryValueCI> products = reaction.getProducts();
		Map<String, StoichiometryValueCI> reactants = reaction.getReactants();

		reaction.setProducts(changeMetabolitesIdInStoich(products, metDictionary));
		reaction.setReactants(changeMetabolitesIdInStoich(reactants, metDictionary));
	}

	private Map<String, StoichiometryValueCI> changeMetabolitesIdInStoich(
			Map<String, StoichiometryValueCI> stoicValues, Map<String, String> metDictionary) {
		Map<String, StoichiometryValueCI> newStoic = new HashMap<String, StoichiometryValueCI>();
		for (StoichiometryValueCI value : stoicValues.values()) {
			String oldId = value.getMetaboliteId();
			if (metDictionary.containsKey(oldId)) {
				String newId = metDictionary.get(oldId);
				value.setMetaboliteId(newId);
				newStoic.put(newId, value);
			} else
				newStoic.put(oldId, value);
		}
		return newStoic;
	}

	// REACTIONS
	public Map<String, MetaboliteCI> getReactionMetabolite(String reactionId) {
		Map<String, MetaboliteCI> reactionMetabolites =
				new HashMap<String, MetaboliteCI>();
		ReactionCI r = getReaction(reactionId);
		for(String mId : r.getMetaboliteSetIds()) {
			reactionMetabolites.put(mId, getMetabolite(mId));
		}
		
		return reactionMetabolites;
	}
	/**
	 * This method gets all the subsystems IDs from the reactions
	 * 
	 * @return The pathways IDs
	 */
	public TreeSet<String> getPathwaysIDs() {
		TreeSet<String> pathways = new TreeSet<String>();
		for (ReactionCI r : reactions.values()) {
			if (r.getSubsystem() != null && !r.getSubsystem().matches("\\s*"))
				pathways.add(r.getSubsystem());
		}
		return pathways;
	}

	public boolean hasReactionsWithoutPathway() {
		for (ReactionCI r : reactions.values()) {
			if (r.getSubsystem() == null || r.getSubsystem().equals(""))
				return true;
		}
		return false;
	}

	public Set<String> getReactionsWithoutPathway() {
		Set<String> reactionsWithoutPathways = new TreeSet<String>();
		for (String s : reactions.keySet()) {
			if (reactions.get(s).getSubsystem() == null || reactions.get(s).getSubsystem().equals(""))
				reactionsWithoutPathways.add(s);
		}
		return reactionsWithoutPathways;
	}

	public void generateExtraPathway(Set<String> reactionsWithoutPathways) {
		if (reactionsWithoutPathways != null)
			for (String s : reactionsWithoutPathways)
				reactions.get(s).setSubsystem(NOPATHWAY);
	}

	public void undoGenerateExtraPathway(Set<String> reactionsWithoutPathways) {
		if (reactionsWithoutPathways != null)
			for (String s : reactionsWithoutPathways)
				reactions.get(s).setSubsystem("");
	}

	public void changeReactionIds(Map<String, String> dictOldNew) throws Exception {
		for (String oldId : dictOldNew.keySet())
			changeReactionId(oldId, dictOldNew.get(oldId), false);

		verifyDepBetweenClass();
	}

	public void changeReactionId(String oldId, String newId) throws Exception {
		changeReactionId(oldId, newId, true);
	}

	public void changeReactionId(String oldId, String newId, boolean verify) throws Exception {
		ReactionCI r = reactions.get(oldId);
		if (reactions.containsKey(newId))
			throw new Exception("New Id " + newId +" already exists");

		r.setId(newId);
		reactions.remove(oldId);
		reactions.put(newId, r);

		ReactionConstraintCI ci = getDefaultEC().remove(oldId);
		if(ci!=null) getDefaultEC().put(newId, ci);
		changeIdInExtraInfo(oldId, newId, reactionsExtraInfo);
		if (verify)
			verifyDepBetweenClass();
	}

	public Map<String, ReactionCI> getReactionsByPathway(String s) {
		Map<String, ReactionCI> res = new HashMap<String, ReactionCI>();
		for (String rId : reactions.keySet()) {
			if (reactions.get(rId).getSubsystem() != null) {
				if (reactions.get(rId).getSubsystem().equals(s))
					res.put(rId, reactions.get(rId));
			}
		}
		return res;
	}

	public Map<String, String> calculateCoFactors(int reactionNumber) {
		Map<String, String> cofactors = new HashMap<String, String>();
		for (String m : this.metabolites.keySet()) {
			Set<String> result = new TreeSet<String>();
			MetaboliteCI met = getMetabolite(m);
			Set<String> setReactions = met.getReactionsId();

			if (setReactions.size() >= reactionNumber && !cofactors.containsKey(met.getId())
					&& !cofactors.containsValue(met.getId())) {
				result = getReaction((String) setReactions.toArray()[0]).getMetaboliteSetIds();
				for (String r : setReactions) {
					Set<String> metabolites = getReaction(r).getMetaboliteSetIds();
					result = CollectionUtils.getIntersectionValues(result, metabolites);
				}

				Object[] o = result.toArray();
				if (result.size() == 2 && isCofactor((String) o[0], (String) o[1])) {
					cofactors.put((String) o[0], (String) o[1]);
				}
			}
		}
		return cofactors;
	}

	private boolean isCofactor(String m1, String m2) {

		MetaboliteCI met1 = metabolites.get(m1);
		MetaboliteCI met2 = metabolites.get(m2);

		if (met1.getReactionsId().containsAll(met2.getReactionsId())
				&& met2.getReactionsId().containsAll(met1.getReactionsId()))
			return true;

		return false;
	}

	/**
	 * This method checks if there is at least one internal reaction with an
	 * ECNumber defined
	 */
	public boolean internalReactionsHaveECNumber() {
		Set<String> notDrains = CollectionUtils.getSetDiferenceValues(getReactions().keySet(), getDrains());
		for (String s : notDrains) {
			if (reactions.get(s).getEc_number() != null && !reactions.get(s).getEc_number().matches("\\s*"))
				return true;
		}
		return false;
	}

	/**
	 * This method checks if there is at least one internal reaction with a
	 * pathway defined
	 */
	public boolean internalReactionsHavePathway() {
		Set<String> notDrains = CollectionUtils.getSetDiferenceValues(getReactions().keySet(), getDrains());
		for (String s : notDrains) {
			if (reactions.get(s).getSubsystem() != null && !reactions.get(s).getSubsystem().matches("\\s*"))
				return true;
		}
		return false;
	}

	public Map<String, Double> balanceOfReactions(Set<String> reactionsIds) {
		Map<String, Double> balance = new HashMap<String, Double>();
		for (String id : reactionsIds) {
			ReactionCI r = getReaction(id);
			Map<String, StoichiometryValueCI> produts = r.getProducts();
			Map<String, StoichiometryValueCI> reactants = r.getReactants();

			balance(balance, produts, 1.0);
			balance(balance, reactants, -1.0);
		}
		return balance;
	}

	private void balance(Map<String, Double> data, Map<String, StoichiometryValueCI> stoic, Double p) {
		for (StoichiometryValueCI v : stoic.values()) {
			String m = v.getMetaboliteId();
			Double s = v.getStoichiometryValue();
			Double sum = data.get(m);
			sum = (sum == null) ? p * s : sum + (p * s);
			if (sum != 0)
				data.put(m, sum);
			else
				data.remove(m);
		}
	}

	public void addTransportReactions(Set<String> metaboliteIds, String compartmentId1, String compartmentId2)
			throws Exception {

		for (String metId : metaboliteIds)
			addTransportReactions(metId, compartmentId1, compartmentId2);
	}

	public void addTransportReactions(String metaboliteId, String compartmentId1, String compartmentId2)
			throws Exception {

		if (!compartments.containsKey(compartmentId1) || !compartments.containsKey(compartmentId2))
			throw new Exception("Compartment does not exist in container");

		if (!metabolites.containsKey(metaboliteId))
			throw new Exception("Metabolite does not exist in container");

		MetaboliteCI met = metabolites.get(metaboliteId);
		CompartmentCI comp1 = compartments.get(compartmentId1);
		CompartmentCI comp2 = compartments.get(compartmentId2);

		HashMap<String, StoichiometryValueCI> reactants = new HashMap<String, StoichiometryValueCI>();
		reactants.put(metaboliteId, new StoichiometryValueCI(metaboliteId, 1.0, comp1.getId()));

		HashMap<String, StoichiometryValueCI> products = new HashMap<String, StoichiometryValueCI>();
		products.put(metaboliteId, new StoichiometryValueCI(metaboliteId, 1.0, comp2.getId()));

		String reactionId = "t_" + met.getId() + "_" + comp1.getId() + "_" + comp2.getId();

		ReactionCI reaction = new ReactionCI(reactionId, "transport " + met.getName(), true, reactants, products);

		comp1.getMetabolitesInCompartmentID().add(metaboliteId);
		comp2.getMetabolitesInCompartmentID().add(metaboliteId);
		met.getReactionsId().add(reactionId);

		reactions.put(reactionId, reaction);
	}

	public ReactionCI removeReaction(String id) {
		return removeReaction(id, true);
	}

	public ReactionCI removeReaction(String id, boolean verify) {
		ReactionCI removed = _removeReaction(id);
		clearInfoElements();
		if (verify) {
			try {
				verifyDepBetweenClass();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return removed;
	}

	public Map<String, ReactionCI> removeReactions(Collection<String> reactionsToRemove) {

		Map<String, ReactionCI> removed = new HashMap<String, ReactionCI>();
		for (String reactionid : reactionsToRemove) {
			ReactionCI r = removeReaction(reactionid, false);
			removed.put(reactionid, r);
		}
		try {
			verifyDepBetweenClass();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return removed;
	}

	// FIXME:
	public void removeDuplicateReactionsByStequiometry() {
		Map<String, Set<String>> dReactions = identifyDuplicateReactionsByStequiometry(true);
		int i = 0;

		for (String id : dReactions.keySet()) {
			Set<String> reactionsToRemove = dReactions.get(id);
			for (String rReaction : reactionsToRemove) {
				i++;
				removeReaction(rReaction, false);
			}
		}
		try {
			verifyDepBetweenClass();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (debug)
			System.out.println("..." + i + " Reactions Removed");
	}

	// DRAINS
	public void constructDrains(Collection<String> set, String extComp) throws Exception {
		constructDrains(set, extComp, 0.0, 50);
	}

	public void constructDrains(Collection<String> set) throws Exception {
		constructDrains(set, 0.0, 50);
	}

	public void constructDrains(Collection<String> set, double lower, double upper) throws Exception {
		constructDrains(set, null, lower, upper);
	}

	public void constructDrains(Collection<String> set, String extComp, double lower, double upper) throws Exception {
		for (String metId : set) {
			String comp = extComp;
			if (comp == null) {
				Set<String> compartments = getMetaboliteCompartments(metId);
				System.out.println(metId + "\t" + compartments );
				if (compartments.size() > 1)
					throw new Exception("To create drains, you must have just one compartment!");

				comp = compartments.iterator().next();
			}
			
			constructDrain(metId, comp, lower, upper, false);
		}
		verifyDepBetweenClass();
		clearInfoElements();
	}

	public String constructDrain(String metaboliteID, String comp, double lower, double upper) throws Exception {

		return constructDrain(metaboliteID, comp, lower, upper, true);
	}

	
	public String constructDrain(String metaboliteID, String comp) throws Exception{
		return constructDrain(metaboliteID, comp,  0.0, 50, true);
		
	}
	
	private String constructDrain(String metaboliteID, String comp, double lower, double upper, boolean verify)
			throws Exception {
		String drainId = "R_EX_" + metaboliteID.replaceFirst("M_", "") + "_";
		if (reactions.get(drainId) != null){
			
			getDefaultEC().put(drainId, new ReactionConstraintCI(lower, upper));
			return drainId;
		}

		Map<String, StoichiometryValueCI> reactants = new HashMap<String, StoichiometryValueCI>();
		reactants.put(metaboliteID, new StoichiometryValueCI(metaboliteID, 1.0, comp));
		Map<String, StoichiometryValueCI> products = new HashMap<String, StoichiometryValueCI>();
		ReactionCI newReaction = new ReactionCI(drainId, "Drain to " + metabolites.get(metaboliteID).getName(), true,
				reactants, products);

		compartments.get(comp).getMetabolitesInCompartmentID().add(metaboliteID);
		metabolites.get(metaboliteID).getReactionsId().add(drainId);
		newReaction.setType(ReactionTypeEnum.Drain);
		reactions.put(drainId, newReaction);

		defaultEC.put(drainId, new ReactionConstraintCI(lower, upper));

		if (verify) {
			try {
				verifyDepBetweenClass();
			} catch (IOException e) {
				// NOTE: Error!! I should not be here... This validation was
				// done in
				// constructor
				System.out.println("Error!! I should not be here... This validation was done in constructor");
				e.printStackTrace();
			}
			clearInfoElements();
		}

		return drainId;
	}

	public void putDrainsInProductsDirection() {
		for (String id : getDrains()) {
			ReactionCI reaction = getReaction(id);
			Map<String, StoichiometryValueCI> reactants = reaction.getReactants();
			Map<String, StoichiometryValueCI> products = reaction.getProducts();

			if (products.size() == 0) {
				reaction.setProducts(reactants);
				reaction.setReactants(products);
				ReactionConstraintCI rc = defaultEC.get(reaction.getId());
				if (rc != null) {
					ReactionConstraintCI newrc = new ReactionConstraintCI(-rc.getUpperLimit(), -rc.getLowerLimit());
					defaultEC.put(reaction.getId(), newrc);
				} else if (!reaction.isReversible())
					defaultEC.put(id, new ReactionConstraintCI(-ReactionConstraintCI.INFINITY, 0.0));
			}
		}
	}

	public void putDrainsInReactantsDirection() {
		for (String id : getDrains()) {
			ReactionCI reaction = getReaction(id);
			Map<String, StoichiometryValueCI> reactants = reaction.getReactants();
			Map<String, StoichiometryValueCI> products = reaction.getProducts();

			if (reactants.size() == 0) {
				reaction.setProducts(reactants);
				reaction.setReactants(products);
				ReactionConstraintCI rc = defaultEC.get(reaction.getId());
				if (rc != null) {
					ReactionConstraintCI newrc = new ReactionConstraintCI(-rc.getUpperLimit(), -rc.getLowerLimit());
					defaultEC.put(reaction.getId(), newrc);
				} else if (!reaction.isReversible())
					defaultEC.put(id, new ReactionConstraintCI(-ReactionConstraintCI.INFINITY, 0.0));
			}

		}
	}

	public void swapDrainsDirection() {
		for (ReactionCI reaction : reactions.values()) {
			if (reaction.isDrain()) {
				Map<String, StoichiometryValueCI> reactants = reaction.getReactants();
				Map<String, StoichiometryValueCI> products = reaction.getProducts();
				reaction.setProducts(reactants);
				reaction.setReactants(products);
			}
		}
	}

	public void swapdefaulDraintBounds() {
		for (ReactionCI reaction : reactions.values()) {
			if (reaction.isDrain()) {
				if (defaultEC.containsKey(reaction.getId())) {
					ReactionConstraintCI cont = defaultEC.get(reaction.getId());
					defaultEC.put(reaction.getId(),
							new ReactionConstraintCI(-cont.getUpperLimit(), -cont.getLowerLimit()));
				}
			}
		}
	}

	public Map<String, String> getMetaboliteToDrain() throws Exception {
		if (metaboliteToDrain == null)
			associateDrainToMetabolite();
		return metaboliteToDrain;
	}

	public Map<String, String> getDrainToMetabolite() {
		if (drainToMetabolite == null)
			try {
				associateDrainToMetabolite();
			} catch (Exception e) {
				e.printStackTrace();
			}

		return drainToMetabolite;
	}

	public void associateDrainToMetabolite() throws Exception {
		Set<String> _drains = getDrains();
		if (_drains.size() > 0) {
			drainToMetabolite = new HashMap<String, String>();
			metaboliteToDrain = new HashMap<String, String>();
		}

		for (String id : _drains) {
			ReactionCI drain = getReaction(id);
			Set<String> met = new HashSet<String>(drain.getProducts().keySet());
			met.addAll(drain.getReactants().keySet());

			if (met.size() > 1)
				throw new Exception("Drain " + id + " has more than one metabolite associated " + met);

			for (String metId : met) {
				if (metaboliteToDrain.containsKey(met))
					throw new Exception("Metabolite " + metId + " has more than one drain associated "
							+ metaboliteToDrain.containsKey(met) + " " + id);

				metaboliteToDrain.put(metId, id);
				drainToMetabolite.put(id, metId);
			}
		}
	}

	// GENES
	public boolean hasGeneInformation() {
		return (genes != null && !genes.isEmpty());
	}

	public void recalculateGenes() {
		HashMap<String, GeneCI> newGenes = new HashMap<String, GeneCI>();

		for (ReactionCI r : this.reactions.values()) {
			Set<String> genes = r.getGenesIDs();
			for (String g : genes) {
				if (!newGenes.containsKey(g)) {
					GeneCI newG = new GeneCI(g, this.genes.get(g).getGeneName());
					newGenes.put(g, newG);
				}
				newGenes.get(g).addReactionId(r.getId());
			}
		}
		this.genes = newGenes;
	}

	// IDENTIFY
	// _____________________________________
	public Set<String> identifyMetabolitesWithTransport(String compartmentId1, String compartmentId2) throws Exception {

		if (!compartments.containsKey(compartmentId1) || !compartments.containsKey(compartmentId2))
			throw new Exception("Compartment does not exist in container");

		Set<String> metWitTrans = new TreeSet<String>();
		for (String metId : compartments.get(compartmentId1).getMetabolitesInCompartmentID()) {
			boolean hasTransport = identifyMetaboliteHasTransport(metId, compartmentId1, compartmentId2);
			if (hasTransport)
				metWitTrans.add(metId);
		}
		return metWitTrans;
	}

	public Set<String> identifyMetabolitesWithoutTransport(String compartmentId1, String compartmentId2)
			throws Exception {

		if (!compartments.containsKey(compartmentId1) || !compartments.containsKey(compartmentId2))
			throw new Exception("Compartment does not exist in container");

		Set<String> metWithoutTrans = new TreeSet<String>();

		for (String metId : compartments.get(compartmentId1).getMetabolitesInCompartmentID()) {
			boolean hasTransport = identifyMetaboliteHasTransport(metId, compartmentId1, compartmentId2);
			if (!hasTransport)
				metWithoutTrans.add(metId);
		}
		return metWithoutTrans;
	}

	public Set<String> identifyTrnasportersByMetabolite(String metId){
		return identifyTrnasportersByMetabolite(metId, null, null);
	}
	
	public Set<String> identifyTrnasportersByMetabolite(String metId, String comp1, String comp2){
		
		Set<String> ret = new HashSet<>();
		MetaboliteCI metInfo = metabolites.get(metId);
		Set<String> reactionsWithMet = metInfo.getReactionsId();
		
		for(String rId : reactionsWithMet){
			
			ReactionCI r = getReaction(rId);
			StoichiometryValueCI reactant = r.getReactants().get(metId);
			StoichiometryValueCI products = r.getProducts().get(metId);
			
			if(reactant != null && products !=null){
				if((comp1==null && comp2==null)  
						|| (comp1.equals(reactant.getCompartmentId()) && comp2.equals(products.getCompartmentId()))
						|| (comp2.equals(reactant.getCompartmentId()) && comp1.equals(products.getCompartmentId()))){
					ret.add(rId);
				}
			}
		}
		return ret;
	}
	
	public boolean identifyMetaboliteHasTransport(String metId, String compartmentId1, String compartmentId2)
			throws Exception {

		if (!compartments.containsKey(compartmentId1) || !compartments.containsKey(compartmentId2))
			throw new Exception("Compartment does not exist in container");

		MetaboliteCI metInfo = metabolites.get(metId);
		Set<String> reactionsWithMet = metInfo.getReactionsId();

		boolean hasTransport = compartments.get(compartmentId1).getMetabolitesInCompartmentID().contains(metId)
				&& compartments.get(compartmentId2).getMetabolitesInCompartmentID().contains(metId);

		if (hasTransport)
			for (String reactionId : reactionsWithMet) {
				ReactionCI reaction = reactions.get(reactionId);
				StoichiometryValueCI st1 = reaction.getProducts().get(metId);
				StoichiometryValueCI st2 = reaction.getReactants().get(metId);

				hasTransport = st1 != null
						&& st2 != null
						&& ((st1.getCompartmentId().equals(compartmentId1) && st2.getCompartmentId().equals(
								compartmentId2)) || (st1.getCompartmentId().equals(compartmentId2) && st2
								.getCompartmentId().equals(compartmentId1)));

				if (hasTransport)
					break;
			}
		return hasTransport;
	}

	public Map<String, Set<String>> identifyDuplicateReactionsByStequiometry(boolean takeCareRev) {
		return identifyDuplicateReactionsByStequiometry(takeCareRev, false);
	}

	public Set<String> getReactionsByType(ReactionTypeEnum rt){
		
		Set<String> ret = new HashSet<String>();
		for(ReactionCI r : reactions.values())
			if(r.getType().equals(rt))
				ret.add(r.getId());
		
		return ret;
	}
	
	public Map<String, Set<String>> identifyDuplicateReactionsByStequiometry(boolean takeCareRev,
			boolean ignoreCompartments) {

		Map<String, Set<String>> ret = new HashMap<String, Set<String>>();
		Set<String> verifiedReactions = new TreeSet<String>();

		for (ReactionCI react : reactions.values()) {
			if (!verifiedReactions.contains(react.getId())) {
				verifiedReactions.add(react.getId());

				Set<String> theSame = new HashSet<String>();

				for (ReactionCI react2 : reactions.values()) {
					if (!verifiedReactions.contains(react2.getId())
							&& react.hasSameStoichiometry(react2, takeCareRev, ignoreCompartments)) {
						verifiedReactions.add(react2.getId());
						theSame.add(react2.getId());
					}
				}

				if (theSame.size() > 0)
					ret.put(react.getId(), theSame);
			}
		}
		return ret;
	}

	public Set<String> identiyReactionsWithInvalidStoiquiometry() {
		Set<String> invalidReactions = new TreeSet<String>();

		for (ReactionCI reaction : reactions.values()) {
			Map<String, StoichiometryValueCI> reactants = reaction.getReactants();
			Map<String, StoichiometryValueCI> products = reaction.getProducts();

			for (String metId : reactants.keySet()) {
				if (products.containsKey(metId)
						&& reactants.get(metId).getCompartmentId().equals(products.get(metId).getCompartmentId())) {
					invalidReactions.add(reaction.getId());
					break;
				}
			}
		}
		return invalidReactions;
	}

	public Set<String> identifyMetabolitesWithDrain() {
		hasUnicIdnt = identifyIfHasUniqueMetaboliteIds();
		Set<String> drains = identifyDrains();
		Map<String, StoichiometryValueCI> temp = new HashMap<String, StoichiometryValueCI>();

		for (String id : drains) {
			ReactionCI r = reactions.get(id);
			temp.putAll(r.getProducts());
			temp.putAll(r.getReactants());
		}

		Set<String> ret = new HashSet<String>();
		if (hasUnicIdnt)
			ret.addAll(temp.keySet());
		else {
			for (StoichiometryValueCI v : temp.values()) {
				String id = v.getMetaboliteId() + "_" + v.getCompartmentId();
				ret.add(id);
			}
		}
		return ret;
	}

	public String identifyReactionIdBySimilarStoichiometry(ReactionCI r, boolean rev_into_account) {
		String ret = null;
		for (ReactionCI rInt : reactions.values()) {
			if (r.hasSameStoichiometry(rInt, rev_into_account)) {
				ret = rInt.getId();
				break;
				// System.out.println(r.getId() + " = " + rInt.getId() + "\t"
				// +ContainerUtils.getReactionToString(this, r) + "\t" +
				// ContainerUtils.getReactionToString(this, rInt));
			}
		}
		return ret;
	}

	public Set<String> identifyDeadEnds(boolean useCompartments) {
		Set<String> metabGaps = new HashSet<String>();

		int dead = 0;
		if (!useCompartments) {
			for (MetaboliteCI metab : metabolites.values()) {
				Set<String> reactions = metab.getReactionsId();
				// stopFlag= -1 Nenhuma reacao encontrada que produza/consuma
				// metabolito
				int stopFlag = -1;
				for (String reactionId : reactions) {
					ReactionCI reaction = this.reactions.get(reactionId);

					stopFlag = nextFlag(stopFlag, reaction, null, metab.getId());

					if (stopFlag == 3)
						break;
				}

				if (stopFlag != 3) {
					System.out.println(metab.getName() + "\t" + stopFlag + "\t" + metab.getId());
					dead++;
					metabGaps.add(metab.getId());
				}
			}
		} else {
			for (CompartmentCI comp : compartments.values()) {

				for (String metId : comp.getMetabolitesInCompartmentID()) {
					MetaboliteCI metab = metabolites.get(metId);

					Set<String> reactions = metab.getReactionsId();

					// stopFlag= -1 Nenhuma reacao encontrada que
					// produza/consuma metabolito
					int stopFlag = -1;
					for (String reactionId : reactions) {
						ReactionCI reaction = this.reactions.get(reactionId);

						stopFlag = nextFlag(stopFlag, reaction, comp.getId(), metab.getId());

						if (stopFlag == 3)
							break;
					}

					if (stopFlag != 3) {
						System.out.println(metab.getName() + "\t" + stopFlag + "\t" + metab.getId() + "\t"
								+ comp.getId());
						dead++;
						metabGaps.add(metab.getId());
					}
				}
			}
		}
		System.out.println("Number of dead ends: " + dead);
		return metabGaps;
	}

	// FIXME: Change flag to something static (enum? or constant) in a diferent
	// class
	// significado da flag
	// -1 -> nenhuma reaction encontrada com o metabolito
	// 0 -> Reaction reversivel
	// 1 -> Reaction de producao do metabolito
	// 2 -> Reaction de consumo do metabolito
	// 3 -> Metabolito Balanceado

	private int nextFlag(int flagAnt, ReactionCI reaction, String comp, String metab) {
		int flag = 0;
		if (comp == null) {
			if (reaction.isReversible()) {
				if (flagAnt >= 0)
					flag = 3;
				else
					flag = 0;
			} else {
				if ((flagAnt == -1 || flagAnt == 1) && reaction.getProducts().containsKey(metab))
					flag = 1;
				else if ((flagAnt == -1 || flagAnt == 2) && reaction.getReactants().containsKey(metab))
					flag = 2;
				else
					flag = 3;
			}
		} else {
			boolean containsInProducts = reaction.containsMetaboliteInProducts(metab, comp);
			boolean containsInReactants = reaction.containsMetaboliteInReactants(metab, comp);

			if (containsInProducts == false && containsInReactants == false)
				flag = flagAnt;
			else {
				if (reaction.isReversible()) {
					if (flagAnt >= 0)
						flag = 3;
					else
						flag = 0;
				} else {
					if ((flagAnt == -1 || flagAnt == 1) && containsInProducts)
						flag = 1;
					else if ((flagAnt == -1 || flagAnt == 2) && containsInReactants)
						flag = 2;
					else
						flag = 3;
				}
			}
		}
		return flag;
	}

	public Set<String> identifyDrains() {
		Set<String> ret = new TreeSet<String>();
		for (ReactionCI reaction : reactions.values()) {
			int numProducts = reaction.getProducts().size();
			int numReactants = reaction.getReactants().size();

			if (numProducts == 0 || numReactants == 0) {
				reaction.setType(ReactionTypeEnum.Drain);
				ret.add(reaction.getId());
			}
		}
		return ret;
	}

	public Set<String> identifyTransportReactions() {
		Set<String> ret = new TreeSet<String>();
		for (ReactionCI reaction : reactions.values()) {
			if (reaction.identifyCompartments().size() > 1)
				ret.add(reaction.getId());
		}
		return ret;
	}

	public Set<String> identifyTransportReactions(String compartmentId) {
		Set<String> ret = new TreeSet<String>();
		for (ReactionCI reaction : reactions.values()) {

			List<String> compReact = getStoicCompartments(reaction.getReactants());
			List<String> compProd = getStoicCompartments(reaction.getProducts());

			if (compReact.contains(compartmentId) || compProd.contains(compartmentId))
				if (!(compReact.size() == 1 && compProd.size() == 1) || !compProd.get(0).equals(compReact.get(0))) {
					ret.add(reaction.getId());
				}
		}
		return ret;
	}

	public Set<String> identifyMetabolitesIdByPattern(Pattern pattern) {
		Set<String> met = new TreeSet<String>();

		for (String metId : metabolites.keySet()) {
			Matcher matcher = pattern.matcher(metId);
			if (matcher.matches())
				met.add(metId);
		}
		return met;
	}

	public Set<String> identifyReactionsIdByPatter(Pattern pattern) {
		Set<String> reacs = new TreeSet<String>();
		for (String r : reactions.keySet()) {
			Matcher matcher = pattern.matcher(r);
			if (matcher.matches())
				reacs.add(r);
		}
		return reacs;
	}

	public Set<String> identifyMetabolitesByNamePatter(Pattern pattern) {
		Set<String> met = new TreeSet<String>();
		for (String metId : metabolites.keySet()) {
			String name = metabolites.get(metId).getName();
			Matcher matcher = pattern.matcher(name);
			if (matcher.matches())
				met.add(metId);
		}
		return met;
	}

	public void verifyDepBetweenClass() throws IOException{
		verifyDepBetweenClass(true);
	}
	
	// VERIFY
	public void verifyDepBetweenClass(boolean throwerros) throws IOException {

		for (CompartmentCI comp : getCompartments().values())
			comp.setMetabolitesInCompartmentID(new HashSet<String>());

		for (MetaboliteCI met : getMetabolites().values())
			met.setReactionsId(new HashSet<String>());


		Set<String> reactionsToDelete = new HashSet<String>();
		for(ReactionCI rci: getReactions().values()){
			if(rci.getMetaboliteSetIds().size() == 0)
				reactionsToDelete.add(rci.getId());
		}
		
		getReactions().keySet().removeAll(reactionsToDelete);
		
//		for(GeneCI g : getGenes().values())
//			g.getReactionIds().clear();
		
		Set<String> genesToRemove = new HashSet<String>();
		genesToRemove.addAll(genes.keySet());

		for (String rid : this.getReactions().keySet()) {
			ReactionCI reaction = this.getReactions().get(rid);
			verifyStoiDep(reaction.getProducts(), rid, throwerros);
			verifyStoiDep(reaction.getReactants(), rid, throwerros);

			for (String g : reaction.getGenesIDs()) {
				if (genesToRemove.contains(g))
					genesToRemove.remove(g);

				GeneCI gene = genes.get(g);
				if (gene == null) {
					gene = new GeneCI(g, "");
					genes.put(g, gene);
				}
				gene.addReactionId(rid);
			}
		}

		for (String g : genesToRemove) {
			genes.remove(g);
		}

		Set<String> metaboliteToRemove = new HashSet<String>();
		for (MetaboliteCI met : metabolites.values())
			if (met.getReactionsId().size() == 0) {
				metaboliteToRemove.add(met.getId());
			}

		if (metaboliteToRemove.size() > 0)
			System.out.println("Metabolites To remove: " + metaboliteToRemove.size() + "/" + metabolites.size());

		for (String id : metaboliteToRemove)
			_removeMetabolite(id);
		
		clearInfoElements();
	}

	private void verifyStoiDep(Map<String, StoichiometryValueCI> stoi, String reactionId, boolean throwProblem) throws IOException {
		for (StoichiometryValueCI val : stoi.values()) {
			String metaboliteId = val.getMetaboliteId();
			String compartmentId = val.getCompartmentId();
			MetaboliteCI m = metabolites.get(metaboliteId);
			CompartmentCI c = compartments.get(compartmentId);
			
			if(throwProblem){
				if (m == null)
					throw new IOException("Metabolite " + metaboliteId + " present in reaction " + reactionId
							+ " was not declared");
				if (c == null)
					throw new IOException("Compartment " + compartmentId + " present in reaction " + reactionId
							+ "was not declared");
			}
			if(m!=null)m.getReactionsId().add(reactionId);
			if(c!=null)c.getMetabolitesInCompartmentID().add(metaboliteId);
		}

	}

	public boolean identifyIfHasUniqueMetaboliteIds(){
		
		if(compartments.size() == 1)
			return true;
		
		for(String metId : metabolites.keySet()){
			
			int countComp = 0;
			for(CompartmentCI comp : compartments.values()){
				if(comp.getMetabolitesInCompartmentID().contains(metId)) countComp++; 
				
			}
			
			if(countComp >1 ){
				return false;
			}
		}
		
		return true;
	}

	// PRIVATE METHODS
	private List<String> getStoicCompartments(Map<String, StoichiometryValueCI> stoich) {
		ArrayList<String> ret = new ArrayList<String>();

		for (StoichiometryValueCI value : stoich.values()) {
			if (!ret.contains(value.getCompartmentId()))
				ret.add(value.getCompartmentId());
		}
		return ret;

	}

	private void removeMetInStoic(Map<String, StoichiometryValueCI> stoic, Set<String> remMet, String compToRemove) {
		List<String> toRemove = new ArrayList<String>();
		for (String metabolite : stoic.keySet()) {
			String comp = stoic.get(metabolite).getCompartmentId();

			if (comp.equals(compToRemove) && remMet.contains(metabolite))
				toRemove.add(metabolite);
		}

		for (String met : toRemove)
			stoic.remove(met);
	}

	private void removeMetaboliteFromAllCompartments(String metId) {
		for (CompartmentCI comp : compartments.values()) {
			comp.getMetabolitesInCompartmentID().remove(metId);
		}
	}

	protected void _removeMetabolite(String id) {
		metabolites.remove(id);
		_removeFromExtraInfo(id, metabolitesExtraInfo);

	}

	private void _removeFromMetaboliteExtraInfo(String metId) {
		if (metabolitesExtraInfo != null)
			for (Map<String, String> extraInfo : metabolitesExtraInfo.values()) {
				extraInfo.remove(metId);
			}
	}

	private ReactionCI _removeReaction(String id) {
		ReactionCI removed = reactions.remove(id);
		defaultEC.remove(id);
//		boolean ifIsDrain = getDrains().remove(id);
		_removeFromExtraInfo(id, reactionsExtraInfo);

//		if (ifIsDrain && drainToMetabolite != null) {
//			String met = drainToMetabolite.remove(id);
//			metaboliteToDrain.remove(met);
//		}
		return removed;
	}

	private void _removeFromExtraInfo(String id, Map<String, Map<String, String>> extraInfoMap) {
		for (Map<String, String> extraInfo : extraInfoMap.values()) {
			extraInfo.remove(id);
		}

	}

	private void _defineDrainReactions() {
		Set<String> drains = identifyDrains();
		defineReactionsType(drains, ReactionTypeEnum.Drain);

	}

	private void _defineTransportReactions() {
		Set<String> transport = identifyTransportReactions();
		defineReactionsType(transport, ReactionTypeEnum.Transport);
	}

	public void defineReactionsType(Set<String> reactionIds, ReactionTypeEnum type) {

		for (String reactionId : reactionIds) {
			ReactionCI reaction = reactions.get(reactionId);
			reaction.setType(type);
		}
	}
   
	//
	public boolean isAppendable(ReactionCI r){

		Set<String> allmets = new HashSet<String>(r.getReactants().keySet());
		allmets.addAll(r.getProducts().keySet());
		
		boolean ret = metabolites.keySet().containsAll(allmets);
		
		if(ret){
			Set<String> comps = r.identifyCompartments();
			ret = compartments.keySet().containsAll(comps);
		}
		
		return ret;
	}
	
	public Set<String> getReactionsIdsComsomingAndProducingSameMet(){
		
		Set<String> ret = new HashSet<String>();
		
		for(ReactionCI r : reactions.values()){
			boolean b = r.validStoiquiometryReaction();
			if(!b) ret.add(r.getId());
		}
		
		return ret;
	}
	
	
	// GETS / SETS
	public String getExternalCompartmentId() {

		if (ext_compartment == null) {
			CompartmentCI comp = getExternalCompartment();
			if (comp != null)
				ext_compartment = comp.getId();
		}
		return ext_compartment;
	}

	public Set<String> getAllEc_Numbers() {
		Set<String> ret = new TreeSet<String>();
		for (ReactionCI reac : reactions.values()) {
			String ec_number = reac.getEc_number();
			if (ec_number != null)
				ret.add(ec_number);
		}
		return ret;
	}

	public Set<String> getAllEcNumbers(String sepExp) {
		int i = 0;
		Set<String> ret = new TreeSet<String>();
		for (ReactionCI reac : reactions.values()) {
			String ec_number = reac.getEcNumber();
			if (ec_number != null && ec_number.compareTo("") != 0 && ec_number.compareTo("null") != 0)
				for (String ec : ec_number.split(sepExp)) {
					if (ret.add(ec.trim()))
						i++;
				}
		}
		return ret;
	}

	public Set<String> getAllSubsystems(String sepExp) {
		Set<String> res = new TreeSet<String>();
		for (ReactionCI reac : reactions.values()) {
			String subsystem = reac.getSubsystem();
			if (subsystem != null)
				res.addAll(Arrays.asList(subsystem.split(sepExp)));
		}
		return res;
	}

	public Set<String> getInternalReactions() {
		Set<String> ret = new TreeSet<String>();
		for (ReactionCI reaction : reactions.values()) {
			if (reaction.getType().equals(ReactionTypeEnum.Internal)
					|| reaction.getType().equals(ReactionTypeEnum.Undefined)) {
				ret.add(reaction.getId());
			}
		}
		return ret;
	}

	public CompartmentCI getCompartment(String compartmentId) {
		return compartments.get(compartmentId);
	}

	public CompartmentCI getExternalCompartment() {
		CompartmentCI ret = compartments.get(ext_compartment);

		return ret;
	}

	public GeneCI getGene(String geneId) {
		return genes.get(geneId);
	}

	public String getModelName() {
		return name;
	}

	public void setModelName(String name) {
		this.name = name;
	}

	public String getOrganismName() {
		return organism;
	}

	public void setOrganism(String organism) {
		this.organism = organism;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Map<String, ReactionCI> getReactions() {
		return reactions;
	}

	public Map<String, ReactionCI> getReactionsNotDrains() {
		Map<String, ReactionCI> notDrains = new HashMap<String, ReactionCI>();

		for (String s : reactions.keySet()) {
			if (!reactions.get(s).getType().equals(ReactionTypeEnum.Drain))
				notDrains.put(s, reactions.get(s));
		}
		return notDrains;
	}

	public void setReactions(Map<String, ReactionCI> reactions) {
		this.reactions = reactions;
	}

	public MetaboliteCI getMetabolite(String id) {
		return metabolites.get(id);
	}

	public ReactionCI getReaction(String id) {
		return reactions.get(id);
	}

	public Map<String, MetaboliteCI> getMetabolites() {
		return metabolites;
	}

	public void setMetabolites(Map<String, MetaboliteCI> metabolites) {
		this.metabolites = metabolites;
	}

	public Map<String, CompartmentCI> getCompartments() {
		return compartments;
	}

	public void setCompartments(Map<String, CompartmentCI> compartments) {
		this.compartments = compartments;
	}

	public String getBiomassId() {
		if (biomassId == null)
			biomassId = getBiomassFluxFromSizeHeuristic();
		return biomassId;
	}

	public void setBiomassId(String biomassId) {
		this.biomassId = biomassId;
		reactions.get(biomassId).setType(ReactionTypeEnum.Biomass);
	}

	public Map<String, Map<String, String>> getMetabolitesExtraInfo() {
		return metabolitesExtraInfo;
	}

	public void setMetabolitesExtraInfo(Map<String, Map<String, String>> metabolitesExtraInfo) {
		this.metabolitesExtraInfo = metabolitesExtraInfo;
	}

	public Map<String, Map<String, String>> getReactionsExtraInfo() {
		return reactionsExtraInfo;
	}

	public void setReactionsExtraInfo(Map<String, Map<String, String>> reactionsExtraInfo) {
		this.reactionsExtraInfo = reactionsExtraInfo;
	}

	public Map<String, GeneCI> getGenes() {
		return genes;
	}

	public void setGenes(Map<String, GeneCI> genes) {
		this.genes = genes;
	}

	public Set<String> getDrains() {
		if (drains == null)
			drains = identifyDrains();
		return drains;
	}

	public Map<String, ReactionConstraintCI> getDefaultEC() {
		return defaultEC;
	}

	public void setDefaultEC(Map<String, ReactionConstraintCI> defaultEC) {
		this.defaultEC = defaultEC;
	}

	public void changeMetaboliteId(String oldMetId, String metId) throws Exception{
		changeMetaboliteId(oldMetId, metId, false);
	}
	
	public void changeMetaboliteId(String oldMetId, String metId, boolean agregateMet) throws Exception {

		if (metabolites.containsKey(oldMetId)) {

			Set<String> reactionsIds = metabolites.get(oldMetId).getReactionsId();

			if (!agregateMet && metabolites.containsKey(metId))
				throw new Exception(" New metabolite id " + metId + " already exists");

			MetaboliteCI met = metabolites.get(oldMetId);

			for (String reactionsId : reactionsIds) {

				ReactionCI reaction = reactions.get(reactionsId);

				if (reaction.getProducts().containsKey(oldMetId)) {
					StoichiometryValueCI value = reaction.getProducts().get(oldMetId);
					reaction.getProducts().remove(oldMetId);
					value.setMetaboliteId(metId);
					reaction.getProducts().put(metId, value);
					compartments.get(value.getCompartmentId()).getMetabolitesInCompartmentID().add(metId);
				}

				if (reaction.getReactants().containsKey(oldMetId)) {
					StoichiometryValueCI value = reaction.getReactants().get(oldMetId);
					reaction.getReactants().remove(oldMetId);
					value.setMetaboliteId(metId);
					reaction.getReactants().put(metId, value);
					compartments.get(value.getCompartmentId()).getMetabolitesInCompartmentID().add(metId);
				}

			}

			met.setId(metId);
			removeMetaboliteFromAllCompartments(oldMetId);
			metabolites.remove(oldMetId);
			metabolites.put(metId, met);
			changeIdInExtraInfo(oldMetId, metId, metabolitesExtraInfo);
		
		}
	}

	
	
	// duplicate method identifyReactionIdByPatter
	@Deprecated
	public Set<String> identifyReactionIdByPatter(Pattern pattern) {
		Set<String> met = new TreeSet<String>();

		for (String metId : reactions.keySet()) {
			Matcher matcher = pattern.matcher(metId);

			if (matcher.matches())
				met.add(metId);
		}

		return met;
	}

	
	public Map<String, Set<String>> getECNumbers(){
		
		Map<String, Set<String>> ret = new HashMap<String, Set<String>>();
		
		for(ReactionCI reac: reactions.values()){
			
			String ec_number = reac.getEc_number();			
			if(ec_number!=null){
				ec_number = ec_number.trim();
				if(!ec_number.equals("")){
					
					Set<String> reactions = ret.get(ec_number);
					if(reactions == null)
						reactions = new TreeSet<String>(); 
					reactions.add(reac.getId());
					ret.put(ec_number, reactions);
				}
			}
		}
		return ret;
	}
	
	public Set<String> checkReactions(ReactionCI reaction, boolean rev, boolean comp){
		
		Set<String> set = new HashSet<>();
		for(ReactionCI containerReaction : reactions.values()) {

			boolean sameReaction = containerReaction.hasSameStoichiometry(reaction, rev, comp);
			if(sameReaction) set.add(containerReaction.getId());
		}
		return set;
	}
	
	public void addReaction(ReactionCI reaction, boolean verify) throws ReactionAlreadyExistsException, IOException {
		String reactionId = reaction.getId();
		if(reactions.containsKey(reactionId))
			throw new ReactionAlreadyExistsException(reactionId);
		
		else {
			for(ReactionCI containerReaction : reactions.values()) {

				boolean sameReaction = containerReaction.hasSameStoichiometry(reaction, true, false);
				if(sameReaction)
				{
					System.out.println("$$$$$$$$$ " + containerReaction.getId() + " # " + containerReaction.getName() + " # " + containerReaction.toStringStoiquiometry());
					throw new ReactionAlreadyExistsException(containerReaction.getId());
				}
			}
			reactions.put(reactionId, reaction);
		}
		if(verify)
			verifyDepBetweenClass();
	}
	
	public void addReaction(ReactionCI reaction) throws ReactionAlreadyExistsException, IOException {

		addReaction(reaction, true);
	}
	
	public ReactionCI addReactionReturnSame(ReactionCI reaction) throws ReactionAlreadyExistsException, IOException {

		
		ReactionCI same = null;
		String reactionId = reaction.getId();
		if(reactions.containsKey(reactionId))
			throw new ReactionAlreadyExistsException(reactionId);
		
		else {
			for(ReactionCI containerReaction : reactions.values()) {

				boolean sameReaction = containerReaction.hasSameStoichiometry(reaction, true, false);
				if(sameReaction)
				{
//					System.out.println("$$$$$$$$$ " + containerReaction.getId() + " # " + containerReaction.getName() + " # " + containerReaction.toStringStoiquiometry());
//					throw new ReactionAlreadyExistsException(containerReaction.getId());
					same = containerReaction;
					return same;
				}
			}
			reactions.put(reactionId, reaction);
		}
		verifyDepBetweenClass();
		
		return same;
	}
	
	public double getMetaboliteStoichiometry(String reactionId, String metaboliteId) throws EntityDoesNotExistsException{
		validateIfExists(metaboliteId, metabolites, ExceptionProperties.METABOLITE_TYPE);
		ReactionCI r = validateIfExists(reactionId, reactions, ExceptionProperties.REACTION_TYPE);
		
		StoichiometryValueCI sValue = r.getReactants().get(metaboliteId);
		if(sValue==null) sValue = r.getProducts().get(metaboliteId);
		if(sValue==null) throw new StoichiometryDoesNotExistsException(reactionId, metaboliteId);
		
		return sValue.getStoichiometryValue();
	}

	
	
	private void validateIfReactionIsPresent(String id) throws ReactionDoesNotExistsException{
		try {
			validateIfExists(id, getReactions(), ExceptionProperties.REACTION_TYPE);
		} catch (EntityDoesNotExistsException e) {
			throw (ReactionDoesNotExistsException)e;
		}
	}
	
	private void validateIfMetaboliteIsPresent(String id) throws MetaboliteDoesNotExistsException{
		try {
			validateIfExists(id, getMetabolites(), ExceptionProperties.METABOLITE_TYPE);
		} catch (EntityDoesNotExistsException e) {
			throw (MetaboliteDoesNotExistsException) e;
		}
	}
	
	private <T extends Object> T validateIfExists(String id, Map<String, T> values, String type) throws EntityDoesNotExistsException{
		
		T value = null;
		
		try { value = values.get(id); } catch (Exception e) {}
		
		if(value == null){
			EntityDoesNotExistsException dne = null;
			switch (type) {
			case ExceptionProperties.METABOLITE_TYPE:
				dne = new MetaboliteDoesNotExistsException(id);
				break;
			case ExceptionProperties.REACTION_TYPE:
				dne = new ReactionDoesNotExistsException(id);
			default:
				dne = new EntityDoesNotExistsException(id, type);
				break;
			}
			throw dne;
		}
		
		return value;
	}

	/**
	 * 
	 * Search reaction from a regular expression 
	 * @param pattern
	 * @return reaction ids
	 */
	public Set<String> searchReactionById(Pattern pattern) {
		Set<String> set = new TreeSet<String>();
		for(String id : getReactions().keySet()){
			Matcher m = pattern.matcher(id);
			if(m.matches()){
				set.add(id);
			}
		}
		return set;
	}
	
	
	/**
	 * Changes reaction bounds
	 * @param rId reaction id
	 * @param lb  reaction lower bound
	 * @param ub  reaction upper bound
	 * @throws ReactionDoesNotExistsException 
	 */
	public void changeReactionBound(String rId, double lb, double ub) throws ReactionDoesNotExistsException{
		validateIfReactionIsPresent(rId);
		getDefaultEC().put(rId, new ReactionConstraintCI(lb,ub));
	}
	
	
	public Set<String> getNotDeclaredMetabolites(){
		Set<String> reactionMetabolites = new HashSet<String>();
		for(ReactionCI r : this.getReactions().values())
			reactionMetabolites.addAll(r.getMetaboliteSetIds());
		
		Set<String> ret = CollectionUtils.getSetDiferenceValues(reactionMetabolites, this.getMetabolites().keySet());
		return ret;
	}
	
	public Set<String> getReactionsWithSameMetaboliteInProductsAndReactants(){
		Set<String> wrongReactions = new HashSet<String>();
		for(ReactionCI r : this.getReactions().values()){
			
			if(!r.validStoiquiometryReaction())
				wrongReactions.add(r.getId());
		}
		
		return wrongReactions;
	}
	
	public Map<String, String> stripInfoReactionIds(Pattern p){
		return stripInfoReactionIds(p, null);
	}
	
	public Map<String, String> stripInfoReactionIds(Pattern p, Collection<String> reactionsToTest){
		reactionsToTest = (reactionsToTest==null)?getReactions().keySet():reactionsToTest;
		
		Map<String, String> reactionChanged = new HashMap<>();
		for(String id : reactionsToTest){
			Matcher m = p.matcher(id);
			if(m.matches()){
				String newId = m.group(1).trim();
				
				if(newId.equals("")) throw new RuntimeException("Empty id is not valid!! oldId = " + id );
				if(getReactions().containsKey(newId)) throw new RuntimeException("The id " + newId + " generated for "+id+" already exists!! ");
				if(reactionChanged.containsKey(newId)) throw new RuntimeException("The id " + newId + "generated from id " +id+ ", was already generated from id " + reactionChanged.get(newId));
				
				reactionChanged.put(newId, id);
			}
		}
		
		try {
			changeReactionIds(MapUtils.revertMapSingleValues(reactionChanged));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return reactionChanged;
	}
	
	public Collection<String> getAllGenesByReactionList(Collection<String> reactionsList){
		Set<String> toRet = new HashSet<String>();
		
		for (String r : reactionsList) {
			ReactionCI reaction = getReaction(r);
			toRet.addAll(reaction.getGenesIDs());
		}
		return toRet;
	}
	
	public Map<String, Double> getGenesReactionsCountMap(Collection<String> genesList){
		HashMap<String, Double> toRet = new HashMap<String, Double>();
		
		for (String geneID : genesList) {
			GeneCI gene = getGene(geneID);
			if(gene == null){
				throw new NullPointerException("Unknown Gene ID: " + geneID);
			}
			toRet.put(geneID, gene.getReactionIds().size()+0.0);
		}
		return toRet;
	}
	
	public Map<String, Double> getGenesReactionsCountMap(){
		return getGenesReactionsCountMap(getGenes().keySet());
	}
	
	public Map<String, Double> getGenesReactionsCountIntersectionMap(Collection<String> reactionList, Collection<String> genesList){
		HashMap<String, Double> toRet = new HashMap<String, Double>();
		
		for (String geneID : genesList) {
			GeneCI gene = getGene(geneID);
			if(gene == null){
				throw new NullPointerException("Unknown Gene ID: " + geneID);
			}
			Set<String> reactionGeneAssoc = new HashSet<String>(gene.getReactionIds());
			Set<String> reactionGeneAssocIntersection = new HashSet<String>();
			for (String reactID : reactionGeneAssoc) {
				if(reactionList.contains(reactID)){
					reactionGeneAssocIntersection.add(reactID);
				}
			}
			
			toRet.put(geneID, reactionGeneAssocIntersection.size()+0.0);
		}
		return toRet;
	}
	
	public Map<String, Double> getGenesReactionsCountIntersectionMap(Collection<String> reactionList){
		return getGenesReactionsCountIntersectionMap(reactionList, getGenes().keySet());
	}
	
//	private ReactionCI validateIfExistsReaction(String reactionId) {
//		ReactionCI r = reactions.get(reactionId);
//		if(r == null) throw new ReactionDoesNotExistsException(reactionId);
//		return r;
//	}
//
//	private MetaboliteCI validateIfExistsMetabolite(String metaboliteId) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	// private void updateCrossLinkage(){
	//
	// for(CompartmentCI comp : compartments.values()){
	// comp.setMetabolitesInCompartmentID(new TreeSet<String>());
	// }
	//
	// for(MetaboliteCI met : metabolites.values()){
	// met.setReactionsId(new TreeSet<String>());
	// }
	//
	// for(ReactionCI reaction : reactions.values()){
	// updateCrossLinkage(reaction.getProducts(), reaction.getId());
	// updateCrossLinkage(reaction.getReactants(), reaction.getId());
	// }
	// }
	// private void updateCrossLinkage(Map<String, StoichiometryValueCI> stoic,
	// String reactionId){
	//
	// for(StoichiometryValueCI value : stoic.values()){
	// String compId = value.getCompartmentId();
	// String metId = value.getMetaboliteId();
	//
	// metabolites.get(metId).addReaction(reactionId);
	// compartments.get(compId).addMetaboliteInCompartment(metId);
	// }
	// }
	/*
	 * I can't get a pattern from RegexpPair Doing local function to solve the
	 * problems atm
	 */
	// public ArrayList<String> getExternalSpeciesByRegularExpressions(
	// RegExpPair... regExps) throws SecurityException,
	// IllegalArgumentException, NoSuchMethodException, IllegalAccessException,
	// InvocationTargetException {
	//
	// boolean useReactionNames = true;
	// ArrayList<String> toRet = new ArrayList<String>();
	// for(MetaboliteCI species: this.getMetabolites().values()){
	//
	// boolean match = false;
	// String speciesID = species.getId();
	// String speciesName = null;
	// if(useReactionNames)
	// speciesName = species.getName();
	//
	// //implements a logic OR
	// for(RegExpPair regExp: regExps){
	// if(regExp.evaluate(speciesID)){
	// match=true;
	// break;
	// }
	// if(useReactionNames)
	// if(regExp.evaluate(speciesName)){
	// match=true;
	// break;
	// }
	// // System.out.println(match);
	// }
	//
	// if(match)
	// toRet.add(speciesID);
	// }
	//
	// if(toRet.size()==0)
	// toRet = null;
	//
	// return toRet;
	// }
	//
	// public Set<String> identifyMetabolitesPresentInComp1AndNotComp2(String
	// compartmentId1, String compartmentId2) throws Exception{
	// if(!compartments.containsKey(compartmentId1) ||
	// !compartments.containsKey(compartmentId2))
	// throw new Exception("Compartment does not exist in container");
	//
	// Set<String> metInC1AndNotC2 = (Set<String>)
	// CollectionUtils.getSetDiferenceValues(compartments.get(compartmentId1).getMetabolitesInCompartmentID(),
	// compartments.get(compartmentId2).getMetabolitesInCompartmentID());
	//
	// return metInC1AndNotC2;
	// }
	//
	//
	// public Set<String> identifyCommonMetabolites(String compartmentId1,
	// String compartmentId2) throws Exception{
	// if(!compartments.containsKey(compartmentId1) ||
	// !compartments.containsKey(compartmentId2))
	// throw new Exception("Compartment does not exist in container");
	//
	// Set<String> met = (Set<String>)
	// CollectionUtils.getIntersectionValues(compartments.get(compartmentId1).getMetabolitesInCompartmentID(),
	// compartments.get(compartmentId2).getMetabolitesInCompartmentID());
	//
	// return met;
	// }
//	private boolean isTheSameReaction(ReactionCI reaction1, ReactionCI reaction2){
//	boolean continueFlag = false;
//	
//	if(reaction1.getReversible().equals(reaction2.getReversible()))
//		continueFlag = true;
//	
//	if(continueFlag){
//		
//	}
//	
//	return continueFlag;
//}
//
//private boolean isTheSameStoik(Map<String, StoichiometryValueCI> stoic1, Map<String, StoichiometryValueCI> stoic2){
//	
//	boolean isTheSame = true;
//		
//	if(stoic1.size() == stoic2.size()){
//		
//		for(String met : stoic1.keySet()){
//			if(!(stoic2.containsValue(met) && stoic1.get(met).equals(stoic2.get(met)))){
//				isTheSame = false;
//				break;
//			}
//			
//		}
//	}
//	
//	return isTheSame;
//}
//	public void normalizeCompartmentIds() {
//	
//	for(CompartmentCI c : this.getCompartments().values()){
//		
//		normalizeCompartment(c);
//	}
//}
//
//public void normalizeCompartment(CompartmentCI c){
//	System.out.println(c.getId() + c.getMetabolitesInCompartmentID());
//}

}
