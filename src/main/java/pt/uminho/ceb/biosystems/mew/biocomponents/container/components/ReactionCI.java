package pt.uminho.ceb.biosystems.mew.biocomponents.container.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions.MetaboliteDoesNotPresentInReaction;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.tree.BooleanTreeUtils;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.AbstractSyntaxTree;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.AbstractSyntaxTreeNode;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.DataTypeEnum;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.IValue;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.parser.ParseException;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.parser.ParserSingleton;

public class ReactionCI implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String id;
	protected String name;
	
	
	protected Boolean reversible;
	protected Map<String, StoichiometryValueCI> products;
	protected Map<String, StoichiometryValueCI> reactants;
	protected ReactionTypeEnum type = ReactionTypeEnum.Undefined;

	private boolean allMetabolitesHaveKEGGId;

	protected String ecNumber = null;
	 
	protected Set<String> subsystems = null;	
	protected AbstractSyntaxTree<DataTypeEnum, IValue> geneRule;
	protected AbstractSyntaxTree<DataTypeEnum, IValue> proteinRule;

	protected Set<String> genesIDs;
	protected Set<String> proteinIds;
	protected Set<String> ecNumbers;


	public ReactionCI(String shortName, String name, Boolean reversible,
			Map<String, StoichiometryValueCI> reactants,
			Map<String, StoichiometryValueCI> products) {
		this.id = shortName;
		this.name = name;
		if(reversible!=null)
			this.reversible = reversible;
		else
			this.reversible = true;
		this.products = products;
		this.reactants = reactants;
	}

	/*
	 * Constructor from object ReactionCI
	 * Added by Noronha
	 */
	public ReactionCI(ReactionCI reaction){
		this.id = reaction.getId();
		this.name = reaction.getName();
		this.reversible = reaction.isReversible();
		this.products = reaction.getProducts();
		this.reactants = reaction.getReactants();
		this.setAllMetabolitesHaveKEGGId(reaction.isAllMetabolitesHaveKEGGId());	
	}

	/*
	 * Constructor from Reaction
	 * Added by Noronha
	 */

	//FIXME: Nao podemos teste constructor aqui... Quanto muito podemos ter   "public ReactionCI(String id, String name, boolean reversible)"
	//	public ReactionCI(Reaction reaction){
	//		this.id = reaction.getId();
	//		this.name = reaction.getName();
	//		this.reversible = reaction.isReversible();
	//	}

	public Set<String> identifyCompartments(){

		Set<String> ret = getCompartments(this.getReactants());
		ret.addAll(getCompartments(this.getProducts()));

		return ret;
	}

	public Set<String> getCompartments(Map<String, StoichiometryValueCI> stoich){
		Set<String> ret = new HashSet<String>();

		for(StoichiometryValueCI value : stoich.values()){
			ret.add(value.getCompartmentId());
		}
		return ret;

	}

	public AbstractSyntaxTree<DataTypeEnum, IValue> getGeneRule() {
		return geneRule;
	}

	public Set<String> setGeneRule(String gRule) throws InvalidBooleanRuleException {
		genesIDs = new HashSet<String>();
		if(gRule == null){
			this.geneRule = null;
		}
		else{
			if(gRule.equals("")){
				this.geneRule = new AbstractSyntaxTree<DataTypeEnum, IValue>();
			}else{

				if(BooleanTreeUtils.valExpression(gRule)){
					AbstractSyntaxTreeNode<DataTypeEnum, IValue> ast;
					try {
						ast = ParserSingleton.boolleanParserString(gRule);
						this.geneRule = new AbstractSyntaxTree<DataTypeEnum, IValue>(ast);
						genesIDs.addAll(withdrawVariablesInRule(geneRule));
					} catch (ParseException e) {
						throw new InvalidBooleanRuleException(gRule);
					}


				}
				else{
					throw new InvalidBooleanRuleException(gRule);
				}
				//				if (genesIDs==null) genesIDs = new ArrayList<String>();
			}
		}
		
		return genesIDs;
	}

	public AbstractSyntaxTree<DataTypeEnum, IValue> getProteinRule() {
		return proteinRule;
	}

	public void setProteinRule(String pRule) throws InvalidBooleanRuleException {
		
		if(pRule == null){
			this.proteinRule = null;
		}
		else{
			if(pRule.equals("")){
				this.proteinRule = new AbstractSyntaxTree<DataTypeEnum, IValue>();
			}else{
				AbstractSyntaxTreeNode<DataTypeEnum, IValue> ast;
				try {
					ast = ParserSingleton.boolleanParserString(pRule);
				} catch (ParseException e) {
					throw new InvalidBooleanRuleException(pRule);
				}
				
				this.proteinRule = new AbstractSyntaxTree<DataTypeEnum, IValue>(ast);
				proteinIds = new HashSet<String>(withdrawVariablesInRule(proteinRule));
			}
		}

	}

	public Set<String> getGenesIDs() {
		if(genesIDs==null)
			genesIDs = new HashSet<String>();

		return genesIDs;
	}

	public Set<String> getProteinIds() {
		return proteinIds;
	}


	public Boolean getReversible() {
		return reversible;
	}


	public String getSubsystem() {
		return CollectionUtils.join(subsystems, "; ");
	}

	public void setSubsystem(String subsystem) {
		this.subsystems = new HashSet<String>(); 
		this.subsystems.add(subsystem);
	}
	
	public Set<String> getSusbystems() {
		return subsystems;
	} 
	
	public void setSubsystems(Set<String> subsystems) {
		this.subsystems = subsystems;
	}
	
	public String getEcNumber() {
		return ecNumber;
	}
	
	public Set<String> getEcNumbers() {
		return ecNumbers;
	}
	
	public void setEcNumbers(Set<String> ecNumbers) {
		this.ecNumbers = ecNumbers;
	}
	
	public void setEcNumber(AbstractSyntaxTree<DataTypeEnum, IValue> ecNumber) {
		this.ecNumber = ecNumber.toString();
	}

	public String getEc_number() {
		return ecNumber == null ? "" : ecNumber.toString();
	}

	public void setEc_number(String ecNumber) {
		if(ecNumber == null){
			this.ecNumber = null;
		}
		else{
//			if(ecNumber.equals("")){
//				this.proteinRule = new AbstractSyntaxTree<DataTypeEnum, IValue>();
//			}else{
//				AbstractSyntaxTreeNode<DataTypeEnum, IValue> ast;
//				try {
//					ast = ParserSingleton.boolleanParserString(ecNumber);
//				} catch (ParseException e) {
//					throw new InvalidBooleanRuleException(ecNumber);
//				}
//				
//				this.ecNumber = new AbstractSyntaxTree<DataTypeEnum, IValue>(ast);
//				ecNumbers = new HashSet<String>(withdrawVariablesInRule(this.ecNumber));
			this.ecNumber = ecNumber;
//			}
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isReversible() {
		return reversible;
	}

	public void setReversible(boolean reversible) {
		this.reversible = reversible;
	}

	public Map<String, StoichiometryValueCI> getProducts() {
		return products;
	}

	public void setProducts(Map<String, StoichiometryValueCI> products) {
		this.products = products;
	}

	public Map<String, StoichiometryValueCI> getReactants() {
		return reactants;
	}

	public void setReactants(Map<String, StoichiometryValueCI> reactants) {
		this.reactants = reactants;
	}

	public ReactionTypeEnum getType() {
		return type;
	}

	public void setType(ReactionTypeEnum type) {
		this.type = type;
	}



	public void setGeneRule(AbstractSyntaxTree<DataTypeEnum, IValue> geneRule) {
		this.geneRule = geneRule;
	}


	public void setProteinRule(AbstractSyntaxTree<DataTypeEnum, IValue> proteinRule) {
		this.proteinRule = proteinRule;
	}


	public void setGenesIDs(Set<String> genesIDs) {
		this.genesIDs = genesIDs;
	}


	public void setProteinIds(Set<String> proteinIds) {
		this.proteinIds = proteinIds;
	}

	@Override
	public ReactionCI clone(){

		Map<String, StoichiometryValueCI> newReactants = new HashMap<String, StoichiometryValueCI>();
		for (StoichiometryValueCI value : reactants.values()) {
			StoichiometryValueCI newValue = value.clone();
			newReactants.put(newValue.getMetaboliteId(), newValue);
		}

		Map<String, StoichiometryValueCI> newProducts = new HashMap<String, StoichiometryValueCI>();
		for (StoichiometryValueCI value : products.values()) {
			StoichiometryValueCI newValue = value.clone();
			newProducts.put(newValue.getMetaboliteId(), newValue);
		}

		ReactionCI cloneReaction = new ReactionCI(id, name, reversible, newReactants, newProducts);

		// NOTE:
		cloneReaction.setGeneRule(this.getGeneRule());
		cloneReaction.setProteinRule(this.proteinRule);
		cloneReaction.setGenesIDs(this.genesIDs);
		cloneReaction.setProteinIds(this.proteinIds);
		cloneReaction.setType(this.type);
		cloneReaction.setAllMetabolitesHaveKEGGId(this.allMetabolitesHaveKEGGId);
		cloneReaction.setSubsystems(this.subsystems);
		cloneReaction.setEc_number(this.ecNumber);
		if(this.getEcNumbers()!=null)
			cloneReaction.setEcNumbers(new HashSet<>(this.getEcNumbers()));
		return cloneReaction;
	}

	public void addGene(String gene){
		//		System.out.println("gene3: " + gene);
		if(genesIDs == null)
			genesIDs = new HashSet<String>();
		genesIDs.add(gene);
		//		System.out.println("genes:"+ genesIDs);
	}

	public void addProtein(String protein){
		if(proteinIds == null)
			proteinIds = new HashSet<String>();
		proteinIds.add(protein);
	}

	public boolean containsMetaboliteInProducts(String metabId, String compId){

		boolean ret = false;

		StoichiometryValueCI value = products.get(metabId);		
		if( value != null && value.getCompartmentId().equals(compId))
			ret = true;

		return ret;
	}

	public boolean containsMetaboliteInReactants(String metabId, String compId){
		boolean ret = false;

		StoichiometryValueCI value = reactants.get(metabId);		
		if( value != null && value.getCompartmentId().equals(compId))
			ret = true;

		return ret;

	}

	public boolean containsMetabolite(String metabId, String compId){

		return containsMetaboliteInReactants(metabId, compId) || containsMetaboliteInProducts(metabId, compId);
	}

	public Set<String> getTransportedMetabolites(){
		Set<String> mets = getMetaboliteSetIds();
		
		Set<String> t = new HashSet<>();
		for(String id : mets){
			Set<String> compartmets = getMetaboliteCompartment(id);
			if(compartmets.size() >1) t.add(id);
		}
		return t;
	}
	
	
	public Set<String> getMetaboliteCompartment(String metId) {
		Set<String> comp = new LinkedHashSet<String>();
		
		StoichiometryValueCI rr = reactants.get(metId);
		StoichiometryValueCI pr = products.get(metId);
		
		if(rr != null) comp.add(rr.getCompartmentId());
		if(pr != null) comp.add(pr.getCompartmentId());
		
		if(comp.size() == 0) throw new MetaboliteDoesNotPresentInReaction(metId, getId());
		
		return comp;
	}
	
	
	
	
	//	public boolean hasTheSameStoichiometry(ReactionCI reaction){
	//		
	//		boolean continueFlag = true;
	//		
	//		if(reversible.equals(reaction.getReversible())){
	//			
	//			continueFlag = products.equals(reaction.getProducts()) && reactants.equals(reaction.getReactants());
	//			
	//			
	//			if(reversible && !continueFlag){
	//				continueFlag = reactants.equals(reaction.getProducts()) && products.equals(reaction.getReactants());
	//			}
	//		}
	//		else{
	//			continueFlag = false;
	//		}
	//		
	//		return continueFlag;
	//	}

	//	private boolean isTheSameStoik(Map<String, StoichiometryValueCI> stoic1, Map<String, StoichiometryValueCI> stoic2){
	//		
	//		boolean isTheSame = true;
	//			
	//		if(stoic1.size() == stoic2.size()){
	//			
	//			for(String met : stoic1.keySet()){
	//				if(!(stoic2.containsValue(met) && stoic1.get(met).equals(stoic2.get(met)))){
	//					isTheSame = false;
	//					break;
	//				}
	//				
	//			}
	//		}
	//		
	//		return isTheSame;
	//	}



	public boolean isDrain() {
		return type.equals(ReactionTypeEnum.Drain);
	}

	public static ArrayList<String> withdrawVariablesInRule(AbstractSyntaxTree<DataTypeEnum, IValue> booleanRule){

		ArrayList<String> ret = new ArrayList<String>();
		AbstractSyntaxTreeNode<DataTypeEnum, IValue> root = null;

		if(booleanRule!=null)
			root = booleanRule.getRootNode();
		else
			return ret;

		Queue<AbstractSyntaxTreeNode<DataTypeEnum, IValue>> nodeQueue = new LinkedList<AbstractSyntaxTreeNode<DataTypeEnum, IValue>>();
		nodeQueue.offer(root);

		while(!nodeQueue.isEmpty()){
			AbstractSyntaxTreeNode<DataTypeEnum, IValue> currentNode = nodeQueue.poll();

			for(int i = 0; i < currentNode.getNumberOfChildren(); i++)
				nodeQueue.offer(currentNode.getChildAt(i));

			if(currentNode.isLeaf())
				ret.add(currentNode.toString());
		}

		return ret;
	}

	public boolean hasSameStoichiometry(ReactionCI r, boolean rev_in_account) {
		return hasSameStoichiometry(r, rev_in_account, false);
	}
	
//	@Deprecated
	public boolean hasSameStoichiometry(ReactionCI r, boolean rev_in_account, boolean ignoreCompartments){
		
		
//		boolean issamedir = (reversible == r.isReversible());
		boolean doubleTest = true;
		boolean equals = true;
		if(rev_in_account){
			equals = reversible == r.isReversible();
			doubleTest = (equals && reversible);
		}
		
		equals = equals && compareStoichiometry(r.getReactants(), r.getProducts(), ignoreCompartments);
		
		if(doubleTest && !equals)
			equals = compareStoichiometry(r.getProducts(), r.getReactants(), ignoreCompartments);
				

		return equals;
	}
	
	//TODO Change this name to compareStoichiometry
	
	/**
	 * Compare reactions
	 * 
	 * output = null diferent stoichiometry
	 *        = 0  same stoiquiometry same reversibility 
	 *        = -1 same stoichiometry reverse direction
	 *        = 1  same stoiquiometry direct direction
	 *        = 2 change to irreversible
	 *        
	 * @param r
	 * @param rev_in_account
	 * @param ignoreCompartments
	 * @return
	 */
	public Integer hasSameStoichiometry2(ReactionCI r, boolean ignoreCompartments){
		
		boolean doubleTest = true;
		boolean equals = true;
		
		Integer ret = null;
		
		equals = equals && compareStoichiometry(r.getReactants(), r.getProducts(), ignoreCompartments);
		if(equals)
			ret = 1;
		
		if(doubleTest && !equals){
			equals = compareStoichiometry(r.getProducts(), r.getReactants(), ignoreCompartments);
			if(equals) ret = -1;
		}
		
		if(ret != null){
			if(isReversible() == r.isReversible())
				ret = 0;
			else if(r.isReversible() && !isReversible())
				ret = 2;
		}
		
		return ret;
	}
	
	protected boolean compareStoichiometry(
			Map<String, StoichiometryValueCI> otherReactants, 
			Map<String, StoichiometryValueCI> otherProducts,
			boolean ignoreCompartments) {
		if(otherReactants.size()>0 && !(otherReactants.keySet().containsAll(reactants.keySet()) 
				&& reactants.keySet().containsAll(otherReactants.keySet()))) 
			return false;
		
		if(otherProducts.size()>0 && !(otherProducts.keySet().containsAll(products.keySet()) 
				&& products.keySet().containsAll(otherProducts.keySet()))) 
			return false;	
		
		boolean ret = true;
		
		for(String producId : products.keySet()) {
			StoichiometryValueCI myStoich = products.get(producId);
			StoichiometryValueCI otherStoich = otherProducts.get(producId);
			boolean sameStoich = myStoich!=null && otherStoich != null && (myStoich.getStoichiometryValue().equals(otherStoich.getStoichiometryValue()));
				ret = ret && sameStoich;
			if(!ignoreCompartments){
				boolean sameComp = myStoich!=null && otherStoich != null && (myStoich.getCompartmentId().equals(otherStoich.getCompartmentId()));
				ret = ret && sameComp;
			}
		}
		
		
		for(String producId : reactants.keySet()) {
			StoichiometryValueCI myStoich = reactants.get(producId);
			StoichiometryValueCI otherStoich = otherReactants.get(producId);
			boolean sameStoich = myStoich!=null && otherStoich != null && (myStoich.getStoichiometryValue().equals(otherStoich.getStoichiometryValue()));
				ret = ret && sameStoich;
			if(!ignoreCompartments){
				boolean sameComp = myStoich!=null && otherStoich != null && (myStoich.getCompartmentId().equals(otherStoich.getCompartmentId()));
				ret = ret && sameComp;
			}
		}
		
		return ret;
	}

//	public boolean hasSameStoichiometry(ReactionCI r, boolean rev_in_account, boolean ignoreCompartments){
//
//		boolean equals = true;
//
//		if(ignoreCompartments) {
//			
//			Map<String, StoichiometryValueCI> reactClone = new HashMap<String,StoichiometryValueCI>(), 
//					prodClone = new HashMap<String,StoichiometryValueCI>(), 
//					rClone = new HashMap<String,StoichiometryValueCI>(), 
//					pClone = new HashMap<String,StoichiometryValueCI>();
//			
//			for(String key : reactants.keySet()) {
//				
//				StoichiometryValueCI s = new StoichiometryValueCI(key, reactants.get(key).getStoichiometryValue(), "");
//				reactClone.put(key, s);
//			}
//			
//			for(String key : products.keySet()) {
//				
//				StoichiometryValueCI s = new StoichiometryValueCI(key, products.get(key).getStoichiometryValue(), "");
//				prodClone.put(key, s);
//			}
//			
//			for(String key : r.getReactants().keySet()) {
//				
//				StoichiometryValueCI s = new StoichiometryValueCI(key, r.getReactants().get(key).getStoichiometryValue(), "");
//				rClone.put(key, s);
//			}
//			
//			for(String key : r.getProducts().keySet()) {
//			
//				StoichiometryValueCI s = new StoichiometryValueCI(key, r.getProducts().get(key).getStoichiometryValue(), "");
//				pClone.put(key, s);
//			}
//
//			boolean doubleTest =true;
//
//			if(rev_in_account) {
//				
//				equals = reversible == r.isReversible();
//				doubleTest = (equals && reversible);
//			}
//
//			equals = equals && reactClone.equals(rClone);
//			equals = equals && prodClone.equals(pClone);
//
//			if(doubleTest && !equals)
//				equals = reactClone.equals(pClone) && prodClone.equals(rClone);
//
//		}
//		else {
//
//			equals = this.hasSameStoichiometry(r, rev_in_account);
//		}
//
//		return equals;
//	}

	//	//FIXME: In This moment the reaction only have one ecnumber
	//	public List<String> getEcnumbers() {
	//		
	//		return null;
	//	}

	public Set<String> getMetaboliteSetIds(){

		Set<String> ret = new HashSet<String>(products.keySet());
		ret.addAll(reactants.keySet());
		return ret;
	}

	/**
	 * @return the allMetabolitesHaveKEGGId
	 */
	public boolean isAllMetabolitesHaveKEGGId() {
		return allMetabolitesHaveKEGGId;
	}

	/**
	 * @param allMetabolitesHaveKEGGId the allMetabolitesHaveKEGGId to set
	 */
	public void setAllMetabolitesHaveKEGGId(boolean allMetabolitesHaveKEGGId) {
		this.allMetabolitesHaveKEGGId = allMetabolitesHaveKEGGId;
	}

	public boolean validStoiquiometryReaction() {

		boolean ret = true;

		Set<String> possibleMetProblem = CollectionUtils.getIntersectionValues(reactants.keySet(), products.keySet());

		for(String id : possibleMetProblem)
			if(reactants.get(id).getCompartmentId().equals(products.get(id).getCompartmentId())){
				ret=false;
				break;
			}

		return ret;
	}
	
	public Set<String> getMetabolitesWithWrongStoichiometry() {

		Set<String> commonMet = CollectionUtils.getIntersectionValues(this.getProducts().keySet(), this.getReactants().keySet());
		Set<String> ret = new HashSet<String>();
		
		for(String mId : commonMet){
			if(this.getProducts().get(mId).getCompartmentId().equals(this.getReactants().get(mId).getCompartmentId())){
				ret.add(mId);
			}
		
		}

		return ret;
	}
	
	public void switchProductsAndReactants() {
		Map<String, StoichiometryValueCI> aux = products;
		products = reactants;
		reactants = aux;
	}

	public void changeMetaboliteIds(
			Map<String, String> dictionary) {

		this.products = changeStoiq(dictionary, products);
		this.reactants = changeStoiq(dictionary, reactants);

	}

	private Map<String, StoichiometryValueCI> changeStoiq(Map<String, String> dic, Map<String, StoichiometryValueCI> stoiq){

		Map<String, StoichiometryValueCI> ret = new HashMap<String, StoichiometryValueCI>();

		for(StoichiometryValueCI v : stoiq.values()){
			String old = v.getMetaboliteId();
			String newId = dic.get(old);

			if(newId!=null)
				v.setMetaboliteId(newId);

			ret.put(v.getMetaboliteId(), v);
		}

		return ret;
	}

	public String toStringStoiquiometry(){

		String reactants = toStringStoiquiometry(getReactants());
		String products = toStringStoiquiometry(getProducts());

		String rev = (reversible?"\t<->\t":"\t-->\t");
		return reactants + rev + products;
	}

	public String toStringStoiquiometry(Map<String, StoichiometryValueCI> stoiq){
		String ret = "";
		if(stoiq!=null)
			for(StoichiometryValueCI v : stoiq.values()){
				ret += " +"+v.getStoichiometryValue() + " * " +v.getMetaboliteId();
			}

		return ret;
	}
	
	public String getGeneRuleString(){
		return (geneRule!=null)?geneRule.toString():"";
	}

//	/* (non-Javadoc)
//	 * @see java.lang.Object#toString()
//	 */
//	@Override
//	public String toString() {
//		return "ReactionCI [id=" + id + ", name=" + name + ", reversible="
//				+ reversible + ", products=" + products + ", reactants="
//				+ reactants + ", type=" + type + ", allMetabolitesHaveKEGGId="
//				+ allMetabolitesHaveKEGGId + ", ec_number=" + ec_number
//				+ ", subsystem=" + subsystem + ", geneRule=" + geneRule
//				+ ", proteinRule=" + proteinRule + ", genesIDs=" + genesIDs
//				+ ", proteinIds=" + proteinIds + "]";
//	}

	
	public void changeCompartmentId(String oldId, String newId){
		changeStoichiometryCompartment(oldId, newId, reactants);
		changeStoichiometryCompartment(oldId, newId, products);
	}

	private void changeStoichiometryCompartment(String oldId, String newId, Map<String, StoichiometryValueCI> stoic) {
		for(StoichiometryValueCI s: stoic.values()){
			if(s.getCompartmentId().equals(oldId))
				s.setCompartmentId(newId);
		}
	}
}
