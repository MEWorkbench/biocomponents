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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.grammar.syntaxtree.TreeUtils;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.parser.ParseException;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneReactionRuleCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionTypeEnum;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.interfaces.IContainerBuilder;

public class NewDatabaseCSVFilesReader implements IContainerBuilder {
	private static final long serialVersionUID = 1L;
	// names that will appear in the HeadedTable, in the CSV Configurations
	// Panel
	public static final String NOTHING = "---";
	public static final String METID = "ID";
	public static final String METNAME = "Name";
	public static final String METFORMULA = "Formula";
	public static final String METCOMP = "Compartment";
	public static final String REACID = "ID";
	public static final String REACNAME = "Name";
	public static final String REACEQUATION = "Equation";
	public static final String REACECNUMBER = "EC Number";
	public static final String REACGENERULE = "Gene Rule";
	public static final String REACSUBSYSTEM = "Subsystem";
	public static final String REACLB = "Lower Bound";
	public static final String REACUB = "Upper Bound";
	public static final String EXTRAINFOID = "Extra Info";
	public static final Set<String> metaboliteInfoID = new TreeSet<String>(Arrays.asList(NOTHING, METID, METNAME,
			METFORMULA, METCOMP, EXTRAINFOID));
	public static final Set<String> reactionInfoID = new TreeSet<String>(Arrays.asList(NOTHING, REACID, REACNAME,
			REACEQUATION, REACECNUMBER, REACGENERULE, REACSUBSYSTEM, REACLB, REACUB, EXTRAINFOID));
	public static final String COMPSEPARATOR = "/";
	// Default values
	public static final double DEFAULT_UPPER_LIMIT = 10000.0;
	public static final double DEFAULT_LOWER_LIMIT = -10000.0;
	public static final String DEFAULT_COMPARTMENT = "cytosol";
	private static final String DEFAULT_EXTARENAL_COMPARTMENT = "external";
	private static final String organismName = "Database";
	private static final String biomassId = null;

	// Variables
	private String USER_REACTIONS_DELIMITER;
	private String USER_METABOLITES_DELIMITER;
	private Map<String, Integer> metIndexes;
	private Map<String, Integer> reacIndexes;
	private boolean metHasHeaders;
	private boolean reacHasHeaders;
	private File reactionsFile;
	private File metabolitesFile;
	private String modelID;
	private String modelName;
	private String notes;
	private int version;
	private IndexedHashMap<String, ReactionConstraintCI> defaultEC;
	private IndexedHashMap<String, MetaboliteCI> compoundsHashAux;
	private IndexedHashMap<String, MetaboliteCI> compoundsHashAuxFile;
	private IndexedHashMap<String, MetaboliteCI> compoundsHash;
	private IndexedHashMap<String, ReactionCI> reactionsHash;
	private HashMap<String, CompartmentCI> compartmentsHash;
	private IndexedHashMap<String, GeneReactionRuleCI> geneReactionRules;
	private IndexedHashMap<String, GeneCI> geneSet;
	private HashMap<String, ArrayList<String>> geneReactionMapping;
	private boolean hasGeneReactioAssociations = false;
	private boolean isMetabolitesFileMandatory = true;

	// Extra info
	private Map<String, Map<String, String>> metabolitesExtraInfo;
	private Map<String, Integer> metabolitesExtraInfoIndex;
	private Map<String, Map<String, String>> reactionsExtraInfo;
	private Map<String, Integer> reactionsExtraInfoIndex;

	// auxiliar
	private Map<String, Set<String>> metaCompartments;
	private Map<String, Set<String>> metaCompartmentsReac;
	private Map<String, Set<String>> metaCompartmentsReacFile;

	private HashMap<String, String> convertMetIds;
	// private HashMap<String, String> convertReacIds;
	private CSVMatcher matcher;
	private int metMatchFieldIndex = -1;

	// Regular Expressions
	// allow special character in names:" : - . "
	private static String name = "[\\w\\d\\.\\:\\(\\)\\-\\,]+";
    private static String stoic = "\\(?(\\d+(?:\\.\\d+)?(?:[eE][\\+\\-]?\\d+)?)?\\s*\\)?";
	
	//private static String stoic = "\\(?(\\d+(?:\\.\\d+)?)?\\s*\\)?"; // with or
																		// without
																		// ()
	private static String compartment = "\\[([\\w\\d\\.\\:]+)\\]";
	private static Pattern patternEquation = Pattern.compile("^\\s*(?:" + compartment + "\\s*\\:)?"
			+ "([\\d\\w\\+\\.\\*\\(\\)\\[\\]\\:\\s\\-\\,]+?)" + "(<)?((?:--?|==?)>)"
			+ "([\\d\\w\\+\\.\\*\\(\\)\\[\\]\\:\\s\\-\\,]*)\\s*$");
	private static Pattern patternCompoundEq = Pattern.compile("^\\s*(?:" + stoic + "\\s+)?\\s*(?:\\*?\\s+)?(" + name
			+ ")\\s*(?:" + compartment + ")?\\s*$");
	private static Pattern patternCompoundName = Pattern.compile("^(" + name + ")\\s*(?:" + compartment + ")?\\s*$");
	
	//Static Mandatory Fields
	private static Set<String> reactionsMandatoryFields = new TreeSet<String>(Arrays.asList(REACID, REACEQUATION));
	private static Set<String> metabolitesMandatoryFields = new TreeSet<String>(Arrays.asList(METID));
	

	// Constructor
	public NewDatabaseCSVFilesReader(File reactionsFile, File metabolitesFile, Map<String, Integer> metIndexes,
			Map<String, Integer> reacIndexes, String modelID, String modelName, String metSep, String reactSep,
			Map<String, Integer> metExtraInfo, Map<String, Integer> reacExtraInfo, boolean metHasHeaders,
			boolean reacHasHeaders, Map<String, String> matchFields, Container origContainer) throws Exception {
		
		this.reactionsFile = reactionsFile;
		this.metabolitesFile = metabolitesFile;
		if (reactionsFile != null && !reactionsFile.exists())
			throw new Exception("The file " + reactionsFile.getAbsolutePath() + " does not exist!");
		
		//if(metabolitesFile == null)	setMetabolitesFileMandatory(false);
		if(metabolitesFile != null)
		{
			if (!metabolitesFile.exists())
				throw new Exception("The file " + metabolitesFile.getAbsolutePath() + " does not exist!");
			
			this.metIndexes = metIndexes;
			this.metHasHeaders = metHasHeaders;
			this.metabolitesExtraInfoIndex = (metExtraInfo==null)? new HashMap<String, Integer>():metExtraInfo;
		}
		this.USER_METABOLITES_DELIMITER = metSep;
		
		this.reacIndexes = reacIndexes;
		this.reacHasHeaders = reacHasHeaders;
		this.USER_REACTIONS_DELIMITER = reactSep;
		this.reactionsExtraInfoIndex = (reacExtraInfo==null)? new HashMap<String, Integer>():reacExtraInfo;
		
		this.setModelID(modelID);
		this.modelName = modelName;
		this.notes = "";
		this.version = 0;
		
		this.defaultEC = new IndexedHashMap<String, ReactionConstraintCI>();
		
		if (matchFields != null) {
			matcher = new CSVMatcher(origContainer, matchFields);
			// get the index of the selected
			String f = matcher.getMetaRegExp().get("DatabaseField");
			metMatchFieldIndex = (metIndexes.containsKey(f)) ? metIndexes.get(f) : -1;
			metMatchFieldIndex = (metExtraInfo.containsKey(f)) ? metExtraInfo.get(f) : metMatchFieldIndex;
		}
		
		// inicialization of objects
		this.convertMetIds = new HashMap<String, String>();
		this.compoundsHashAux = new IndexedHashMap<String, MetaboliteCI>();
		this.compoundsHashAuxFile = new IndexedHashMap<String, MetaboliteCI>();
		this.compoundsHash = new IndexedHashMap<String, MetaboliteCI>();
		this.reactionsHash = new IndexedHashMap<String, ReactionCI>();
		this.metaCompartments = new HashMap<String, Set<String>>();
		this.metaCompartmentsReac = new HashMap<String, Set<String>>();
		this.metaCompartmentsReacFile = new HashMap<String, Set<String>>();
		this.compartmentsHash = new HashMap<String, CompartmentCI>();

		populateInformation();
	}
	
	public NewDatabaseCSVFilesReader(File reactionsFile, Map<String, Integer> reacIndexes, 
			String modelID, String modelName, String reactSep, Map<String, Integer> reacExtraInfo, 
			boolean reacHasHeaders, Map<String, String> matchFields, Container origContainer) throws Exception{
		this(reactionsFile, null, null, reacIndexes, modelID, modelName, "", reactSep, null, reacExtraInfo, false, reacHasHeaders, matchFields, origContainer);
	}

	/**
	 * This method populates the information
	 * 
	 * @throws Exception
	 */
	private void populateInformation() throws Exception {
		
		loadReactions();
		loadMetabolites();
		
		compoundsHashAux.putAll(compoundsHashAuxFile);
		metaCompartmentsReac.putAll(metaCompartmentsReacFile);
		metaCompartments.putAll(metaCompartmentsReacFile);
		
		putCompartmentsInMetaIds();
		
//		verifyFile();
	}

	// substitute the MetaboliteID for the MetaboliteID[Compartment]
	private void putCompartmentsInMetaIds() {
		for (String meta : metaCompartments.keySet()) {
			Set<String> allCompartOfMeta = metaCompartments.get(meta);
			allCompartOfMeta.addAll(metaCompartmentsReac.get(meta));
			for (String comp : allCompartOfMeta) {
				MetaboliteCI newMeta = compoundsHashAux.get(meta);
				String newMetaId = meta + "_" + comp;
				if (convertMetIds.containsKey(newMetaId)) {
					newMetaId = convertMetIds.get(newMetaId);
				}
				newMeta = newMeta.clone();
				newMeta.setId(newMetaId);
				compoundsHash.put(newMetaId, newMeta);
				if (!compartmentsHash.containsKey(comp)) {
					String external = (comp.equals(DEFAULT_COMPARTMENT) ? DEFAULT_EXTARENAL_COMPARTMENT : null);
					CompartmentCI newCompartment = new CompartmentCI(comp, comp, external);
					TreeSet<String> metaIds = new TreeSet<String>();
					metaIds.add(newMetaId);
					newCompartment.setMetabolitesInCompartmentID(metaIds);
					compartmentsHash.put(comp, newCompartment);
				} else {
					compartmentsHash.get(comp).getMetabolitesInCompartmentID().add(newMetaId);
				}
			}
		}
	}

	/**
	 * This method does some verification operations in the CSV file.
	 * Metabolites that aren't used will not be imported
	 * 
	 * @throws Exception
	 */
	// private void verifyFile() throws Exception {
	private void verifyFile() {
		System.out.println("verifyFiles");
		boolean found = false;
		Set<String> removedMetabolites = new TreeSet<String>();
		for (String metId : compoundsHash.keySet()) {
			found = false;
			for (String reactionId : reactionsHash.keySet()) {
				if (reactionsHash.get(reactionId).getReactants().keySet().contains(metId)
						|| reactionsHash.get(reactionId).getProducts().keySet().contains(metId))
					found = true;
			}
			if (!found)
				removedMetabolites.add(metId);
		}
		// remove metabolites that are not used in reactions
		for (String metId : removedMetabolites)
			compoundsHash.remove(metId);
		for (String comp : compartmentsHash.keySet()) {
			compartmentsHash.get(comp).getMetabolitesInCompartmentID().removeAll(removedMetabolites);
		}
	}

	/**
	 * This method reads the metabolites from the metabolites file.
	 * 
	 * @throws Exception
	 */
	private void loadMetabolites() throws Exception {
		System.out.println("Load Metabolites");
		if(metabolitesFile==null || !metabolitesFile.exists())
			return;
		
		FileReader f = new FileReader(metabolitesFile);
		BufferedReader r = new BufferedReader(f);
		
		metabolitesExtraInfo = new HashMap<String, Map<String, String>>();
		for (String s : metabolitesExtraInfoIndex.keySet())
			metabolitesExtraInfo.put(s, new HashMap<String, String>());
		int line = 1;
		if (metHasHeaders){
			r.readLine(); // to advance the headers line
			line++;
		}
		int compoundIdIndex = metIndexes.get(METID);
		int compoundNameIndex = getMetabolitesMandatoryFields().contains(METNAME) && (metIndexes.containsKey(METNAME)) ? metIndexes.get(METNAME) : -1;
		int compoundFormulaIndex = (metIndexes.containsKey(METFORMULA)) ? metIndexes.get(METFORMULA) : -1;
		int compoundCompartmentIndex = (metIndexes.containsKey(METCOMP)) ? metIndexes.get(METCOMP) : -1;

		String str;
		while (r.ready()) {
			str = r.readLine();
			if(str.trim().equals(""))
				//throw new Exception("The line " + line + " is blank in "+metabolitesFile.getName()+" file.");
				continue;
			// to consider the last field when it's empty
			str = (str.substring(str.length() - this.USER_METABOLITES_DELIMITER.length())
					.equals(this.USER_METABOLITES_DELIMITER)) ? str + " " : str;
			String[] fields = str.split(this.USER_METABOLITES_DELIMITER);
			for (int j = 0; j < fields.length; j++) {
				fields[j] = treatString(fields[j]);
			}
			String compoundId = "", compoundName = "", compoundFormula = "", compoundCompartment = "";
			// Mandatory fields
			compoundId = treatString(getFieldsFromSplitedArray(fields,compoundIdIndex));
			if (compoundNameIndex >= 0)
				compoundName = getFieldsFromSplitedArray(fields,compoundNameIndex);
			if (compoundId.trim().equals(""))
				throw new Exception("No compound id field in line " + line);
			if (compoundName.trim().equals(""))
				//throw new Exception("No compound name field in line " + line);
				compoundName = compoundId;

			// Optional Fields
			if (compoundFormulaIndex >= 0)
				compoundFormula = getFieldsFromSplitedArray(fields,compoundFormulaIndex);
			if (compoundCompartmentIndex >= 0)
				compoundCompartment = getFieldsFromSplitedArray(fields,compoundCompartmentIndex).trim();
			// verifies if the current metabolite already exists in the
			// metabolites structure
			if (compoundsHashAux.containsKey(compoundId))
				throw new Exception("The metabolite " + compoundId + " is repeated at the metabolites file "
						+ metabolitesFile.getName() + ", at line " + line
						+ ". If you want use the same id for more than one compartment, use a new field where "
						+ "the compartments are separated by \"/\"");

			// calculate the compartments of this compound
			Set<String> compartments = calculateCompartment(compoundId, compoundCompartment, line);
			
			// get compundID without compartment identification
			compoundId = getCompoundID(compoundId, line);

			if (metMatchFieldIndex != -1) {
				// the match function return 0: compoundId[compInModel]
				// 1:compoundId in model
				String[] m = matcher.convertMetaId(fields[this.metMatchFieldIndex], compoundId);
				if (m != null)
					convertMetIds.put(m[0], m[1]);
			}

			// insert the relation of compound and the compartments where this
			// compartment exists
			metaCompartments.put(compoundId, compartments);
			// used for compartments present only in Reactions
			metaCompartmentsReac.put(compoundId, new TreeSet<String>());

			// create the object for the compound
			MetaboliteCI m = new MetaboliteCI(compoundId, compoundName);
			if (!compoundFormula.equals(""))
				m.setFormula(compoundFormula);
			compoundsHashAux.put(compoundId, m);
			
			// put extra information
			for (String s : metabolitesExtraInfoIndex.keySet()) {
				metabolitesExtraInfo.get(s).put(compoundId, getFieldsFromSplitedArray(fields, metabolitesExtraInfoIndex.get(s)));
			}

			line++;
		}
		r.close();
		f.close();
	}
	
	// return the compound id without the information of compartment (if
	// compoundId = "C0076[cyt]" return C0076)
	private String getCompoundID(String compoundId, int line) throws Exception {
		Matcher matcher = patternCompoundName.matcher(compoundId);
		String res = null;
		if (matcher.find()) {
			res = matcher.group(1);
		} else {
			throw new Exception("FORMAT ERROR:  " + compoundId + "at line " + line
					+ "\nExpected: metaId[compartment] or metaId.");
		}
		return res;
	}

	// calculate the compartments of one compound.
	// If record has COMPARTMENT field return all compartments of this field
	// If compoundId is "comp[meta]" return meta
	// If compartments are in COMPARTMENT field and in compound id throw an
	// exception
	private Set<String> calculateCompartment(String compoundId, String compoundCompartment, int line) throws Exception {
		Matcher matcher = patternCompoundName.matcher(compoundId);
		TreeSet<String> metaComp = new TreeSet<String>();
		if (matcher.find()) {
			// get compartment name from compoundId
			String compartment = matcher.group(2);
			if (compartment == null) { // metabolite isn't in format mets[comp]
				if (!compoundCompartment.equals("")) { // exist the compartment
														// field
					String[] comps = compoundCompartment.split(COMPSEPARATOR);
					for (String c : comps)
						metaComp.add(c);
				}
			} else {
				if (!compoundCompartment.equals(""))
					throw new Exception("FORMAT ERROR:  " + compoundId
							+ " has compartments in ID and COMPARTMENT fields " + line);
				metaComp.add(compartment);
			}
		} else {
			throw new Exception("FORMAT ERROR:  " + compoundId + "at line " + line
					+ "\nExpected: metaId[compartment] or metaId.");
		}
		return metaComp;
	}

	/**
	 * This method reads the reactions from the reactions file
	 * 
	 * @throws Exception
	 */
	private void loadReactions() throws Exception {
		System.out.println("loadReactions");
		
		reactionsExtraInfo = new HashMap<String, Map<String, String>>();
		for (String s : reactionsExtraInfoIndex.keySet())
			reactionsExtraInfo.put(s, new HashMap<String, String>());
		
		
		FileReader f = new FileReader(reactionsFile);
		BufferedReader r = new BufferedReader(f);
		String str;
		int line = 1;
		if (reacHasHeaders){
			r.readLine();
			line++;
		}
		// Mandatory fields
		int reactionIdIndex = reacIndexes.get(REACID);//(valitade(reacIndexes.get(REACID), "reaction", REACID, getReactionsMandatoryFields());
		int reactionNameIndex = reacIndexes.containsKey(REACNAME) ? reacIndexes.get(REACNAME) : -1;
		int reactionEquationIndex = reacIndexes.get(REACEQUATION);
		int reactionECNumberIndex = (reacIndexes.containsKey(REACECNUMBER)) ? reacIndexes.get(REACECNUMBER) : -1;
		int reactionGeneIndex = (reacIndexes.containsKey(REACGENERULE)) ? reacIndexes.get(REACGENERULE) : -1;
		int reactionSubsystemIndex = (reacIndexes.containsKey(REACSUBSYSTEM)) ? reacIndexes.get(REACSUBSYSTEM) : -1;
		int reactionLBIndex = (reacIndexes.containsKey(REACLB)) ? reacIndexes.get(REACLB) : -1;
		int reactionUBIndex = (reacIndexes.containsKey(REACUB)) ? reacIndexes.get(REACUB) : -1;
		
		
		
		if (reactionGeneIndex < 0) {
			this.hasGeneReactioAssociations = false;
		}

		while (r.ready()) {
			str = r.readLine();
			if(str.trim().equals(""))
				continue;
			str = (str.substring(str.length() - this.USER_METABOLITES_DELIMITER.length()) == this.USER_METABOLITES_DELIMITER) ? str
					+ this.USER_METABOLITES_DELIMITER
					: str;
			String[] fields = str.split(this.USER_REACTIONS_DELIMITER);
			for (int j = 0; j < fields.length; j++) {
				fields[j] = treatString(fields[j]);
			}

			String reactionId = "", reactionName = "", equation = "", ecnumber = "", subsystem = "";
			double lowerB = DEFAULT_LOWER_LIMIT, upperB = DEFAULT_UPPER_LIMIT;

			// Required fields
			if (reactionNameIndex >= 0)
				reactionName = getFieldsFromSplitedArray(fields, reactionNameIndex);
			reactionId = getFieldsFromSplitedArray(fields,reactionIdIndex);
			equation = getFieldsFromSplitedArray(fields,reactionEquationIndex);
			
			
//			if (reactionName.equals(""))
//				throw new Exception("No reaction name field in line " + line);
			if (reactionId.equals(""))
				throw new Exception("No reation id field in line " + line);
			if (equation.equals(""))
				throw new Exception("No reation equation field in line " + line);
			// Optional fields
			if (reactionECNumberIndex >= 0)
				ecnumber = getFieldsFromSplitedArray(fields,reactionECNumberIndex);
			if (reactionSubsystemIndex >= 0)
				subsystem = getFieldsFromSplitedArray(fields,reactionSubsystemIndex);
			if (reactionLBIndex >= 0) {
				try {
					lowerB = Double.parseDouble(getFieldsFromSplitedArray(fields,reactionLBIndex));
				} catch (Exception e) {
					throw new Exception("ERROR! line " + line + "\nExpected a number at column " + reactionLBIndex
							+ " (lower bounds column)");
				}
			}
			if (reactionUBIndex >= 0) {
				try {
					upperB = Double.parseDouble(getFieldsFromSplitedArray(fields,reactionUBIndex));
				} catch (Exception e) {
					throw new Exception("ERROR! line " + line + "\nExpected a number at column " + reactionUBIndex
							+ " (upper bounds column)");
				}
			}
			// 0:generalcompartment;
			// 1:left side;
			// 2:right side;
			// 3:reversible or irreversible
			String[] parseResults = parseReactionEquation(equation, line);
			
			// actualize the lower bound
			if (parseResults[3].equals("I") && lowerB < 0.0)
				lowerB = 0.0;

			// Build the reaction object
			ReactionCI reaction = new ReactionCI(reactionId, reactionName, parseResults[3].equals("R"),
					getReactantsOrProducts(parseResults[1], parseResults[0], reactionId, line), getReactantsOrProducts(
							parseResults[2], parseResults[0], reactionId, line));
			
			if (!reactionExistInModel(reaction)) {
				// set subsystem and ecnumber and Type
				reaction.setType(ReactionTypeEnum.Internal);
				reaction.setSubsystem(subsystem);
				reaction.setEc_number(ecnumber);
				// set reactionin hash
				reactionsHash.put(reactionId, reaction);
				// Set constrains
				ReactionConstraintCI rc = new ReactionConstraintCI(lowerB, upperB);
				defaultEC.put(reactionId, rc);

				// set external information
				for (String s : reactionsExtraInfoIndex.keySet()) {
					reactionsExtraInfo.get(s).put(reactionId, getFieldsFromSplitedArray(fields, reactionsExtraInfoIndex.get(s)));
				}
				if (this.hasGeneReactioAssociations)
					reacHasGeneReacAssociation(fields, reactionGeneIndex, reactionId);
			}
			line++;
		}
		r.close();
		f.close();
	}
	
	private String getFieldsFromSplitedArray(String[] data, int index){
		String ret = "";
		if(data.length>index)
			ret=data[index];
		
		return ret;
	}
	
	

	/*
	 * return 0: general compartment (if none, null); 1: left side of the
	 * equation; 2: right side of the equation; 3: reversible or irreversible
	 */
	private String[] parseReactionEquation(String equation, int line) throws Exception {
		String[] res = new String[4];
		Matcher matcher = patternEquation.matcher(equation);
		if (matcher.find()) {
			res[0] = matcher.group(1);
			res[1] = matcher.group(2);
			res[2] = (matcher.group(5) == null) ? "" : matcher.group(5);
			res[3] = (matcher.group(3) != null) ? "R" : "I";
		} else {
//			System.out.println("\n\n\nequation " + equation);
			throw new Exception(
					"FORMAT ERROR: Equation isn't correctly defined (use -->, =>, <==>, <=>, -> or <->) in line "
							+ line
							+ "\n Expected [general compartment]: met1 + (stoich) met2 + ... --> met3 \n [general compartment]: is optional.");
		}
		return res;
	}
	
	private Map<String, StoichiometryValueCI> getReactantsOrProducts(String reactionPart, String generalComp,
			String reactionId, int line) throws Exception {
		Map<String, StoichiometryValueCI> result = new HashMap<String, StoichiometryValueCI>();
//		System.out.println("reactionPart :" + reactionPart);
		// if the reaction is of type "meta[xpo] -->
		if (!reactionPart.trim().equals("")) {
			// Splits the reactionPart by '+', obtaining all the metabolites and
			// stoichiometric value, if any. allows met1 + + met2
			String[] rOrP = reactionPart.split("(\\s*\\+\\s*)+");
			for (int i = 0; i < rOrP.length; i++) {
				String[] rOrPParsed = parseCompoundInEqn(rOrP[i], line);
				String compartment = (rOrPParsed[2] == null) ? generalComp : rOrPParsed[2];
				String compoundId = rOrPParsed[1];
				
				if(compartment == null)
					compartment = DatabaseCSVFilesReader.DEFAULT_COMPARTMENT;
				
				if(metaCompartmentsReacFile.containsKey(compoundId))
					metaCompartmentsReacFile.get(compoundId).add(compartment);
				else
					metaCompartmentsReacFile.put(compoundId, new TreeSet<String>(Arrays.asList(compartment)));
				
				// compartments at reaction and metabolites level
				if (generalComp != null && !compartment.equals(generalComp))
					throw new Exception("The reaction  at line" + line
							+ " has general compartment and metabolite compartments.");
				compoundsHashAuxFile.put(compoundId, new MetaboliteCI(compoundId, compoundId));
				
				Double d = new Double(rOrPParsed[0]);

				compoundId = compoundId + "_" + compartment;
				if (this.convertMetIds.containsKey(compoundId)) {
					compoundId = this.convertMetIds.get(compoundId);
				}
				result.put(compoundId, new StoichiometryValueCI(compoundId, d, compartment));
				
				
			}
		}
		return result;
	}

	/*private Map<String, StoichiometryValueCI> getReactantsOrProducts(String reactionPart, String generalComp,
			String reactionId, int line) throws Exception {
		
		Map<String, StoichiometryValueCI> result = new HashMap<String, StoichiometryValueCI>();
//		System.out.println("reactionPart :" + reactionPart);
		// if the reaction is of type "meta[xpo] -->
		if (!reactionPart.trim().equals("")) {
			// Splits the reactionPart by '+', obtaining all the metabolites and
			// stoichiometric value, if any. allows met1 + + met2
			String[] rOrP = reactionPart.split("(\\s*\\+\\s*)+");
			for (int i = 0; i < rOrP.length; i++) {
				System.out.println(".......... " +rOrP[i]);
				String[] rOrPParsed = parseCompoundInEqn(rOrP[i], line);
				String compartment = (rOrPParsed[2] == null) ? generalComp : rOrPParsed[2];
				String compoundId = rOrPParsed[1];
				
				//if (!compoundsHashAux.containsKey(compoundId))
				{
					//myMethod(compoundId, compoundId);
//					throw new Exception("The metabolite " + compoundId + ", in the reaction " + reactionId + " (line "
//							+ line + ") is not defined in the metabolite file " + metabolitesFile.getName());
				}
				// compartments at reaction and metabolites level
//				if (generalComp != null && !compartment.equals(generalComp))
//					throw new Exception("The reaction  at line" + line
//							+ " has general compartment and metabolite compartments.");
//				// if no information of compartment in reaction
				
//
//				// insert a new compartment to this metabolite, if there not
//				// exist
//				// in hash.
//				
				Set<String> compartments = new TreeSet<String>();
//				
				compartments = calculateCompartment(rOrP[i].trim(), "", 0);
				System.out.println("OLA: " + compoundId);
				System.out.println("SIZE:::: " + compartments.size());
//				
				metaCompartments.put(compoundId, compartments);
				for (String string : compartments) {
					System.out.println("-------- " + compoundId + " : " + string);
				}
				metaCompartmentsReac.put(compoundId, new TreeSet<String>());
				
//				if (compartment == null) {
//					if (metaCompartments.get(compoundId).size() > 1)
//						throw new Exception("Compartment of metabolite " + rOrPParsed[1] + " at line " + line
//								+ " is unknown!");
//					else if (metaCompartments.get(compoundId).size() == 0)
//						compartment = NewDatabaseCSVFilesReader.DEFAULT_COMPARTMENT;
//					else
//						compartment = (String) metaCompartments.get(compoundId).toArray()[0];
//				}
//				
//				if (!metaCompartments.get(compoundId).contains(compartment)) {
//					metaCompartmentsReac.get(compoundId).add(compartment);
//					metaCompartments.get(compoundId).add(compartment);
//				}

				compoundsHashAux.put(compoundId, new MetaboliteCI(compoundId, compoundId));
				
				Double d = new Double(rOrPParsed[0]);

				if(compartment == null)
					compartment = DEFAULT_COMPARTMENT;
				compoundId = compoundId + "_" + compartment;
				if (this.convertMetIds.containsKey(compoundId)) {
					compoundId = this.convertMetIds.get(compoundId);
				}
				result.put(compoundId, new StoichiometryValueCI(compoundId, d, compartment));
				metabolitesInReactionFile.put(compoundId, new MetaboliteCI(compoundId, compoundId));
				
				
				
				//metaCompartments.put(compoundId, );
//				if(!metaCompartments.containsKey(compoundId))
//					metaCompartments.put(compoundId, new TreeSet<String>(Arrays.asList(new String[]{compartment})));
//				
//				if(!metaCompartmentsReac.containsKey(compoundId))
//					metaCompartmentsReac.put(compoundId, new TreeSet<String>(Arrays.asList(new String[]{compartment})));
			}
		}
		return result;
	}*/
	
	

	// returns 0-coef; 1-name; 2-compartment
	private String[] parseCompoundInEqn(String text, int line) throws Exception {
//		System.out.println("parseCompoundInEqn" + text);
		
		String[] res = new String[3];
		Matcher matcher = patternCompoundEq.matcher(text);
		if (matcher.find() && matcher.group(2) != null) {
			res[0] = (matcher.group(1) != null && !matcher.group(1).equals("")) ? matcher.group(1) : "1.0";
			// if the compound id make match in the model use
			res[1] = matcher.group(2);
			if (matcher.group(3) != null)
				res[2] = matcher.group(3);
		} else {
			throw new Exception("FORMAT ERROR: reactions file [" + reactionsFile.getName() + "] line " + line
					+ "\nExpected (stoichiometry) metabolite_id + ... -> metabolite_id + ...\n");
		}
		return res;
	}

	private boolean reactionExistInModel(ReactionCI reac) {
		return false;
	}

	/**
	 * This method handles with reactions that have geneReactions associations
	 * (gene rules)
	 * 
	 * @param fields
	 *            An array of Strings with the data of each line
	 * @param reactionGeneIndex
	 *            The index of the geneRules column in the reactions file
	 * @param reactionId
	 *            The reaction ID
	 * @throws ParseException
	 */
	private void reacHasGeneReacAssociation(String[] fields, int reactionGeneIndex, String reactionId)
			throws ParseException {

		String geneAssociation = fields[reactionGeneIndex];
		GeneReactionRuleCI geneReactionRule = new GeneReactionRuleCI(geneAssociation);
		geneReactionRules.put(reactionId, geneReactionRule);

		ArrayList<String> genesId = TreeUtils.withdrawVariablesInRule(geneReactionRule.getRule());
		for (int g = 0; g < genesId.size(); g++) {
			if (!geneSet.containsKey(genesId.get(g))) {
				geneSet.put(genesId.get(g), new GeneCI(genesId.get(g), genesId.get(g)));
			}

			if (geneReactionMapping.containsKey(genesId.get(g))) {
				ArrayList<String> dependentReactions = geneReactionMapping.get(genesId.get(g));
				dependentReactions.add(reactionId);

			} else {
				ArrayList<String> dependentReactions = new ArrayList<String>();
				dependentReactions.add(reactionId);
				geneReactionMapping.put(genesId.get(g), dependentReactions);
			}
		}

	}

	/**
	 * Deletes the '"' from strings coming from CSV files
	 */
	private String treatString(String string) {
		if (string != null && string.length() > 0) {
			if (string.charAt(0) == '"')
				string = string.substring(1);
			if (string.charAt(string.length() - 1) == '"')
				string = string.substring(0, string.length() - 1);
		}
		return string;
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
	 * @return A boolean telling if the file has gene reaction associations
	 *         (true) or not (false)
	 */
	public boolean hasGeneReactionAssociations() {
		return hasGeneReactioAssociations;
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
		return compartmentsHash;
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
		return DEFAULT_EXTARENAL_COMPARTMENT;
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

	public static void main(String[] args) {
		
		String meta = "0.1582 10F_THF[H]";
		
		String meta2 = "2 H[c]";
		
		// try {
		// String f = "D:/OptFlux/data/modeloEcoli_red.sbml";
		// Container cont = new Container(new JSBMLReader(f, "Ecoli"));
		//
		// File f1 = new File("D:/OptFlux/data/msb201165-s2_metabolites.csv");
		// File f2 = new File("D:/OptFlux/data/msb201165-s2_reactions_2.csv");
		// TreeMap<String, Integer> metIndexes = new TreeMap<String, Integer>();
		// metIndexes.put(METID, 0);
		// metIndexes.put(METNAME, 1);
		// TreeMap<String, Integer> reacIndexes = new TreeMap<String,
		// Integer>();
		// reacIndexes.put(REACID, 0);
		// reacIndexes.put(REACNAME, 1);
		// reacIndexes.put(REACEQUATION, 2);
		//
		// Map m1 = new HashMap<String, String>();
		// m1.put("ModelField", "Name");
		// m1.put("DatabaseField", "Name");
		// m1.put("ModelRegExp", "");
		// m1.put("DatabaseRegExp", "");
		//
		// DatabaseCSVFilesReader db = new DatabaseCSVFilesReader(f2, f1,
		// metIndexes, reacIndexes, "model", "name",
		// ";", ";", new TreeMap<String, Integer>(), new TreeMap<String,
		// Integer>(), true, true, m1, cont);
		// Container n = new Container(db);
		// System.out.println("Terminei");
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		// Matcher matcher =
		// Pattern.compile("^M_(.*)?\\s*l$").matcher("M_sjkdhkjshdk   l");
//		String eq = "";	
//		String name = "[\\w\\d\\.\\:\\(\\)\\-\\,]+";
//		String stoic = "\\(?(\\d+(?:\\.\\d+)?(?:[eE][\\+\\-]?\\d+)?)?\\s*\\)?";
//		
//		//private static String stoic = "\\(?(\\d+(?:\\.\\d+)?)?\\s*\\)?"; // with or
//																			// without
//																			// ()
//		String compartment = "\\[([\\w\\d\\.\\:]+)\\]";
//		Pattern patternCompoundEq = Pattern.compile("^\\s*(?:" + stoic + "\\s+)?\\s*(?:\\*?\\s+)?(" + name
//				+ ")\\s*(?:" + compartment + ")?\\s*$");
//
//		String eq = "";	
//		String name = "[\\w\\d\\.\\:\\(\\)\\-\\,]+";
//		String stoic = "\\(?(\\d+(?:\\.\\d+)?(?:[eE][\\+\\-]?\\d+)?)?\\s*\\)?";
//		
//		//private static String stoic = "\\(?(\\d+(?:\\.\\d+)?)?\\s*\\)?"; // with or
//																			// without
//																			// ()
//		String compartment = "\\[([\\w\\d\\.\\:]+)\\]";
//		Pattern patternCompoundEq = Pattern.compile("^\\s*(?:" + stoic + "\\s+)?\\s*(?:\\*?\\s+)?(" + name
//				+ ")\\s*(?:" + compartment + ")?\\s*$");
//		String eq = "";	
//		String name = "[\\w\\d\\.\\:\\(\\)\\-\\,]+";
//		String stoic = "\\(?(\\d+(?:\\.\\d+)?(?:[eE][\\+\\-]?\\d+)?)?\\s*\\)?";
//		
//		//private static String stoic = "\\(?(\\d+(?:\\.\\d+)?)?\\s*\\)?"; // with or
//																			// without
//																			// ()
//		String compartment = "\\[([\\w\\d\\.\\:]+)\\]";
//		Pattern patternCompoundEq = Pattern.compile("^\\s*(?:" + stoic + "\\s+)?\\s*(?:\\*?\\s+)?(" + name
//				+ ")\\s*(?:" + compartment + ")?\\s*$");
//
//		Pattern patternStoic = Pattern.compile(stoic);
//		Matcher matcher = patternCompoundEq.matcher(eq);
//		Matcher matcher2 = patternStoic.matcher("2.5e-005");
////
//
//		Pattern patternStoic = Pattern.compile(stoic);
//		Matcher matcher = patternCompoundEq.matcher(eq);
//		Matcher matcher2 = patternStoic.matcher("2.5e-005");
////
//		System.out.println(matcher.find());
//		String str = "";
//		System.out.println("match :" + str.trim() + "--");
//		System.out.println("match null:" + str.trim().equals(""));
//		 System.out.println("match 1:" + matcher2.group(1));
//		 System.out.println("match 2:" + matcher.group(2));
//		 System.out.println("match 3:" + matcher.group(3));
//		 System.out.println("match 4:" + matcher.group(4));
//		 System.out.println("match 4:" + matcher.group(5));

	}

	public String getModelID() {
		return modelID;
	}

	public void setModelID(String modelID) {
		this.modelID = modelID;
	}
	
	
	
	
	
	
	public boolean isMetabolitesFileMandatory() {
		return isMetabolitesFileMandatory;
	}
	
	public void setMetabolitesFileMandatory(boolean isMetabolitesFileMandatory) {
		this.isMetabolitesFileMandatory = isMetabolitesFileMandatory;
	}
	
//	private Map<String, Set<String>> getMandatoryFields() {
//		return mandatoryFields;
//	}
//	
//	public void setMandatoryFields(Map<String, Set<String>> mandatoryFields) {
//		this.mandatoryFields = mandatoryFields;
//	}
//	
//	private Map<String, Set<String>> mandatoryFields = java.util.Collections.unmodifiableMap(
//	    new HashMap<String, Set<String>>() {
//	    	{
//		        put("Reactions", new HashSet<String>(){{add(REACID);add(REACEQUATION);}});
//		        put("Metabolites", new HashSet<String>(){{add(METID);}});
//	    	}
//	    });
	
	
	public void setReactionsMandatoryFields(Set<String> reactionsMandatoryFields) {
		NewDatabaseCSVFilesReader.reactionsMandatoryFields = reactionsMandatoryFields;
	}
	
	public static Set<String> getReactionsMandatoryFields() {
		return reactionsMandatoryFields;
	}
	
	public void setMetabolitesMandatoryFields(Set<String> metabolitesMandatoryFields) {
		NewDatabaseCSVFilesReader.metabolitesMandatoryFields = metabolitesMandatoryFields;
	}
	
	public static Set<String> getMetabolitesMandatoryFields() {
		return metabolitesMandatoryFields;
	}		    
 
	private static <T> T validate(T object, String name, String id, Set<String> madatory, T validavalue, T defaultValue){
//		if(madatory.contains(id) && object==validavalue){
//			throw new Exception();
//		}
		
		return object;
	}
}
