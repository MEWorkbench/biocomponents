/**
 * SilicoLife
 * 
 * Copyright 2010
 * 
 * <p>This is PROPRIETARY software.</p>
 * <p>You can not copy, distribute, modify or for this matter,</p> 
 * <p>proceed into any other type of unauthorized form of use for this code</p>
 * 
 * 
 * www.silicolife.com @see <a href="http://www.silicolife.com">SilicoLife</a>
 * 
 * (c) All rights reserved
 */
package pt.uminho.ceb.biosystems.mew.biocomponents.container.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MetaboliteCI implements Serializable, Cloneable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String id;
	protected String name;
	protected String formula = null;
	protected String smiles;
	protected int charge;
	protected double mass;
	protected List<String> synonyms;
	protected String inchikey;
	
	protected Set<String> reactionsId;
	
	public MetaboliteCI(String shortName, String name) {
		this.id = shortName;
		this.name = name;
		reactionsId = new TreeSet<String>();
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
	
	public String toString(){
		return id + " charge:" + charge;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public String getSmiles() {
		return smiles;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}

	public Integer getCharge() {
		return charge;
	}

	public void setCharge(Integer charge) {
		if(charge == null)
			this.charge = 0;
		else
			this.charge = charge;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public List<String> getSymnonyms() {
		return synonyms;
	}

	public void setSymnonyms(List<String> symnonyms) {
		this.synonyms = symnonyms;
	}

	
	public void addReaction(String reactionId){
		
		//if(reactionsId==null)
		//	reactionsId = new TreeSet<String>();
		
		if(!reactionsId.contains(reactionId))
			reactionsId.add(reactionId);
	}
	
	public Set<String> getReactionsId(){
		return reactionsId;
	}

	public String getInchikey() {
		return inchikey;
	}

	public void setInchikey(String inchikey) {
		this.inchikey = inchikey;
	}

	public void setReactionsId(Set<String> reactionsId) {
		this.reactionsId = reactionsId;
	}
	
	public MetaboliteCI clone(){
		
		MetaboliteCI met = new MetaboliteCI(id, name);
		met.setCharge(this.charge);
		met.setFormula(this.formula);
		met.setMass(this.mass);
		if(synonyms != null)
		met.setSymnonyms(new ArrayList<String>(synonyms));
		met.setInchikey(this.inchikey);
		met.setReactionsId(new HashSet<String>(this.reactionsId));
		return met;
	}

//	public void setInformtaion(MetaboliteExternalRef metER) {
//		
//		name = metER.getName();
//		formula = metER.getFormula();
//		smiles = metER.getSmiles();
//		charge = metER.getCharge();
////		mass = metER.getMass();
////		symnonyms = metER.;
//		inchikey = metER.getInchikey();
//		
//	}
}
