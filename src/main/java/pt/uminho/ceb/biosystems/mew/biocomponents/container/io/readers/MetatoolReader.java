/*
 * Copyright 2010
 * IBB-CEB - Institute for Biotechnology and Bioengineering - Centre of Biological Engineering
 * CCTC - Computer Science and Technology Center
 *
 * University of Minho 
 * 
 * This is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This code is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICimport java.util.HashMap;ULAR PURPOSE. See the 
 * GNU Public License for more details. 
 * 
 * You should have received a copy of the GNU Public License 
 * along with this code. If not, see http://www.gnu.org/licenses/ 
 * 
 * Created inside the SysBioPseg Research Group (http://sysbio.di.uminho.pt)
 */
package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.interfaces.IContainerBuilder;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;


/**
 * @author pmaia
 */
public class MetatoolReader implements IContainerBuilder{
	
	private static final long serialVersionUID = 1L;
	
	/** Metatool file tags */
	final static String REVERSIBLE_REACTIONS_DEFAULT_IDENTIFIER = "-ENZREV";
	final static String IRREVERSIBLE_REACTIONS_DEFAULT_IDENTIFIER = "-ENZIRREV";
	final static String INTERNAL_METABOLITES_DEFAULT_IDENTIFIER = "-METINT";
	final static String EXTERNAL_METABOLITES_DEFAULT_IDENTIFIER = "-METEXT";
	final static String REACTION_CATALYSIS_DEFAULT_IDENTIFIER = "-CAT";
	
	/** Compartments */
	public final static String DEFAULT_METATOOL_EXTERNAL_COMPARTMENT = "e";
	public final static String DEFAULT_METATOOL_INTERNAL_COMPARTMENT = "c";
	
	private ArrayList<String> reversibleReactions;
	private ArrayList<String> irreversibleReactions;
	private Set<String> internalMetabolites;
	private Set<String> externalMetabolites;	
	
	private Map<String, CompartmentCI> compartmentSet = new IndexedHashMap<String,CompartmentCI>();
	private Map<String, MetaboliteCI> metaboliteSet = new IndexedHashMap<String, MetaboliteCI>();
	private Map<String, ReactionCI> reactionSet = new IndexedHashMap<String, ReactionCI>();
	private String biomassId;
	protected Map<String, Map<String, String>> metabolitesExtraInfo = null;
	protected Map<String, Map<String, String>> reactionsExtraInfo = null;
	
	
	public MetatoolReader(String path) throws Exception{
		File f = new File(path);
		if(f != null && !f.exists()) throw new Exception("The file "+f.getAbsolutePath()+" does not exist!");
		reversibleReactions = new ArrayList<String>();
		irreversibleReactions = new ArrayList<String>();
		internalMetabolites = new TreeSet<String>();
		externalMetabolites = new TreeSet<String>();
		
		reactionSet = new HashMap<String, ReactionCI>();
		this.read(path);
		
		populateInformation();
		
	}
	
	/**
	 * @param biomassId The biomass ID to be set
	 */
	public void setBiomassId(String biomassId) {
		this.biomassId = biomassId;
	}

	/**
	 * Populates the container structures with the file information
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void populateInformation() throws Exception {
		
		/** compartments */
		CompartmentCI internalCompartment = new CompartmentCI(DEFAULT_METATOOL_INTERNAL_COMPARTMENT,"INTERNAL", DEFAULT_METATOOL_EXTERNAL_COMPARTMENT);
		CompartmentCI externalCompartment = new CompartmentCI(DEFAULT_METATOOL_EXTERNAL_COMPARTMENT,"EXTERNAL","");
		
		for (String i : internalMetabolites){
			internalCompartment.addMetaboliteInCompartment(i);
		}
		
		for (String e : externalMetabolites){
			externalCompartment.addMetaboliteInCompartment(e);
		}
		
		//Checks if there are metabolites present in both internal and external compartments
		Set<String> inter = (Set<String>) CollectionUtils.getIntersectionValues(internalMetabolites, externalMetabolites);
		if(inter.size()>0)
			throw new Exception("The ids " + inter + " are in both internal and external sets of metabolites");
		
		compartmentSet.put(internalCompartment.getId(),internalCompartment);
		compartmentSet.put(externalCompartment.getId(),externalCompartment);
		
		
		/** metabolites */
		
		for(String internal : this.getInternalMetabolites()){
			MetaboliteCI metabolites = new MetaboliteCI(internal,internal);
			metaboliteSet.put(metabolites.getId(), metabolites);
		}
		
		for(String external : this.getExternalMetabolites()){
			MetaboliteCI metabolites = new MetaboliteCI(external, external);
			metaboliteSet.put(metabolites.getId(), metabolites);
		}		
		
		//Checks if there are metabolites in both reversible and irreversible sets of reactions
		inter = (Set<String>) CollectionUtils.getIntersectionValues(reversibleReactions, irreversibleReactions);
		if(inter.size()>0)
			throw new Exception("The ids " + inter + " are in both reversible and irreversible sets of reactions");
		
				
		Iterator<?> irreversibleReactionsIterator = irreversibleReactions.iterator();
		while(irreversibleReactionsIterator.hasNext()){
			String reaction = (String) irreversibleReactionsIterator.next();
			reactionSet.get(reaction).setReversible(false);
		}
	}
	
	/**
	 * Reads the metatool file
	 * @param path the file path
	 * @throws Exception
	 */
	public void read(String path) throws Exception{
		
		FileReader f = new FileReader(path);
		BufferedReader r = new BufferedReader(f);
		
		/** read reversible reactions */
		String str = new String("");
		String flag =null;
		
		int line = 0;
		while(r.ready()){
			line++;
			str = r.readLine();
			if(!str.matches("\\s*#.*") && !str.trim().equals("")){		//if it isn't a comment nor an empty line
				if(str.matches("\\s*-.*")){								//if it is a metatool flag
					flag=str.trim();
				}
				else{
					readLine(flag,str,line);
				}
			}
		}
		
		verifyMetatoolFile();

		r.close();
		f.close();
		
	}
	
	/**
	 * Reads each line in the file, and forwards it to the right path
	 * @param flag the metatool file flag (ENZREV, ENZIRREV, METINT, METEXT or CAT)
	 * @param lineStr the line of the file
	 * @param lineIndex the number of the read line
	 * @throws Exception
	 */
	private void readLine(String flag, String lineStr, int lineIndex) throws Exception{

		if(flag == null){		//if flag isn't defined in the file
			throw new Exception("FORMAT ERROR: line "+lineIndex+"\nFlag not defined("+REVERSIBLE_REACTIONS_DEFAULT_IDENTIFIER+", "+INTERNAL_METABOLITES_DEFAULT_IDENTIFIER+", "+EXTERNAL_METABOLITES_DEFAULT_IDENTIFIER+" or "+REACTION_CATALYSIS_DEFAULT_IDENTIFIER+")");
		}
		if(flag.equals(REVERSIBLE_REACTIONS_DEFAULT_IDENTIFIER)){
			treatString(lineStr, getReversibleReactions());
		}else if(flag.equals(IRREVERSIBLE_REACTIONS_DEFAULT_IDENTIFIER)){
			treatString(lineStr, getIrreversibleReactions());
			
		}else if(flag.equals(INTERNAL_METABOLITES_DEFAULT_IDENTIFIER)){
			treatString(lineStr, getInternalMetabolites());
			
		}else if(flag.equals(EXTERNAL_METABOLITES_DEFAULT_IDENTIFIER)){
			treatString(lineStr, getExternalMetabolites());
			
		}else if(flag.equals(REACTION_CATALYSIS_DEFAULT_IDENTIFIER)){
			treatReactionStep1(lineStr, lineIndex);
			
		}else		//unknown flag
			throw new Exception("FORMAT ERROR: line "+lineIndex+"\nUnknown keyword["+flag+"]\n");
	
	}
	
	/**
	 * Verifies if the metatool file has any incongruity, throwing an Exception if it does
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void verifyMetatoolFile() throws Exception {
		//Verifies if the file has any reactions
		if(reversibleReactions.size() + irreversibleReactions.size() == 0){
			throw new Exception("The metatool file doesn't have any reactions\n");
		}
		
		//Verifies if the file has any internal metabolites
		if(internalMetabolites.size()==0){
			throw new Exception("The metatool file doesn't have any internal metabolites\n");
		}
		
		//Verifies if the file has any drains
		if(externalMetabolites.size()==0){
			throw new Exception("The metatool file doesn't have any external metabolites\n");
		}
		
		//Verifies if the reactionId's at ENZREV and EZNIRREV are at CAT
		for(String revReaction : reversibleReactions){
			if(!reactionSet.containsKey(revReaction)){
				throw new Exception("The reaction ["+revReaction+"] at "+REVERSIBLE_REACTIONS_DEFAULT_IDENTIFIER+" doesn't exist on "+REACTION_CATALYSIS_DEFAULT_IDENTIFIER+"\n");
			}
		}
		for(String irrevReaction : irreversibleReactions){
			if(!reactionSet.containsKey(irrevReaction)){
				throw new Exception("The reaction ["+irrevReaction+"] at "+IRREVERSIBLE_REACTIONS_DEFAULT_IDENTIFIER+" doesn't exist on "+REACTION_CATALYSIS_DEFAULT_IDENTIFIER+"\n");
			}
		}
		Set<String> metaboliteInReactions = new TreeSet<String>();
		Set<String> allDeclaredMet = new HashSet<String>(externalMetabolites);
		allDeclaredMet.addAll(internalMetabolites);
		
		//Verifies if the reactionId's at CAT are on ENZREV or EZNIRREV
		for(String r : reactionSet.keySet()){
			if(reversibleReactions.contains(r) && irreversibleReactions.contains(r)){
				throw new Exception("The reaction ["+r+"] at "+REACTION_CATALYSIS_DEFAULT_IDENTIFIER+" appears in both "+REVERSIBLE_REACTIONS_DEFAULT_IDENTIFIER+" and "+IRREVERSIBLE_REACTIONS_DEFAULT_IDENTIFIER+"\n");
			}
			if(!reversibleReactions.contains(r) && !irreversibleReactions.contains(r)){
				throw new Exception("The reaction ["+r+"] at "+REACTION_CATALYSIS_DEFAULT_IDENTIFIER+" doesn't appear neither in "+REVERSIBLE_REACTIONS_DEFAULT_IDENTIFIER+" or "+IRREVERSIBLE_REACTIONS_DEFAULT_IDENTIFIER+"\n");
			}
			
			//Verifies if all the reactions at CAT are consistent and if all the metabolites are at METINT or METEXT
			ReactionCI reaction = reactionSet.get(r);
			Set<String> metInReaction = new HashSet<String>(reaction.getReactants().keySet());
			metInReaction.addAll(reaction.getProducts().keySet());
			
			metaboliteInReactions.addAll(metInReaction);
			
			Set<String> dif = (Set<String>) CollectionUtils.getSetDiferenceValues(metInReaction, allDeclaredMet);
			
			if(dif.size()>0)
				throw new Exception("The metabolites " + dif + " in reaction " + r + " are not declared");
	
		}

		Set<String> dif = (Set<String>) CollectionUtils.getSetDiferenceValues(internalMetabolites, metaboliteInReactions);
		if(dif.size()>0) throw  new Exception("The metabolites "+dif+" at "+INTERNAL_METABOLITES_DEFAULT_IDENTIFIER+" doesn't appear in any reaction at "+REACTION_CATALYSIS_DEFAULT_IDENTIFIER+"\n");
		
		dif = (Set<String>) CollectionUtils.getSetDiferenceValues(externalMetabolites, metaboliteInReactions);
		if(dif.size()>0) throw  new Exception("The metabolites "+dif+" at "+EXTERNAL_METABOLITES_DEFAULT_IDENTIFIER+" doesn't appear in any reaction at "+REACTION_CATALYSIS_DEFAULT_IDENTIFIER+"\n");
		
	}

	/**
	 * Gets a reaction in a line
	 * @param reactionName the name of the reaction
	 * @param reaction the reaction
	 * @param line the line number
	 * @return a ReactionCI object with the reaction
	 * @throws Exception
	 */
	private ReactionCI getReaction(String reactionName, String reaction, int line) throws Exception{
		
		//Divides the equation in the "=", obtaining the reactants (index 0) and products (index 1)
		String[] equality = reaction.split("=");
		
		ReactionCI reactionCI = null;
		if(equality.length == 2 && !equality[0].equals("") && !equality[1].equals("")){		//if the equation has a valid format
			String[] reactants = equality[0].split("\\+");		//array with the reactants
			String[] products = equality[1].split("\\+");		//array with the products
				
			Map<String,StoichiometryValueCI> reactantsMap = getStoiqValues(reactants, line);
			Map<String,StoichiometryValueCI> productsMap = getStoiqValues(products, line);
			
			for(String r : reactantsMap.keySet())
				for(String p : productsMap.keySet())
					if(r.equals(p))
						throw new Exception("ERROR: line "+line+"\nThe reaction "+reactionName+" has the same metabolite ("+r+") with the same stoichiometric value in both reactants and products.");
			
			boolean isReversible = true;		
			
			reactionCI = new ReactionCI(reactionName,reactionName, isReversible, reactantsMap, productsMap);
			reactionCI.setProducts(productsMap);
			reactionCI.setReactants(reactantsMap);
		}else
			throw new Exception("FORMAT ERROR: line "+line+"\nExpected ... : ... = ..." );
		return reactionCI;
	}
	
	/**
	 * Gets the stoichiometric value of a metabolite in a certain reaction
	 * @param values an array with some elements of an equation (eg: [stoichiometric value]? metabolite)
	 * @param line the line number
	 * @return a map which keys are the metabolite name and the values are the corresponding stoichiometric value (represented by a StoichiometricValueCI object) 
	 * @throws Exception
	 */
	private Map<String, StoichiometryValueCI> getStoiqValues(String[] values, int line) throws Exception{
		Map<String,StoichiometryValueCI> ret = new HashMap<String,StoichiometryValueCI>();
		
		for(String value : values){
			value = value.trim();
			String tokens[] = value.split("\\s+");	//divides each element of the equation by spaces (eg: the element "3 H2O" will be "3" and "H2O")
			double stoich = 1.0;
			String valueName;
			if(tokens.length==1 && !tokens[0].trim().equals("")){	//doesn't have stoichiometric value. in other words, the stoichiometric value is 1
				valueName = tokens[0].trim();
			}
			else if(tokens.length==2){								//has stoichiometric value
				stoich = Double.parseDouble(tokens[0].trim());
				valueName = tokens[1].trim();
			}else{
				throw new Exception("FORMAT ERROR: line"+line+"\nExpected ... : (\\d+)? [metabolite id] + ... = (\\d+)? [metabolite id] + ...  ");
			}
			
			
			
			String comp_id;
			if(internalMetabolites.contains(valueName)) comp_id = DEFAULT_METATOOL_INTERNAL_COMPARTMENT;
			else comp_id = DEFAULT_METATOOL_EXTERNAL_COMPARTMENT;
			
			ret.put(valueName, new StoichiometryValueCI(valueName, stoich, comp_id));
		}
		
		return ret;
	}
	
	/**
	 * This method is the first to act in the reaction string from the file (in other words, it treats the lines with the CAT flag)
	 * @param reaction the line from the file with the reaction
	 * @param line the line number
	 * @throws Exception
	 */
	private void treatReactionStep1(String reaction, int line) throws Exception{
		
		
		if(!reaction.contains(":"))
			throw new Exception("FORMAT ERROR: line "+line+"\nExpected ^[reaction_id] : ... ");
		
		String tokens [] = reaction.split("\\s*:\\s*");
		String token0 = tokens[0].trim();
		String token1 = tokens[1].replaceAll("\\.$", "").trim();		//ignore the final dot at some reactions
		
		if(tokens.length==2 && !token0.equals("") && !token1.equals("")){
			ReactionCI reactionData = getReaction(token0, token1, line);
			reactionSet.put(token0, reactionData);
//			if(reactionData.getId().equals("R587"))
//				System.out.println("Reaccao R587: "+reactionData.getReactants().keySet()+" ---- "+reactionData.getProducts().keySet());
//			if(reactionData.getId().equals("R607"))
//				System.out.println("Reaccao R607: "+reactionData.getReactants().keySet()+" ---- "+reactionData.getProducts().keySet());
//			if(reactionData.getId().equals("R732"))
//				System.out.println("Reaccao R732: "+reactionData.getReactants().keySet()+" ---- "+reactionData.getProducts().keySet());
		}else{		//the string line doesn't have the character ":" dividing the rection name and the reaction itself
			throw new Exception("FORMAT ERROR: line "+line+"\nExpected ^[reaction_id] : [reactants] = [products] ");
		}
	}
	
	/**
	 * This method treats the lines with ENZREV, ENZIRREV, METINT or METEXT flags
	 * @param string the line of the file
	 * @param list the list which will be populated with the information from the string (first argumet)
	 */
	private void treatString(String string,Collection<String> list){
		
//		NOTE: Confirm this pattern
		String[] tokens = string.split("\\s+"); // white spaces
		for(String tok : tokens)
			if(tok.compareTo("") != 0)
				list.add(tok);			
	}
		
	/**
	 * @return A list with the reversible reactions
	 */
	public ArrayList<String> getReversibleReactions() {
		return reversibleReactions;
	}

	/**
	 * @param reversibleReactions A list with the reversible reactions to be set
	 */
	public void setReversibleReactions(ArrayList<String> reversibleReactions) {
		this.reversibleReactions = reversibleReactions;
	}

	/**
	 * @return A list with the irreversible reactions
	 */
	public ArrayList<String> getIrreversibleReactions() {
		return irreversibleReactions;
	}

	/**
	 * @param irreversibleReactions A list with the irreversible reactions to be set
	 */
	public void setIrreversibleReactions(ArrayList<String> irreversibleReactions) {
		this.irreversibleReactions = irreversibleReactions;
	}

	/**
	 * @return A set with the internal metabolites
	 */
	public Set<String> getInternalMetabolites() {
		return internalMetabolites;
	}

	/**
	 * @param internalMetabolites A set with the internal metabolites to be set
	 */
	public void setInternalMetabolites(Set<String> internalMetabolites) {
		this.internalMetabolites = internalMetabolites;
	}

	/**
	 * @return A set with the external metabolites
	 */
	public Set<String> getExternalMetabolites() {
		return externalMetabolites;
	}

	/**
	 * @param externalMetabolites A set with the external metabolites to be set
	 */
	public void setExternalMetabolites(Set<String> externalMetabolites) {
		this.externalMetabolites =externalMetabolites;
	}

	/**
	 * @return The model name
	 */
	@Override
	public String getModelName() {
		return "";
	}

	/**
	 * @return The organism name
	 */
	@Override
	public String getOrganismName() {
		return null;
	}

	/**
	 * @return The notes
	 */
	@Override
	public String getNotes() {
		return "converted by metabolic package - OptFlux2 Project";
	}

	/**
	 * @return The model verion
	 */
	@Override
	public Integer getVersion() {
		return null;
	}

	/**
	 * @return A map with the compartments
	 */
	@Override
	public Map<String, CompartmentCI> getCompartments() {
		return compartmentSet;
	}

	/**
	 * @return A map with the metabolites
	 */
	@Override
	public Map<String, MetaboliteCI> getMetabolites() {
		return metaboliteSet;
	}
	
	/**
	 * @return A map with the reactions
	 */
	@Override
	public Map<String, ReactionCI> getReactions() {
		return reactionSet;
	}

	/**
	 * @return A map with the genes
	 */
	@Override
	public Map<String, GeneCI> getGenes() {
		return null;
	}

	/**
	 * @return The biomass ID
	 */
	@Override
	public String getBiomassId() {
		return biomassId ;
	}

	/**
	 * @return A map with the environmental conditions
	 */
	@Override
	public Map<String, ReactionConstraintCI> getDefaultEC() {
		return null;
	}

	/**
	 * @return The default external compartment
	 */
	@Override
	public String getExternalCompartmentId() {
		return DEFAULT_METATOOL_EXTERNAL_COMPARTMENT;
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

	

}
