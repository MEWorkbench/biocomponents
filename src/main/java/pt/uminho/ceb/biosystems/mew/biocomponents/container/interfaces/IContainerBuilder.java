/**
O * SilicoLife
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
package pt.uminho.ceb.biosystems.mew.biocomponents.container.interfaces;

import java.io.Serializable;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;

public interface IContainerBuilder extends Serializable{
	
	//Container/model indentification
	String getModelName();
	String getOrganismName();
	String getNotes();
	Integer getVersion();
	
	//NetWork Information
	Map<String, CompartmentCI> getCompartments();
	Map<String, ReactionCI> getReactions();
	Map<String, MetaboliteCI> getMetabolites();
	Map<String, GeneCI> getGenes();
	Map<String, Map<String, String>> getMetabolitesExtraInfo();
	Map<String, Map<String, String>> getReactionsExtraInfo();
	
	//Simulation Information
	String getBiomassId();
	Map<String, ReactionConstraintCI> getDefaultEC();
//	Map<String, String> getMetaboliteIdToSpecieTermId();
//	Map<String, SpeciesTypesInformation> getsTypeInformationList();
	
	String getExternalCompartmentId();


}
