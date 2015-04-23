
package pt.uminho.ceb.biosystems.mew.biocomponents.container.components;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

public class CompartmentCI implements Serializable, Cloneable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String id;
	protected String name;
	protected String outside;
	protected Set<String> metabolitesInCompartmentID;


	/***
	 * 
	 * @param shortName
	 * @param name
	 * @param outside
	 */
	public CompartmentCI(String shortName, String name, String outside) {
		this.id = shortName;
		this.name = name;
		this.outside = outside;
		this.metabolitesInCompartmentID = new TreeSet<String>();
	}

	public CompartmentCI(CompartmentCI compartmentCI) {
		this.id = compartmentCI.id;
		this.name = compartmentCI.name;
		this.outside = compartmentCI.outside;
		this.metabolitesInCompartmentID = new TreeSet<String>(compartmentCI.metabolitesInCompartmentID);
	}

	public CompartmentCI clone() {
		return new CompartmentCI(this);
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

	public String getOutside() {
		return outside;
	}

	public void setOutside(String outside) {
		this.outside = outside;
	}

	public String toString(){
		return id + name+ outside;
	}
	
	public void addMetaboliteInCompartment(String metaboliteId){
		if(!metabolitesInCompartmentID.contains(metaboliteId))
		metabolitesInCompartmentID.add(metaboliteId);
	}
	
	public Set<String> getMetabolitesInCompartmentID() {
		return metabolitesInCompartmentID;
	}

	public void setMetabolitesInCompartmentID(Set<String> metabolitesInCompartmentID) {
		this.metabolitesInCompartmentID = metabolitesInCompartmentID;
	}
	
	
}
