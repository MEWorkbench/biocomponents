package pt.uminho.ceb.biosystems.mew.biocomponents.container;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.InvalidBooleanRuleException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.BalanceValidator;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public class ContainerUtils {

	public static double DEFAULT_BOUND_VALUE = 100000;
	public static String METABOLITE_SOURCE_UNDEF = "UNDEFINED";

	public static Map<String, String> getAllMetabolitesFormula(Container cont){

		Map<String, String> ret = new HashMap<String, String>();
		for(MetaboliteCI m : cont.getMetabolites().values()){

			String id = m.getId();
			String f = m.getFormula();
			ret.put(id, f);
		}
		return ret;
	}

	
	public static String getReactionToString(Container cont, String rid){
		return getReactionToString(cont, rid, false);
	}
	
	public static String getReactionToString(Container cont, String rid, boolean useNames){
		String ret = "";

		ReactionCI r = cont.getReaction(rid);

		ret += printStoiqMap(cont, r.getReactants(), useNames);
		if(r.isReversible())
			ret+=" <<=>> ";
		else
			ret+=" ===>> ";

		ret += printStoiqMap(cont, r.getProducts(), useNames);
		return ret;
	}

	public static String getReactionToString(Container cont, ReactionCI r){
		String ret = "";

		//		ReactionCI r = cont.getReaction(rid);

		ret += printStoiqMap(cont, r.getReactants(), false);
		if(r.isReversible())
			ret+=" <<=>> ";
		else
			ret+=" ===>> ";

		ret += printStoiqMap(cont, r.getProducts(), false);
		return ret;
	}

	public static String getReactionToString(ReactionCI r){
		return getReactionToString(r, false);
	}
	
	public static String getReactionToString(ReactionCI r, boolean unicMetIds){
		String ret = "";

		//		ReactionCI r = cont.getReaction(rid);

		ret += printStoiqMap(r.getReactants(), unicMetIds);
		if(r.isReversible())
			ret+=" <<=>> ";
		else
			ret+=" ===>> ";

		ret += printStoiqMap( r.getProducts(), unicMetIds);
		return ret;
	}

	private static String printStoiqMap(Container cont,
			Map<String, StoichiometryValueCI> reactants, boolean useNames) {

		String ret = "";

		for(String metId : reactants.keySet()){
			Double value = reactants.get(metId).getStoichiometryValue();

			ret += " "+ value + " " + metId +" \""+cont.getMetabolite(metId).getName()+"\" ("+reactants.get(metId).getCompartmentId()+")" +" +";
		}
		if(!ret.equals(""))
			ret = ret.substring(1, ret.length()-1);

		return ret;
	}

	private static String printStoiqMap(
			Map<String, StoichiometryValueCI> reactants, boolean isUnicIds) {

		String ret = "";

		for(String metId : reactants.keySet()){
			Double value = reactants.get(metId).getStoichiometryValue();

			ret += " "+ value + " " + metId + ((isUnicIds)?"":"("+reactants.get(metId).getCompartmentId()+")") + " +";
		}
		if(!ret.equals(""))
			ret = ret.substring(1, ret.length()-1);

		return ret;
	}

	static public String getMetaboliteToString(MetaboliteCI met){

		String ret="ID:         "+ met.getId()+"\n";
		ret      +="Name:       "+ met.getName()+"\n";
		ret      +="Formula:    "+ met.getFormula()+"\n";
		ret      +="Charge:     "+ met.getCharge()+"\n";
		ret      +="Smiles:     "+ met.getSmiles()+"\n";
		ret      +="Inchy Key:  "+ met.getInchikey()+"\n";
		if(met.getSymnonyms() !=null)ret      +="Synonyms:   "+ met.getSymnonyms().toString()+"\n";

		return ret;
	}

	public static Container readSBML(String path, String name, boolean checkConsistency) throws Exception {
		//FIXME: Check this later.
		JSBMLReader reader = new JSBMLReader(path, name, checkConsistency, 0.0, 10000.0);
		Container container = new Container(reader);
		return container;
	}

	public static Map<String, Double> balanceOfReactions(Set<String> reactionsIds, Container c, boolean addComp, Map<String, Double> values, Double norm){

		Map<String, Double> balance = new HashMap<String, Double>();

		for(String id : reactionsIds){

			ReactionCI r = c.getReaction(id);
			Double d = values.get(id);
			if(d == null) d = 1d;
			d = d/norm;
			Map<String,StoichiometryValueCI> produts = r.getProducts();
			Map<String,StoichiometryValueCI> reactants = r.getReactants(); 

			balance(balance,produts,1.0 * d, addComp);
			balance(balance,reactants,-1.0 * d, addComp);
		}
		return balance;
	}
	
	public static ReactionCI joinReactions(String id, String name, Set<String> reactionsIds, Container c) throws Exception{
		return joinReactions(id, name, reactionsIds, c, null, null);
	}
	
	public static ReactionCI joinReactions(String id, String name, Set<String> reactionsIds, Container c, Map<String , Double>  values, Double norm) throws Exception{
		
		if(values == null) values = new HashMap<>();
		if(norm == null) norm = 1d;
		Map<String, Double> t = balanceOfReactions(reactionsIds, c, true, values, norm);
		
		
		Pattern p = Pattern.compile("(.+)\\|(.+)\\|");
		boolean rev = true;
		for(String rId: reactionsIds)
			rev = rev && c.getReaction(rId).isReversible();
		
		Map<String, StoichiometryValueCI> products = new TreeMap<String, StoichiometryValueCI>();
		Map<String, StoichiometryValueCI> reactants = new TreeMap<String, StoichiometryValueCI>();
		
		for(String key: t.keySet()){
			
			Matcher m = p.matcher(key);
			
			if(m.matches()){
				String metId = m.group(1);
				String comp = m.group(2);
				Double s = t.get(key);
				
				if(s<0) reactants.put(metId, new StoichiometryValueCI(metId, -s, comp));
				else if(s>0) products.put(metId, new StoichiometryValueCI(metId, s, comp));
			}else{
				throw new Exception("Key: " + key + " does not match with pattern");
			}
		}
		
		
		ReactionCI r = new ReactionCI(id, name, rev, reactants, products);
		
//		if(reactionsIds.contains(c.getBiomassId())) r.set
		return r;
	}

	private static void balance(Map<String, Double> data, Map<String,StoichiometryValueCI> stoic, Double p, boolean addComp){

		for(StoichiometryValueCI v : stoic.values()){
			String m = (addComp)? v.getMetaboliteId() + "|"+ v.getCompartmentId()+ "|": v.getMetaboliteId();
			Double s = v.getStoichiometryValue();

			Double sum = data.get(m);
			sum=(sum==null)?p*s:sum+(p*s);

			data.put(m, sum);
		}

	}

	static public BalanceValidator balanceModelInH(Container cont, String protonId){

		BalanceValidator bv = new BalanceValidator(cont);
		runBalidationByDefault(bv,protonId);

		return bv;
	}

	static protected void runBalidationByDefault(BalanceValidator bv, String protonId){

		System.out.println("Set Formulas");
		bv.setFormulasFromContainer();
		System.out.println("validateAllReactions");
		bv.validateAllReactions();
		System.out.println("balanceH");
		bv.balanceH(protonId);

	}

	static public String toStringBalanceInfo(BalanceValidator bv, Collection<String> reactions, Collection<String> reactioTags, Map<String, String> metaboliteSource) throws Exception{

		String ret = "";
		Container cont = bv.getBalancedContainer();
		Map<String, String> formulas = getFormulas(cont);

		for(String reactionId : reactions){

			if(!cont.getDrains().contains(reactionId)){
				ReactionCI r = cont.getReaction(reactionId);
				Set<String> mets = r.getMetaboliteSetIds();

				Set<String> metTags = new TreeSet<String>();
				for(String id : mets){
					
					String mT = (metaboliteSource != null)?metaboliteSource.get(id):METABOLITE_SOURCE_UNDEF;
					mT = (mT == null)?METABOLITE_SOURCE_UNDEF:mT;
					metTags.add(mT);
				}

				String reactionTag = bv.getReactionTags().get(reactionId);

				if(reactioTags.contains(reactionTag));
				ret+= (reactionId + "\t"+pt.uminho.ceb.biosystems.mew.biocomponents.container.ContainerUtils.getReactionToString(r)+"\t"+ printStoiqInfo(r.getReactants(), formulas, metaboliteSource, bv.getCharges()) + "\t"+
						printStoiqInfo(r.getProducts(), formulas, metaboliteSource, bv.getCharges()) +"\t" + bv.getSumOfReactantsToString(reactionId) + "\t"+
						bv.getSumOfProductsToString(reactionId)+"\t"+ bv.getDifResultToString(reactionId)+"\t"+
						bv.getChargeR(reactionId) + "\t" + bv.getChargeP(reactionId) +"\t"+ bv.getChargeDiff(reactionId)
						+"\t" + metTags + "\t"+reactionTag)+"\n";
			}

		}
		return ret;
	}

	static private String printStoiqInfo(Map<String, StoichiometryValueCI> reactants,
			Map<String, String> formulas, Map<String, String> source,
			Map<String, Integer> charges) {

		String ret ="";

		for(StoichiometryValueCI v : reactants.values()){
			String id = v.getMetaboliteId();
			ret += "(" + v.getStoichiometryValue()+ ", "+v.getMetaboliteId() + ", " + formulas.get(id) +", " + charges.get(id) + ", " + source.get(id)+") ";
		}
		return ret;
	}


	private static Map<String, String> getFormulas(Container cont) {

		Map<String, String> ret = new HashMap<String, String>();

		for(MetaboliteCI met : cont.getMetabolites().values())
			ret.put(met.getId(), met.getFormula());

		return ret;
	}


	static public Set<String> remomeMetaboliteDeadEnds(Container cont, boolean useComp){

		Set<String> dead = cont.identifyDeadEnds(useComp);
		cont.removeMetaboliteAndItsReactions(dead);

		return dead; 
	}

	static public Set<String> removeDeadEndsIteratively(Container cont, boolean useComp){
		Set<String> allDeadEnds = new HashSet<String>();

		Set<String> aux = remomeMetaboliteDeadEnds(cont, useComp);
		while(aux.size()>0){
			allDeadEnds.addAll(aux);
			aux = remomeMetaboliteDeadEnds(cont, useComp);
		}

		return allDeadEnds;
	}
	
	
	static public Set<String> identyfyReactionWithDeadEnds(Container c/*, boolean useComp*/){
		
		Set<String> met = removeDeadEndsIteratively(c.clone(), /*useComp*/ false);
		
		Set<String> reactions = new HashSet<String>();
		
		for(String m : met){
			reactions.addAll(c.getMetabolite(m).getReactionsId());
		}
		
		return reactions;
	}
	
	static public Set<String> identyfyReactionWithDeadEndsNotIt(Container c/*, boolean useComp*/){
		
		Set<String> met = c.identifyDeadEnds(false);
		
		Set<String> reactions = new HashSet<String>();
		
		for(String m : met){
			reactions.addAll(c.getMetabolite(m).getReactionsId());
		}
		
		return reactions;
	}

	/**
	 * Retrieve metabolites withou kegg ids from container
	 * 
	 * @param reactionID
	 * @param container
	 * @return
	 */
	public static Set<String> getMetabolitesWithoutKeggIDs(ReactionCI reaction, Container container) {

		Set<String> ret = new HashSet<String>();

		for(String id : reaction.getMetaboliteSetIds()) {

			if(!container.getMetabolitesExtraInfo().get("KEGG_CPD").containsKey(id)) {

				ret.add(id);
			}
		}

		return ret;
	}


	public static Set<String> getTrasportedMetabolitesId(ReactionCI r){
		//		Set<String> ret = new HashSet<String>();

		Set<String> sameMet = CollectionUtils.getIntersectionValues(r.getProducts().keySet(), r.getReactants().keySet());

		return sameMet;
	}


	/**
	 * @param newInternalID
	 * @param newExternalID
	 * @param container
	 * @param reactionCI
	 * @return
	 */
	public static ReactionCI replaceCompartmentsID(String newInternalID, String newExternalID, Container container, ReactionCI reactionCI) {

		Set<String> internalCompartments = new HashSet<String>();
		
		for (StoichiometryValueCI value : reactionCI.getReactants().values()) {

			CompartmentCI compartmentCI = container.getCompartment(value.getCompartmentId());

			if(compartmentCI.getOutside()!=null) {
				internalCompartments.add(compartmentCI.getId());
			}
		}

		for (StoichiometryValueCI value : reactionCI.getProducts().values()) {

			CompartmentCI compartmentCI = container.getCompartment(value.getCompartmentId());
			internalCompartments.add(compartmentCI.getId());
		}
		
		Map<String, StoichiometryValueCI> newReactants = new HashMap<String, StoichiometryValueCI>();
		for (StoichiometryValueCI value : reactionCI.getReactants().values()) {

			StoichiometryValueCI newValue = value.clone();
			CompartmentCI compartmentCI = container.getCompartment(newValue.getCompartmentId());
			
			if(internalCompartments.contains(compartmentCI.getId())) {
			
				newValue.setCompartmentId(newInternalID);				
			}
			else {
				
				newValue.setCompartmentId(newExternalID);				
			}
			newReactants.put(newValue.getMetaboliteId(), newValue);
		}

		Map<String, StoichiometryValueCI> newProducts = new HashMap<String, StoichiometryValueCI>();
		for (StoichiometryValueCI value : reactionCI.getProducts().values()) {

			StoichiometryValueCI newValue = value.clone();
			CompartmentCI compartmentCI = container.getCompartment(newValue.getCompartmentId());
			if(internalCompartments.contains(compartmentCI.getId())) {
				
				newValue.setCompartmentId(newInternalID);				
			}
			else {
				
				newValue.setCompartmentId(newExternalID);				
			}
			newProducts.put(newValue.getMetaboliteId(), newValue);
		}

		ReactionCI newReaction = new ReactionCI(reactionCI.getId(), reactionCI.getName(), reactionCI.getReversible(), newReactants, newProducts);

		newReaction.setGeneRule(reactionCI.getGeneRule());
		newReaction.setProteinRule(reactionCI.getProteinRule());
		newReaction.setGenesIDs(reactionCI.getGenesIDs());
		newReaction.setProteinIds(reactionCI.getProteinIds());
		newReaction.setType(reactionCI.getType());
		newReaction.setAllMetabolitesHaveKEGGId(reactionCI.isAllMetabolitesHaveKEGGId());
		newReaction.setSubsystem(reactionCI.getSubsystem());

		return newReaction;
	} 



	public static Set<String> getTransportedMetabolites(ReactionCI reaction, Set<String> compoundsToBeIgnored) {

		Set<String> ret = new HashSet<String>();
		ret .addAll(reaction.getProducts().keySet());
		ret.retainAll(reaction.getReactants().keySet());

		if(compoundsToBeIgnored!=null)
			ret.removeAll(compoundsToBeIgnored);

		return ret;
	}

	public static Set<String> getTransportedMetabolites(Container container, Set<String> reactions, Set<String> compoundsToBeIgnored) {

		Set<String> ret = new HashSet<String>();

		for(String rid : reactions) {

			ReactionCI r = container.getReaction(rid);
			ret .addAll(getTransportedMetabolites(r, compoundsToBeIgnored));
		}

		return ret;
	}

	public static Set<String> getTransportedMetabolites(Container container, Set<String> compoundsToBeIgnored) {

		return getTransportedMetabolites(container, container.getReactions().keySet(), compoundsToBeIgnored);
	}

	public static Map<String, Set<String>> getMetabolitesByTransportReactions(Container container, Set<String> compoundsToBeIgnored){

		Map<String, Set<String>> metabolites = new HashMap<String, Set<String>>();

		for(ReactionCI r : container.getReactions().values()) {

			metabolites.put(r.getId(), ContainerUtils.getTransportedMetabolites(r, compoundsToBeIgnored));
		}

		return  metabolites;
	}


	public static Set<String> convertUsingDictionaryIgnoreAbsent(Set<String>values, Map<String, String> dic){

		Set<String> ret = new TreeSet<String>();
		for(String id:values){

			String syn = dic.get(id);
			if(syn!=null)
				ret.add(syn);
		}

		return ret;
	}


	public static Container removeGenesFromContainer(Container container, Set<String> genes, boolean verbose) throws InvalidBooleanRuleException  {

		TreeSet<String> allgenes = new TreeSet<String>();
		Set<String> reactionsToRem = new HashSet<String>();

		for(String r : container.getReactions().keySet()) {

			ReactionCI reaction = container.getReaction(r);
			Set<String> r_genes = reaction.getGenesIDs();

			r_genes.retainAll(genes);

			if(verbose)
				System.out.println(r+"\t"+ ContainerUtils.getReactionToString(reaction) + "\t" + genes);

			reaction.setGeneRule(CollectionUtils.join(r_genes, " OR "));

			if(r_genes.size()==0)
				reactionsToRem.add(r);

			allgenes.addAll(r_genes);
		}

		if(verbose)
			System.out.println(allgenes.size() + " " + allgenes);

		container.getGenes().keySet().retainAll(allgenes);
		container.removeReactions(reactionsToRem);

		return container;
	}

	public static void removeDuplicateReactions(Container container){

		Map<String, Set<String>> duplicate = container.identifyDuplicateReactionsByStequiometry(true);
		Set<String> toRemove = new HashSet<String>();

		for(Set<String> set: duplicate.values()) {

			toRemove.addAll(set);
		}

		System.out.println("Remove r: " + toRemove.size() + toRemove);
		container.removeReactions(toRemove);
	}


	public static Set<String> getAllEcNumbers(Container container) {
		Set<String> ecs = new HashSet<String>();
		for(ReactionCI r : container.getReactions().values()){
			
			if(r.getEcNumber()!= null && !r.getEcNumber().equals("")){
				
				
				String[] info = r.getEcNumber().split("[, ;:]");
//				System.out.println(r.getEcNumber() + "\t" + CollectionUtils.join(info, "\t"));
				for(String ec : info){
					if(!ec.equals("")) ecs.add(ec.trim());
				}
			}
		}
		return ecs;
	}


	public static Set<String> getReactionsWithGPR(Container container,
			Set<String> ids) {
		Set<String> rs = new HashSet<String>();
		for(String r : ids){
			ReactionCI rci = container.getReaction(r);
			if(rci.getGeneRuleString() != null && 
					!rci.getGeneRuleString().equals(""))
				rs.add(r);
		}
		return rs;
	}


	public static Set<String> getInternalReactions(Container container) {
		Set<String> allReactions = new HashSet<String>(container.getReactions().keySet());
		allReactions.removeAll(container.getDrains());
		allReactions.removeAll(container.identifyTransportReactions());
		return allReactions;
	}
	
	public static Container subContainer(Container origin, Collection<String> reactions) throws IOException{
		Container c = new Container(origin);
		c.getReactions().keySet().retainAll(reactions);
		c.verifyDepBetweenClass();
		
		return c;
	}
}
