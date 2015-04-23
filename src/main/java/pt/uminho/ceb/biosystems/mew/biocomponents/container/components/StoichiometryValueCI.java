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

public class StoichiometryValueCI implements Serializable, Cloneable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String metaboliteId;
	protected String compartmentId;
	protected Double stoichiometryValue;
	
	public StoichiometryValueCI(String metaboliteId, Double stoichiometryValue, String compartmentId) {
		this.metaboliteId = metaboliteId;
		this.stoichiometryValue = stoichiometryValue;
		this.compartmentId = compartmentId;
	}

	public String getCompartmentId() {
		return compartmentId;
	}

	public void setMetaboliteId(String metaboliteId) {
		this.metaboliteId = metaboliteId;
	}

	public Double getStoichiometryValue() {
		return stoichiometryValue;
	}

	public void setStoichiometryValue(Double stoichiometryValue) {
		this.stoichiometryValue = stoichiometryValue;
	}
	
	public String getMetaboliteId() {
		return metaboliteId;
	}
	
	public StoichiometryValueCI clone(){
		return new StoichiometryValueCI(metaboliteId, stoichiometryValue, compartmentId);
	}
	
	public void setCompartmentId(String compartmentId){
		
		this.compartmentId = compartmentId;
	}
	
	@Override
	public boolean equals(Object value){
		
		if(value.getClass().isAssignableFrom(StoichiometryValueCI.class))
		return metaboliteId.equals(((StoichiometryValueCI)value).getMetaboliteId()) && 
			compartmentId.equals(((StoichiometryValueCI)value).getCompartmentId()) &&
			stoichiometryValue.equals(((StoichiometryValueCI)value).getStoichiometryValue());
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StoichiometryValueCI ["
				+ (metaboliteId != null ? "metaboliteId=" + metaboliteId + ", "
						: "")
				+ (compartmentId != null ? "compartmentId=" + compartmentId
						+ ", " : "")
				+ (stoichiometryValue != null ? "stoichiometryValue="
						+ stoichiometryValue : "") + "]";
	}
	
	
}
