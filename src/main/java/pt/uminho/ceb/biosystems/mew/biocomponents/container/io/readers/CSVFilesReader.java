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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneReactionRuleCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionTypeEnum;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.interfaces.IContainerBuilder;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.TreeUtils;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.parser.ParseException;


public class CSVFilesReader implements IContainerBuilder
{
	//names that will appear in the HeadedTable, in the CSV Configurations Panel
	public static String NOTHING = "---";
	public static String METID = "ID";
	public static String METNAME = "Name";
	public static String METFORMULA = "Formula";
	public static String REACID = "ID";
	public static String REACNAME = "Name";
	public static String REACEQUATION = "Equation";
	public static String REACECNUMBER = "EC Number";
	public static String REACGENERULE = "Gene Rule";
	public static String REACSUBSYSTEM = "Subsystem";
	public static String REACLB = "Lower Bound";
	public static String REACUB = "Upper Bound";
	public static final String EXTRAINFOID = "Extra Info";
	
	private static final long serialVersionUID = 1L;
	public static final double DEFAULT_UPPER_LIMIT = 10000.0;
	public static final double DEFAULT_LOWER_LIMIT = -10000.0;
	
	protected double USER_LOWER_LIMIT = DEFAULT_LOWER_LIMIT;
	protected double USER_UPPER_LIMIT = DEFAULT_UPPER_LIMIT;
		
	public String USER_REACTIONS_DELIMITER;
	public String USER_METABOLITES_DELIMITER;
	
	private Map<String, Integer> metIndexes;
	private Map<String, Integer> reacIndexes;
	private boolean metHasHeaders;
	private boolean reacHasHeaders;
	
	protected File reactionsFile;
	protected File metabolitesFile;
	protected String modelID;

	protected List<Integer> listExternalMetabolites = null; 
	
	protected IndexedHashMap<String,MetaboliteCI> compoundsHash;
	protected IndexedHashMap<String,ReactionCI> reactionsHash;
	private IndexedHashMap<String,String[]> equationsHash; // values[0]-compartment; values[1]-left; values[2]-right
	protected HashMap<String, CompartmentCI> compartments; 

	protected IndexedHashMap<String,String[]> reactantsHash; // reaction to reactants
	protected IndexedHashMap<String,String[]> productsHash; // reaction to products
	
	protected IndexedHashMap<String,GeneReactionRuleCI> geneReactionRules;
	protected IndexedHashMap<String, GeneCI> geneSet;
	protected HashMap<String,ArrayList<String>> geneReactionMapping;
	protected boolean hasGeneReactioAssociations = false;
	protected Map<String, Map<String, String>> metabolitesExtraInfo;
	protected Map<String, Integer> metabolitesExtraInfoIndex;
	protected Map<String, Map<String, String>> reactionsExtraInfo;
	protected Map<String, Integer> reactionsExtraInfoIndex;
	
	public boolean debug = false;
	
	private String modelName;
	private String organismName;
	private String notes;
	private int version;
	private String biomassId;
	private IndexedHashMap<String, ReactionConstraintCI> defaultEC = new IndexedHashMap<String, ReactionConstraintCI>();
	private String externalCompartmentId;

	public static Set<String> metaboliteInfoID = new TreeSet<String>(Arrays.asList(NOTHING, METID, METNAME, METFORMULA));
	public static Set<String> reactionInfoID = new TreeSet<String>(Arrays.asList(NOTHING, REACID, REACNAME, REACEQUATION, REACECNUMBER, REACGENERULE, REACSUBSYSTEM, REACLB, REACUB));
	
	public CSVFilesReader(String reactionsFilePath, Map<String, Integer> reacIndexes, String reactSep, Map<String, Integer> reacExtraInfo, boolean metHasHeaders, boolean reacHasHeaders) throws Exception{
		this(reactionsFilePath, null, null, reacIndexes,"", "", "", "", 0, "", "", null, reactSep, null, reacExtraInfo, metHasHeaders, reacHasHeaders);
	}
	
	public CSVFilesReader(String reactionsFilePath, String metabolitesFilePath, Map<String, Integer> metIndexes, Map<String, Integer> reacIndexes, String modelID, String modelName, String organismName, String notes, int version, String biomassId, String externalCompartmentId, String metSep, String reactSep, Map<String, Integer> metExtraInfo, Map<String, Integer> reacExtraInfo, boolean metHasHeaders, boolean reacHasHeaders) throws Exception{
		this.reactionsFile = new File(reactionsFilePath);
		this.metabolitesFile = new File(metabolitesFilePath);
		if(reactionsFile != null && !reactionsFile.exists()) throw new Exception("The file "+reactionsFile.getAbsolutePath()+" does not exist!");
		if(metabolitesFile != null && !metabolitesFile.exists()) throw new Exception("The file "+metabolitesFile.getAbsolutePath()+" does not exist!");
		this.metIndexes = metIndexes;
		this.reacIndexes = reacIndexes;
		this.metHasHeaders = metHasHeaders;
		this.reacHasHeaders = reacHasHeaders;
		this.modelID = modelID;
		this.modelName = modelName;
		this.organismName = organismName;
		this.notes = notes;
		this.version = version;
		this.biomassId = biomassId;
		this.externalCompartmentId = externalCompartmentId;
		this.USER_METABOLITES_DELIMITER = metSep;
		this.USER_REACTIONS_DELIMITER = reactSep;
		this.metabolitesExtraInfoIndex = metExtraInfo;
		this.reactionsExtraInfoIndex = reacExtraInfo;
		
		populateInformation();
	}
	
	public CSVFilesReader(File reactionsFile, File metabolitesFile, Map<String, Integer> metIndexes, Map<String, Integer> reacIndexes, String modelID, String modelName, String organismName, String notes, int version, String biomassId, String externalCompartmentId, String metSep, String reactSep, Map<String, Integer> metExtraInfo, Map<String, Integer> reacExtraInfo, boolean metHasHeaders, boolean reacHasHeaders) throws Exception{
		if(reactionsFile != null && !reactionsFile.exists()) throw new Exception("The file "+reactionsFile.getAbsolutePath()+" does not exist!");
		if(metabolitesFile != null && !metabolitesFile.exists()) throw new Exception("The file "+metabolitesFile.getAbsolutePath()+" does not exist!");
		this.reactionsFile = reactionsFile;
		this.metabolitesFile = metabolitesFile;
		this.metIndexes = metIndexes;
		this.reacIndexes = reacIndexes;
		this.metHasHeaders = metHasHeaders;
		this.reacHasHeaders = reacHasHeaders;
		this.modelID = modelID;
		this.modelName = modelName;
		this.organismName = organismName;
		this.notes = notes;
		this.version = version;
		this.biomassId = biomassId;
		this.externalCompartmentId = externalCompartmentId;
		this.USER_METABOLITES_DELIMITER = metSep;
		this.USER_REACTIONS_DELIMITER = reactSep;
		this.metabolitesExtraInfoIndex = metExtraInfo;
		this.reactionsExtraInfoIndex = reacExtraInfo;
		
		populateInformation();
	}
	
	/**
	 * This method populates the extra info
	 */
	private void populateExtraInfo(){
		metabolitesExtraInfo = new HashMap<String, Map<String,String>>();
		for(String s : metabolitesExtraInfoIndex.keySet()){
			metabolitesExtraInfo.put(s, new HashMap<String, String>());
		}
		
		reactionsExtraInfo = new HashMap<String, Map<String,String>>();
		for(String s : reactionsExtraInfoIndex.keySet()){
			reactionsExtraInfo.put(s, new HashMap<String, String>());
		}
	}
	
	/**
	 * This method populates the information
	 * @throws Exception
	 */
	private void populateInformation() throws Exception {
		populateExtraInfo();
		loadMetabolites();
		loadReactions();
		verifyFile();
	}

	/**
	 * This method does some verification operations in the CSV file
	 * @throws Exception
	 */
	private void verifyFile() throws Exception {
		boolean found = false;
		for(String metId : compoundsHash.keySet()){
			found = false;
			for(String reactionId : reactionsHash.keySet()){
				if(reactionsHash.get(reactionId).getReactants().keySet().contains(metId) || reactionsHash.get(reactionId).getProducts().keySet().contains(metId))
					found = true;
			}
			if(!found)
				throw new Exception("The metabolite "+metId+" defined in the metabolites file "+metabolitesFile.getName()+" is not being used in any reaction on reactions file "+reactionsFile.getName());
		}
	}

	/**
	 * This method reads the metabolites from the metabolites file
	 * @throws Exception
	 */
	public void loadMetabolites() throws Exception{

		if (debug) System.out.println("Loading metabolites from file...");

		FileReader f = new FileReader(metabolitesFile);
	
		int line = 0;
		int compoundIdIndex = metIndexes.get(METID);
		int compoundNameIndex = metIndexes.get(METNAME);
		int compoundFormulaIndex = -1;
		if(metIndexes.containsKey(METFORMULA)) compoundFormulaIndex = metIndexes.get(METFORMULA);
		
		BufferedReader r = new BufferedReader(f);
		String str = "";
		if(metHasHeaders){
			line++;
			r.readLine();	//to advance the headers line
		}

		if (compoundIdIndex<0) throw new Exception("No compound id field in line "+ line);
		if (compoundNameIndex<0) throw new Exception("No compound name field in line "+ line);

		this.compoundsHash = new IndexedHashMap<String, MetaboliteCI>();
		
		compartments = new HashMap<String, CompartmentCI>();
		
		while(r.ready()){
			str = r.readLine();
			line++;
			
			String[] fields = str.split(this.USER_METABOLITES_DELIMITER);
			int size = fields.length;
			String compoundId = "", compoundName = "", compoundFormula = "";
			
			//This checks if the split didn't omit the tokens, in case that the selected columns are empty and are the last of the file
			if(size > compoundIdIndex) compoundId = treatString(fields[compoundIdIndex]);
			if(size > compoundNameIndex) compoundName = fields[compoundNameIndex];
			if(compoundFormulaIndex >= 0 && size > compoundFormulaIndex) compoundFormula = fields[compoundFormulaIndex];
			for(String s : metabolitesExtraInfoIndex.keySet()){
				if(size > metabolitesExtraInfoIndex.get(s))
					metabolitesExtraInfo.get(s).put(compoundId, fields[metabolitesExtraInfoIndex.get(s)]);
				else
					metabolitesExtraInfo.get(s).put(compoundId, "");
			}
			
			//verifies if the current metabolite already exists in the metabolites structure
			if(compoundsHash.containsKey(compoundId)) throw new Exception("The metabolite "+compoundId+" is repeated at the metabolites file "+metabolitesFile.getName()+", at line "+line);

			
			MetaboliteCI m = new MetaboliteCI(compoundId, compoundName);
			if(!compoundFormula.equals("")) m.setFormula(compoundFormula);

			validateMetabolite(m, compoundId, line);
			
			if (debug) System.out.println("Added compound Id" + compoundId + " - " + compoundName);
		}
		r.close();
		f.close();
		if (debug) System.out.println("Done reading metabolites file with: " + compoundsHash.size());
		
//		CompartmentCI c = new CompartmentCI(externalCompartmentId, externalCompartmentId, externalCompartmentId);
//		compartments.put(c.getId(), c);
		
	}
	
	/**
	 * This method does some validation operations in each metabolite and, if it is valid, adds it to the corresponding compartment
	 * @param m A MetaboliteCI object
	 * @param compoundId The metabolite ID
	 * @param line the line number
	 * @throws Exception
	 */
	public void validateMetabolite(MetaboliteCI m, String compoundId, int line) throws Exception{
		String[] metInfo = compoundId.split("\\[");
		//The metabolite ID can only have at most 1'[' 
		if(countOpen(compoundId) > 1) throw new Exception("There must be only one '[...]' at each metabolite, but there is more than one on the metabolite "+compoundId+", at line "+line);
		//If there are more '[' than ']', then some ']' are missing
		if(countOpen(compoundId) > countClose(compoundId)) throw new Exception("A ']' is missing at metabolite "+compoundId+", at line "+line);
		//If there are more ']' than '[', then some '[' are missing
		if(countOpen(compoundId) < countClose(compoundId)) throw new Exception("A '[' is missing at metabolite "+compoundId+", at line "+line);
		else{
			if(metInfo.length == 2){
				String[] comp = metInfo[1].split("\\]");
				if(compartments.containsKey(comp[0])) compartments.get(comp[0]).addMetaboliteInCompartment(compoundId);
				else{
					CompartmentCI c = new CompartmentCI(comp[0], comp[0], comp[0]);
					c.addMetaboliteInCompartment(compoundId);
					compartments.put(c.getId(), c);
				}
			}
			else{
				compoundId += "[c]";
				m.setId(compoundId);
				if(compartments.containsKey("c")) compartments.get("c").addMetaboliteInCompartment(compoundId);
				else{
					CompartmentCI c = new CompartmentCI("c", "c", "c");
					c.addMetaboliteInCompartment(compoundId);
					compartments.put(c.getId(), c);
				}
			}
			compoundsHash.put(compoundId, m);
		}
	}

	
	/**
	 * This method counts how many times the character ']' appears in the metabolite ID
	 * @param compoundId The metabolite ID
	 * @return The number of times that the character ']' appears
	 */
	public int countClose(String compoundId) {
		int count = 0;
		for(int i=0; i<compoundId.length(); i++){
			if(compoundId.charAt(i) == ']') count++;
		}
		return count;
	}
	
	/**
	 * This method counts how many times the character '[' appears in the metabolite ID
	 * @param compoundId The metabolite ID
	 * @return The number of times that the character '[' appears
	 */
	public int countOpen(String compoundId) {
		int count = 0;
		for(int i=0; i<compoundId.length(); i++){
			if(compoundId.charAt(i) == '[') count++;
		}
		return count;
	}

	/**
	 * This method reads the reactions from the reactions file
	 * @throws Exception
	 */
	public void loadReactions () throws Exception{
	
		if (debug) System.out.println("Loading reactions from file...");

		FileReader f = new FileReader(reactionsFile);
		BufferedReader r = new BufferedReader(f);
		String str = new String("");

		int line = 0;
		
		int reactionIdIndex = reacIndexes.get(REACID);
		int reactionNameIndex = reacIndexes.get(REACNAME);
		int reactionEquationIndex = reacIndexes.get(REACEQUATION);
		int reactionECNumberIndex = -1;
		int reactionGeneIndex = -1;
		int reactionSubsystemIndex = -1;
		int reactionLBIndex = -1;
		int reactionUBIndex = -1;
		
		if(reacIndexes.containsKey(REACECNUMBER)) reactionECNumberIndex = reacIndexes.get(REACECNUMBER);
		if(reacIndexes.containsKey(REACGENERULE)) reactionGeneIndex = reacIndexes.get(REACGENERULE);
		if(reacIndexes.containsKey(REACSUBSYSTEM)) reactionSubsystemIndex = reacIndexes.get(REACSUBSYSTEM);
		if(reacIndexes.containsKey(REACLB)) reactionLBIndex = reacIndexes.get(REACLB);
		if(reacIndexes.containsKey(REACUB)) reactionUBIndex = reacIndexes.get(REACUB);
		
		if(reacHasHeaders){
			line++;
			r.readLine();

		}
		
		//Mandatory headers
		if (reactionNameIndex<0) throw new Exception("No reaction name field in line " + line);
		if (reactionIdIndex<0) throw new Exception("No reaction id field in line " + line);
		if (reactionEquationIndex<0) throw new Exception("No reaction equation field in line "+ line);
		if (reactionGeneIndex<0) {
			this.hasGeneReactioAssociations = false;
			if (debug) System.out.println("No reaction lines detected");
		}
		
		this.reactionsHash = new IndexedHashMap<String, ReactionCI>();
		this.equationsHash = new IndexedHashMap<String, String[]>();
		
		while(r.ready()){
			str = r.readLine();
			line++;

			String[] fields = str.split(this.USER_REACTIONS_DELIMITER);
			for(int j=0; j<fields.length; j++){
				fields[j] = treatString(fields[j]);
			}
			int size = fields.length;
			String reactionId = "", reactionName = "", equation = "", ecnumber = "", subsystem ="" , lowerB = "", upperB = "";
			
			if(size > reactionNameIndex) reactionName = fields[reactionNameIndex];
			if(size > reactionIdIndex) reactionId = fields[reactionIdIndex];
			if(size > reactionEquationIndex) equation = fields[reactionEquationIndex];
			
			if(reactionECNumberIndex >= 0 && size > reactionECNumberIndex) ecnumber = fields[reactionECNumberIndex];
			if(reactionSubsystemIndex >= 0 && size > reactionSubsystemIndex) subsystem = fields[reactionSubsystemIndex];
			if(reactionLBIndex >= 0 && size > reactionLBIndex) lowerB = fields[reactionLBIndex];
			if(reactionUBIndex >= 0 && size > reactionUBIndex) upperB = fields[reactionUBIndex];
			for(String s : reactionsExtraInfoIndex.keySet()){
				if(size > reactionsExtraInfoIndex.get(s))
					reactionsExtraInfo.get(s).put(reactionId, fields[reactionsExtraInfoIndex.get(s)]);
				else
					reactionsExtraInfo.get(s).put(reactionId, "");
			}
			
			String[] parseResults = parseReactionEquation(equation, line);			//0: general compartment; 1: left; 2: right; 3: reversible or irreversible
			if(lowerB.equals("") || upperB.equals("")){
				if(parseResults[3].equals("R"))	lowerB = "-10000";
				else lowerB = "0";
				upperB = "10000";
			}
			
			double lb, ub;
			try{
				lb = Double.parseDouble(lowerB);
			} catch (Exception e){
				throw new Exception("ERROR! line "+line+"\nExpected a number at column "+reactionLBIndex+" (lower bounds column)");
			}
			
			try{
				ub = Double.parseDouble(upperB);
			} catch (Exception e){
				throw new Exception("ERROR! line "+line+"\nExpected a number at column "+reactionUBIndex+" (upper bounds column)");
			}
			
			ReactionConstraintCI rc = new ReactionConstraintCI(lb, ub);
			defaultEC.put(reactionId, rc);
			
			ReactionCI reaction = new ReactionCI(reactionId, reactionName, parseResults[3].equals("R"), getReactantsOrProducts(parseResults[1], parseResults[0], reactionId, line), getReactantsOrProducts(parseResults[2], parseResults[0], reactionId, line));
			reaction.setType(ReactionTypeEnum.Internal);
			if(!subsystem.equals("")) reaction.setSubsystem(subsystem);
			reaction.setEc_number(ecnumber);
			
			reactionsHash.put(reactionId, reaction);
			equationsHash.put(reactionId, parseResults);
			
			if(debug) 
				System.out.println("Added reaction: " + reactionId + " name: " + reactionName + " eqn: " + equation);
			
			if (this.hasGeneReactioAssociations)
				reacHasGeneReacAssociation(fields, reactionGeneIndex, reactionId);
			
		}
		
		if (debug) System.out.println("Loaded reactions file with: " + reactionsHash.size());
		
		r.close();
		f.close();
	}
	
	/**
	 * This method handles with reactions that have geneReactions associations (gene rules)
	 * @param fields An array of Strings with the data of each line
	 * @param reactionGeneIndex The index of the geneRules column in the reactions file
	 * @param reactionId The reaction ID
	 * @throws ParseException
	 */
	public void reacHasGeneReacAssociation(String[] fields, int reactionGeneIndex, String reactionId) throws ParseException{
		if( (fields.length > reactionGeneIndex)){
			
			String geneAssociation = fields[reactionGeneIndex];
			GeneReactionRuleCI geneReactionRule = new GeneReactionRuleCI(geneAssociation);
			geneReactionRules.put(reactionId, geneReactionRule);
			
			ArrayList<String> genesId = TreeUtils.withdrawVariablesInRule(geneReactionRule.getRule());
			for(int g=0; g<genesId.size();g++){
				if(!geneSet.containsKey(genesId.get(g))){
					geneSet.put(genesId.get(g), new GeneCI(genesId.get(g),genesId.get(g)));
					if (debug) System.out.println(genesId.get(g));
				}
				
				if(geneReactionMapping.containsKey(genesId.get(g))){
					ArrayList<String> dependentReactions = geneReactionMapping.get(genesId.get(g));
					dependentReactions.add(reactionId);
					
				}
				else{
					ArrayList<String> dependentReactions = new ArrayList<String>();
					dependentReactions.add(reactionId);
					geneReactionMapping.put(genesId.get(g), dependentReactions);
				}
			}
		}
	}
	
	/**
	 * Parses an equation
	 * @return 0: general compartment (if none, null);
	 * 1: left side of the equation;
	 * 2: right side of the equation;
	 * 3: reversible or irreversible
	 */
	private String[] parseReactionEquation (String equation, int line) throws Exception
	{
		String[] res = new String[4];
		String sep = "";
		
		if (equation.matches(".* --> .*")){
			res[3] = "I";
			sep = "-->";
		}
		else if (equation.matches(".* => .*")){
			res[3] = "I";
			sep = "=>";
		}
		else if (equation.matches(".* <==> .*")) {
			res[3] = "R";
			sep = "<==>";
		}
		else if (equation.matches(".* <=> .*")) {
			res[3] = "R";
			sep = "<=>";
		}
		else if (equation.matches(".* -> .*")){
			res[3] = "I";
			sep = "->";
		}
		else if (equation.matches(".* <-> .*")){
			res[3] = "R";
			sep = "<->";
		}
		else throw new Exception("Equation isn't correctly defined (no -->, =>, <==>, <=>, -> or <->) in line "+ line);
		
		String[] parseRes = equation.split(sep);	
		String left = parseRes[0].trim();
		
		if (parseRes.length > 1)	//isn't a drain
			res[2] = parseRes[1].trim();
		else res[2] = "";
		
		if (left.charAt(0)=='['){		//if the equation has a general compartment. In other words, if the equation is like this:   [compartment]: left_side -> right_side
			String[] x = left.split("\\s*:\\s*");
			
			//if it has a general compartment, then it must have the character ':', dividing the [compartment] and the equation itself
			if(x.length != 2) throw new Exception("FORMAT ERROR: reactions file ["+reactionsFile.getName()+"], line "+line+"\n Expected [general compartment] : ...\n");
				
			res[0] = x[0].substring(1, 2);
			res[1] = x[1];
		}
		else {
			res[0] = null;
			res[1] = left;
		}
		return res;
	}

	/**
	 * This method receives the a part of the equation (left, reactants, or right, products) and parses it, giving a structure with all the metabolites and its stoichiometric value in the corresponding part of the equantion
	 * @param reactionPart A String with the left side or the right side of the equation
	 * @param generalComp The general compartment of the equation, if it exists. null if doesn't exist
	 * @param reactionId The ID of the reaction
	 * @param line The line number
	 * @return A structure with all the metabolites and its stoichiometric value in the corresponding part of the equantion
	 * @throws Exception
	 */
	public Map<String, StoichiometryValueCI> getReactantsOrProducts(String reactionPart, String generalComp, String reactionId, int line) throws Exception{
		Map<String, StoichiometryValueCI> result = new HashMap<String, StoichiometryValueCI>();
		
		//Splits the reactionPart by '+', obtaining all the metabolites and stoichiometric value, if any
		String[] rOrP = reactionPart.split("\\s*\\+\\s*");
		for(int i=0; i<rOrP.length; i++){
			if(rOrP[i].length() != 0){
				
				/**
				 * The following command parses each element of the reactionPart (each element is the stoichiometric
				 * value (if any) and the metabolite).
				 * The result is an array of Strings, where the indexes are:
				 * 0: coefficient;
				 * 1: name;
				 * 2: compartment (if none, null)
				 */
				String[] rOrPParsed = parseCompoundInEqn(rOrP[i], line);
				
				if(generalComp == null)	//if there isn't any general compartment, then the metabolite name is metaboliteId[metaboliteCompartment]
					putMetabolite(rOrPParsed[0], rOrPParsed[1], rOrPParsed[2], reactionId, result, line);
				else		//if there is a general compartment, then the metabolite name is metaboliteId[generalCompartment]
					putMetabolite(rOrPParsed[0], rOrPParsed[1], generalComp, reactionId, result, line);
			}
		}
		
		return result;
	}
	
	/**
	 * This is an auxiliary method that puts a metabolite and its stoichiometric value into a structure
	 * @param coefficient The coefficient of the compound
	 * @param metaboliteId The metabolite Id
	 * @param compartment The metabolite compartment
	 * @param reactionId The reaction Id
	 * @param result The structur where it will be inserted the metabolite and its stoichiometric value
	 * @param line The line number
	 * @throws Exception
	 */
	public void putMetabolite(String coefficient, String metaboliteId, String compartment, String reactionId, Map<String, StoichiometryValueCI> result, int line) throws Exception{
		if(!compoundsHash.containsKey(metaboliteId+"["+compartment+"]")) throw new Exception("The metabolite "+metaboliteId+"["+compartment+"], in the reaction "+reactionId+" (line "+line+") is not defined in the metabolite file "+metabolitesFile.getName());
		Double d;
		try{d = Double.parseDouble(coefficient);}
		catch(NumberFormatException e){
			throw new Exception("FORMAT ERROR! line "+line+"\nExpected [number]? metabolite + [number]? metabolite + ...");
		}
		
		result.put(metaboliteId+"["+compartment+"]", new StoichiometryValueCI(metaboliteId+"["+compartment+"]", d, compartment));
	}
	
	/**
	 * This method gets all the reactans and products of all the reactions
	 */
	public void getReactantsAndProducts(){
		this.reactantsHash = new IndexedHashMap<String, String[]>();;
		this.productsHash = new IndexedHashMap<String, String[]>();;
		
		for(int i=0; i< this.equationsHash.size(); i++)
		{
			String reactionName = equationsHash.getKeyAt(i);
			
			String leftSide = equationsHash.getValueAt(i)[1];
			if (leftSide != null){
				
				int l = divideAndAddCompound(leftSide, reactionName, this.reactantsHash);
				if (debug) System.out.println("Adding " + l + " reactants to " + reactionName + " -> " + leftSide);
			}
			
			String rightSide = equationsHash.getValueAt(i)[2];
			if (rightSide != null){
				
				int l = divideAndAddCompound(rightSide, reactionName, this.productsHash);
				if (debug) System.out.println("Adding " + l + " products to " + reactionName + " -> " + rightSide);
			}
		}
	}
	
	/**
	 * Auxiliary method that divides the compounds by '+' and add it to a structure
	 * @param side The left or right side of the equation
	 * @param reactionName The reaction name
	 * @param structure The structure where the results will be added
	 * @return The number of compounds
	 */
	public int divideAndAddCompound(String side, String reactionName, IndexedHashMap<String, String[]> structure){
		String[] compounds = side.split("\\+");
		
		for(int k = 0;k < compounds.length;k++)
			compounds[k] = compounds[k].trim();
		
		structure.put(reactionName, compounds);
		return compounds.length;
	}
	
	
	/**
	 * Deletes the '"' from strings coming from CSV files
	 */
	private String treatString(String string){
		if(!(string==null)){
			if(string.length() > 0){
				if(string.charAt(0) == '"') string = string.substring(1);
				if(string.charAt(string.length()-1) == '"') string = string.substring(0, string.length()-1);
//				string = string.toUpperCase();
			}
		}
		return string;
	}
	
	/**
	 * Parses a compound
	 * @param text String with the coefficient (if any) and the compound
	 * @return 0: coefficient; 1:  name; 2: compartment (if none, null)
	 * @throws Exception 
	 */
	private String[] parseCompoundInEqn (String text, int line) throws Exception{
		String [] res = new String[3]; // returns coef; name; compartment
		
		if (text.trim().startsWith("(")){	//if starts with '(', then the coefficient is between '()'
			String[] cn = text.trim().substring(1).split("\\s*\\)\\s*");
			if(cn.length == 1) throw new Exception("FORMAT ERROR: reactions file ["+reactionsFile.getName()+"] line "+line+"\nA ')' is missing in the reaction equation\n");
			res[0] = cn[0];
			text = cn[1];
		}
		else{
			String[] aux = text.split("\\s+");
			if(aux.length == 1){	//if it doesn't have coefficient, or the coefficient was already read because it was between ()
				res[0] = "1.0";
			}
			else if(aux.length == 2){		//has coefficient and doesn't have "()"
				res[0] = aux[0];
				text = aux[1];
			}
			else{
				throw new Exception("FORMAT ERROR: reactions file ["+reactionsFile.getName()+"] line "+line+"\nExpected ... : (\\d+)? metabolite id + ... = (\\d+)? metabolite id + ...  ");
			}
		}
		String[] cn = text.split("\\[");		//the compartment is between []
		if(cn.length == 1){						//doesn't have []. in other words, doesn't have compartment
			res[1] = text.trim();
			res[2] = null;
		}
		else if(cn.length == 2){				//has a compartment
			res[1] = cn[0].trim();
			res[2] = cn[1].substring(0,1);
		}
		else{
			throw new Exception("FORMAT ERROR: reactions file ["+reactionsFile.getName()+"] line "+line+"\nExpected metabolite_id[compartment] + ... -> metabolite_id[compartment] + ...\n");
		}
		return res;
	}	
		
	/**
	 * @return The default upper limit
	 */
	public static double getDEFAULT_UPPER_LIMIT() {
		return DEFAULT_UPPER_LIMIT;
	}
	
	/**
	 * @return The default lower limit
	 */
	public static double getDEFAULT_LOWER_LIMIT() {
		return DEFAULT_LOWER_LIMIT;
	}

	/**
	 * @param user_lower_limit The user lower limit to set
	 */
	public void setUSER_LOWER_LIMIT(double user_lower_limit) {
		USER_LOWER_LIMIT = user_lower_limit;
	}

	/**
	 * @param user_upper_limit The user upper limit to set
	 */
	public void setUSER_UPPER_LIMIT(double user_upper_limit) {
		USER_UPPER_LIMIT = user_upper_limit;
	}
	
	/**
	 * @return A map with the gene reaction rules
	 */
	public IndexedHashMap<String, GeneReactionRuleCI> getGeneReactionRules() {
		return geneReactionRules;
	}

	/**
	 * @return A map with the gene reactions mapping
	 */
	public HashMap<String, ArrayList<String>> getGeneReactionMapping() {
		return geneReactionMapping;
	}

	/**
	 * @return A boolean telling if the file has gene reaction associations (true) or not (false)
	 */
	public boolean hasGeneReactionAssociations() {
		return hasGeneReactioAssociations;
	}
	
	/**
	 * @param id The biomassID to set
	 */
	public void setbiomassId(String id){
		this.biomassId = id;
	}

	/**
	 * @return The model name 
	 */
	@Override
	public String getModelName() {
		return modelName;
	}

	/**
	 * @return The organism name
	 */
	@Override
	public String getOrganismName() {
		return organismName;
	}

	/**
	 * @return The notes
	 */
	@Override
	public String getNotes() {
		return notes;
	}

	/**
	 * @return The version
	 */
	@Override
	public Integer getVersion() {
		return version;
	}

	/**
	 * @return A map with the reactions
	 */
	@Override
	public Map<String, ReactionCI> getReactions() {
		return reactionsHash;
	}

	/**
	 * @return A map with the metabolites
	 */
	@Override
	public Map<String, MetaboliteCI> getMetabolites() {
		return compoundsHash;
	}

	/**
	 * @return A map with the genes
	 */
	@Override
	public Map<String, GeneCI> getGenes() {
		return geneSet;
	}
	
	/**
	 * @return A map with the compartments
	 */
	@Override
	public HashMap<String, CompartmentCI> getCompartments() {
		return compartments;
	}

	/**
	 * @return The biomassID
	 */
	@Override
	public String getBiomassId() {
		return biomassId;
	}

	/**
	 * @return A map with the Environmental Conditions
	 */
	@Override
	public Map<String, ReactionConstraintCI> getDefaultEC() {
		return defaultEC;
	}

	/**
	 * @return The external compartment ID
	 */
	@Override
	public String getExternalCompartmentId() {
		return externalCompartmentId;		
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
