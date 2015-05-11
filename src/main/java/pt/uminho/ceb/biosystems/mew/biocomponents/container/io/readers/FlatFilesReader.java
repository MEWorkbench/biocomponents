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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.InvalidBooleanRuleException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionTypeEnum;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.interfaces.IContainerBuilder;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.MatrixEnum;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.indexedhashmap.IndexedHashMap;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.parser.ParseException;



/**
 * 
 * @author paulo maia, Mar 6, 2009 at 4:46:56 PM
 *
 */
public class FlatFilesReader implements IContainerBuilder{	

	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_MATRIX_OFFSET = 0;
	public static final String DEFAULT_METABOLITES_PREFIX = "meta_";
	
	public static final double DEFAULT_UPPER_LIMIT = 10000;
	public static final double DEFAULT_LOWER_LIMIT = -10000;
	
	public static final String DEFAULT_COMPARTMENT="extra-celular";
	public static final String DEFAULT_COMPARTMENT_CELLDESIGNER="default";
	
	
	public String USER_REACTIONS_DELIMITER = null;
	public String USER_METABOLITES_DELIMITER = null; 
	public String USER_MATRIX_DELIMITER = null;
	public int USER_MATRIX_OFFSET = -1;
	
	protected File reactionsFile = null;
	protected File matrixFile = null;
	protected File metabolitesFile = null;
	protected File genesFile = null;
	protected String modelID;
	protected Map<String, Map<String, String>> metabolitesExtraInfo = null;
	protected Map<String, Map<String, String>> reactionsExtraInfo = null;
	
	protected boolean externalMetabolitesInFile = false;
	protected List<Integer> listExternalMetabolites = null; 
	
	protected MatrixEnum matrixType = MatrixEnum.SPARSE;
	
	private IndexedHashMap<String, CompartmentCI> compartmentSet = new IndexedHashMap<String,CompartmentCI>();
	private IndexedHashMap<String, MetaboliteCI> metaboliteSet = new IndexedHashMap<String, MetaboliteCI>();
	private IndexedHashMap<String, ReactionCI> reactionSet = new IndexedHashMap<String, ReactionCI>();
	private IndexedHashMap<String, GeneCI> geneSet = new IndexedHashMap<String, GeneCI>();
	private IndexedHashMap<String, ReactionConstraintCI> defaultEC = new IndexedHashMap<String, ReactionConstraintCI>();
	
	private HashMap<Integer, CompartmentCI> metabolite_compartment = new HashMap<Integer, CompartmentCI>();
	private String biomassId;
	
	private int numMet = 0;
	private int numReac = 0;
	
	public FlatFilesReader(File reactionsFile, File matrixFile, File metabolitesFile, File genesFile, String modelID, MatrixEnum mt, String userReactionsDelimiter, String userMetabolitesDelimiter, String userMatrixDelimiter) throws Exception{
		if(reactionsFile != null && !reactionsFile.exists()) throw new Exception("The file "+reactionsFile.getAbsolutePath()+" does not exist!");
		if(matrixFile != null && !matrixFile.exists()) throw new Exception("The file "+matrixFile.getAbsolutePath()+" does not exist!");
		if(metabolitesFile != null && !metabolitesFile.exists()) throw new Exception("The file "+metabolitesFile.getAbsolutePath()+" does not exist!");
		if(genesFile != null && !genesFile.exists()) throw new Exception("The file "+genesFile.getAbsolutePath()+" does not exist!");
		this.reactionsFile = reactionsFile;
		this.matrixFile = matrixFile;
		this.metabolitesFile = metabolitesFile;
		this.modelID = modelID;
		if(mt != null) this.matrixType = mt;
		if(genesFile == null || genesFile.getName() == "") this.genesFile = null;
		else this.genesFile = genesFile;
		this.USER_REACTIONS_DELIMITER = userReactionsDelimiter;
		this.USER_METABOLITES_DELIMITER = userMetabolitesDelimiter;
		this.USER_MATRIX_DELIMITER = userMatrixDelimiter;
		
		this.populateInformation();
	}
	
	public FlatFilesReader(String reactionsFilePath, String matrixFilePath, String metabolitesFilePath, String genesFilePath, String modelID, MatrixEnum mt, String userReactionsDelimiter, String userMetabolitesDelimiter, String userMatrixDelimiter) throws Exception{
		if(reactionsFilePath != null)
			this.reactionsFile = new File(reactionsFilePath);
		if(matrixFilePath != null)
			this.matrixFile = new File(matrixFilePath);
		if(metabolitesFilePath != null)
			this.metabolitesFile = new File(metabolitesFilePath);
		if(reactionsFilePath != null)
			this.reactionsFile = new File(reactionsFilePath);
		if(genesFilePath == null || genesFilePath == "") this.genesFile = null;
		else this.genesFile = new File (genesFilePath);
		
		if(reactionsFile != null && !reactionsFile.exists()) throw new Exception("The file "+reactionsFile.getAbsolutePath()+" does not exist!");
		if(matrixFile != null && !matrixFile.exists()) throw new Exception("The file "+matrixFile.getAbsolutePath()+" does not exist!");
		if(metabolitesFile != null && !metabolitesFile.exists()) throw new Exception("The file "+metabolitesFile.getAbsolutePath()+" does not exist!");
		if(genesFile != null && !genesFile.exists()) throw new Exception("The file "+genesFile.getAbsolutePath()+" does not exist!");
		
		this.modelID = modelID;
		if(mt != null) this.matrixType = mt;
		this.USER_REACTIONS_DELIMITER = userReactionsDelimiter;
		this.USER_METABOLITES_DELIMITER = userMetabolitesDelimiter;
		this.USER_MATRIX_DELIMITER = userMatrixDelimiter;
		
		this.populateInformation();
	}
	
	public FlatFilesReader(String reactionsFilePath, String matrixFilePath, String metabolitesFilePath, String genesFilePath, String modelID, MatrixEnum mt) throws Exception{
		this(reactionsFilePath, matrixFilePath, metabolitesFilePath, genesFilePath, modelID, mt, ",", "\t", "\t");
	};
	
	public FlatFilesReader(File reactionsFile, File matrixFile, File metabolitesFile, File genesFile, String modelID, MatrixEnum mt) throws Exception{
		this(reactionsFile, matrixFile, metabolitesFile, genesFile, modelID, mt, ",", "\t", "\t");
	};
	
	public FlatFilesReader(String reactionsFilePath, String matrixFilePath, String metabolitesFilePath, String genesFilePath, String modelID) throws Exception{
		this(reactionsFilePath, matrixFilePath, metabolitesFilePath, genesFilePath, modelID, null, ",", "\t", "\t");
	};
	
	public FlatFilesReader(File reactionsFile, File matrixFile, File metabolitesFile, File genesFile, String modelID) throws Exception{
		this(reactionsFile, matrixFile, metabolitesFile, genesFile, modelID, null, ",", "\t", "\t");
	};
	
	public FlatFilesReader(File reactionsFile, File matrixFile, File metabolitesFile, File genesFile, String modelID, String userReactionsDelimiter, String userMetabolitesDelimiter, String userMatrixDelimiter) throws Exception{
		this(reactionsFile, matrixFile, metabolitesFile, genesFile, modelID, null, userReactionsDelimiter, userMetabolitesDelimiter, userMatrixDelimiter);
	}
	
	public FlatFilesReader(String reactionsFilePath, String matrixFilePath, String metabolitesFilePath, String genesFilePath, String modelID, String userReactionsDelimiter, String userMetabolitesDelimiter, String userMatrixDelimiter) throws Exception{
		this(reactionsFilePath, matrixFilePath, metabolitesFilePath, genesFilePath, modelID, null, userReactionsDelimiter, userMetabolitesDelimiter, userMatrixDelimiter);
	}
	
	/**
	 * This method reads the information in the reactions file
	 * @throws Exception
	 */
	public void loadReactions() throws Exception{
		
		countReactions();
		
		ArrayList<ArrayList<Double>> denseMatrix = new ArrayList<ArrayList<Double>>();
		HashMap<Integer,HashMap<Integer, Double>> sparseMatrix = new HashMap<Integer,HashMap<Integer, Double>>();
		
		if(matrixType == MatrixEnum.SPARSE) sparseMatrix = loadSparseMatrix();
		else denseMatrix = loadDenseMatrix();
		
		verifyFiles(sparseMatrix, denseMatrix);
		
		FileReader f = new FileReader(reactionsFile);
		BufferedReader r = new BufferedReader(f);

		String str = new String("");
		StringTokenizer st;
		
		ReactionConstraintCI rc;
		
		for(int i=0; r.ready();i++)
		{
			str = r.readLine();
			st = new StringTokenizer(str,USER_REACTIONS_DELIMITER);
			int numTokens = st.countTokens();
			String name = st.nextToken();
			String ir = null;
			Double lo = null;
			Double up = null;
			double[] bounds = null;
			
			if (numTokens==2) {			//each line has 2 elements: the reaction name and its reversibility
				ir = st.nextToken();
				bounds = reactionsTwoTokens(ir, i+1);
				lo = bounds[0];
				up = bounds[1];
			}
			else if (numTokens >= 3) {
				if(numTokens==4){		//each line has 4 elements: the reaction name, its reversibility, lower bound and upper bound
					ir = st.nextToken();
					if (!ir.equals("R") && !ir.equals("I"))
						throw new Exception("FORMAT ERROR: reactions file ["+reactionsFile+"], line "+(i+1)+"\nThe second element must be 'R' (for reversible) or 'I' (for irreversible)\n");
				}
				ir = st.nextToken();
				String ir2 = st.nextToken();
				bounds = reactionsMoreThanTwoTokens(ir, ir2, i+1, numTokens);
				lo = bounds[0];
				up = bounds[1];
				
				if (lo<0.0 && up > 0.0) ir= "R";
				else ir = "I";
			}
			else throw new Exception("FORMAT ERROR: reactions file ["+reactionsFile+"], line "+(i+1)+"\nIt has ["+numTokens+"] elements, but should be two (reaction ID and reversibility [I/R]), three (reaction ID, lower bound and upper bound) or four (reaction ID, reversibility[I/R], lower bound and upper bound)\n");
			
			if(lo != DEFAULT_LOWER_LIMIT || up != DEFAULT_UPPER_LIMIT){
				rc = new ReactionConstraintCI(lo, up);
				defaultEC.put(name, rc);
			}
			
			HashMap<String, StoichiometryValueCI> reactants = new HashMap<String, StoichiometryValueCI>();
			HashMap<String, StoichiometryValueCI> products = new HashMap<String, StoichiometryValueCI>();
			
			if(matrixType == MatrixEnum.SPARSE)
				analyzeSparseMatrix(sparseMatrix, reactants, products, i);
			else
				analyzeDenseMatrix(denseMatrix, reactants, products, i);
			
			ReactionCI fh = new ReactionCI(name, name, ir.equals("R"), reactants, products);
			reactionSet.put(name, fh);
			fh.setType(ReactionTypeEnum.Internal);
		}

		r.close();
		f.close();
		
		if(genesFile != null)
			loadGenes();
	}

	/**
	 * This method counts the number of reactions in the reactions file
	 * @throws IOException
	 */
	public void countReactions() throws IOException{
		FileReader f = new FileReader(reactionsFile);
		BufferedReader r = new BufferedReader(f);
//		String str;
		
		while((r.readLine())!= null){
			numReac++;
		}
		
		r.close();
		f.close();
	}
	
	/**
	 * If the reactions file has 2 tokens, then it is the reaction name and its reversibility. This method handles with this case.
	 * @param ir String: "I" (irreversible) or "R" (reversible)
	 * @param line The line number
	 * @return An array with the lower and upper bounds (0: lower bound; 1: upper bound)
	 * @throws Exception
	 */
	public double[] reactionsTwoTokens(String ir, int line) throws Exception{
		double[] bounds = new double[2]; //0: lower bound; 1: upper bound
		if (ir.equals("R")){
			bounds[0] = DEFAULT_LOWER_LIMIT;
			bounds[1] = DEFAULT_UPPER_LIMIT;
		}
		else if (ir.equals("I")){
			bounds[0] = 0.0;
			bounds[1] = DEFAULT_UPPER_LIMIT;
		}
		else{
			throw new Exception("FORMAT ERROR: reactions file ["+reactionsFile+"], line "+line+"\nThe second element must be 'R' (for reversible) or 'I' (for irreversible)\n");
		}
		return bounds;
	}
	
	/**
	 * If the reactions file has more than 2 tokens, then it is the reaction name, its reversibility, lower and upper bounds (it cannot have more than 4 tokens). This method handles with this case.
	 * @param ir Lower bound token
	 * @param ir2 Upper bound token
	 * @param line The line number
	 * @param numTokens Total number of tokens
	 * @return An array with the lower and upper bounds (0: lower bound; 1: upper bound)
	 * @throws Exception
	 */
	public double[] reactionsMoreThanTwoTokens(String ir, String ir2, int line, int numTokens) throws Exception{
		double[] bounds = new double[2]; //0: lower bound; 1: upper bound
		if(numTokens > 4){		// cannot have more than 4 elements
			throw new Exception("FORMAT ERROR: reactions file ["+reactionsFile+"], line "+line+"\nIt has ["+numTokens+"] elements, but should be two (reaction ID and reversibility [I/R]), three (reaction ID, lower bound and upper bound) or four (reaction ID, reversibility[I/R], lower bound and upper bound)\n");
		}
		
		try {
			bounds[0] = Double.parseDouble(ir);
		} catch (Exception e) {
			throw new Exception("FORMAT ERROR: reactions file ["+reactionsFile+"], line "+line+"\nThe third element must be a number\n");
		}
		
		try {
			bounds[1] = Double.parseDouble(ir2);
		} catch (Exception e) {
			throw new Exception("FORMAT ERROR: reactions file ["+reactionsFile+"], line "+line+"\nThe fourth element must be a number\n");
		}
		
		return bounds;
	}
	
	/**
	 * This method analyzes the sparse matrix and defines if each metabolite is a reactant or a product
	 * @param sparseMatrix The sparse matrix
	 * @param reactants A map to be filled with the reactants
	 * @param products A map to be filled with the reactants
	 * @param i Index of the current reaction
	 */
	public void analyzeSparseMatrix(HashMap<Integer,HashMap<Integer, Double>> sparseMatrix, HashMap<String,StoichiometryValueCI> reactants, HashMap<String,StoichiometryValueCI> products, int i){
		for (int met : sparseMatrix.keySet()){
			HashMap<Integer, Double> m = sparseMatrix.get(met);
			for (int reac : m.keySet()){
				if (reac == i){
					if(m.get(reac) < 0){
						if(externalMetabolitesInFile) reactants.put(metaboliteSet.getKeyAt(met), new StoichiometryValueCI(metaboliteSet.getKeyAt(met), Math.abs(m.get(reac)), metabolite_compartment.get(met).getId()));
						else reactants.put(metaboliteSet.getKeyAt(met), new StoichiometryValueCI(metaboliteSet.getKeyAt(met), Math.abs(m.get(reac)), "I"));
					}
					else{
						if(externalMetabolitesInFile) products.put(metaboliteSet.getKeyAt(met), new StoichiometryValueCI(metaboliteSet.getKeyAt(met), Math.abs(m.get(reac)), metabolite_compartment.get(met).getId()));
						else products.put(metaboliteSet.getKeyAt(met), new StoichiometryValueCI(metaboliteSet.getKeyAt(met), Math.abs(m.get(reac)), "I"));
					}
				}
			}
		}
	}
	
	/**
	 * This method analyzes the dense matrix and defines if each metabolite is a reactant or a product
	 * @param denseMatrix The dense matrix
	 * @param reactants A map to be filled with the reactants
	 * @param products A map to be filled with the reactants
	 * @param i Index of the current reaction
	 */
	public void analyzeDenseMatrix(ArrayList<ArrayList<Double>> denseMatrix, HashMap<String,StoichiometryValueCI> reactants, HashMap<String,StoichiometryValueCI> products, int i){
		
		/**
		 * This block analyzes the dense matrix and defines if each metabolite is a reactant or a product
		 */
		for(int j=0; j<denseMatrix.size(); j++){
			if(denseMatrix.get(j).get(i) < 0){
				if(externalMetabolitesInFile) reactants.put(metaboliteSet.getKeyAt(j), new StoichiometryValueCI(metaboliteSet.getKeyAt(j), Math.abs(denseMatrix.get(j).get(i)), metabolite_compartment.get(j).getId()));
				else reactants.put(metaboliteSet.getKeyAt(j), new StoichiometryValueCI(metaboliteSet.getKeyAt(j), Math.abs(denseMatrix.get(j).get(i)), "I"));
			}
			if(denseMatrix.get(j).get(i) > 0){
				if(externalMetabolitesInFile) products.put(metaboliteSet.getKeyAt(j), new StoichiometryValueCI(metaboliteSet.getKeyAt(j), Math.abs(denseMatrix.get(j).get(i)), metabolite_compartment.get(j).getId()));
				else products.put(metaboliteSet.getKeyAt(j), new StoichiometryValueCI(metaboliteSet.getKeyAt(j), Math.abs(denseMatrix.get(j).get(i)), "I"));
			}
		}
	}
	
	/**
	 * This method reads the genes file, if it exists
	 * @throws IOException
	 * @throws ParseException
	 * @throws Exception
	 */
	public void loadGenes() throws IOException, ParseException, Exception{
		FileReader f = new FileReader(genesFile);
		BufferedReader r = new BufferedReader(f);
		int line = 0;
		String str;
		
		while(r.ready()){
			str = r.readLine();
			line++;
			System.out.println(str);
			
			if(!str.contains("="))		//each geneRule must have one "=", that divides the reaction name and the geneRule itself
				throw new Exception("FORMAT ERROR: LINE "+line+"\nExpected reactionID = geneRule\n");
			
			str+=" =";
			String[] data = str.split("=");
			
			if(!reactionSet.containsKey(data[0].trim())){		//if the reaction name in the geneRules file isn't defined in the reaction file
				throw new Exception("The reactionID ["+data[0]+"] in the file ["+genesFile.getName()+"], at line "+line+", doesn't appear in the file ["+reactionsFile.getName()+"]\n");
			}
			
			if(data.length == 2)
				try{
					reactionSet.get(data[0].trim()).setGeneRule(data[1].trim());
				} catch (InvalidBooleanRuleException e){
					throw new Exception("FORMAT ERROR: LINE "+line+"\nExpected reactionID = geneRule, where geneRule can be:\ngene\ngene1 and/or gene2\n(gene1 and/or gene2) and/or gene3\n...");
				}
			else				//a geneRule with more than one "=" is an invalid geneRule
				throw new Exception("FORMAT ERROR: LINE "+line+"\nExpected reactionID = geneRule\n");
				
			Set<String> geneIDs = reactionSet.get(data[0].trim()).getGenesIDs();
			
			for (String s : geneIDs){
				if (!geneSet.containsKey(s)){
					geneSet.put(s, new GeneCI(s, s));
				}
			}
		}
		
		r.close();
		f.close();
	}
	
	/**
	 * This method does some verification operations in the file
	 * @param sparseMatrix 
	 * @param denseMatrix
	 * @throws Exception
	 */
	private void verifyFiles(HashMap<Integer, HashMap<Integer, Double>> sparseMatrix, ArrayList<ArrayList<Double>> denseMatrix) throws Exception {
		int met = getMetMatrix(sparseMatrix, denseMatrix);
		int reac = getReacMatrix(sparseMatrix, denseMatrix);
		
		if(met < numMet){
			throw new Exception("The metabolites in the file ["+metabolitesFile.getName()+"] from line "+(met+1)+" until the end of the file are not being used in the file ["+matrixFile.getName()+"]\n");
		}
		if(reac < numReac){
			throw new Exception("The reactions in the file ["+reactionsFile.getName()+"] from line "+(reac+1)+" until the end of the file are not being used in the file ["+matrixFile.getName()+"]\n");
		}
		
	}

	/**
	 * This method calculates the number of metabolites in the matrix
	 * @param sparseMatrix
	 * @param denseMatrix
	 * @return the number of metabolites in the matrix
	 */
	private int getMetMatrix(HashMap<Integer, HashMap<Integer, Double>> sparseMatrix, ArrayList<ArrayList<Double>> denseMatrix) {
		int result = Integer.MIN_VALUE;
		if(matrixType == MatrixEnum.SPARSE){
			for(int met : sparseMatrix.keySet()){
				if(met > result) result = met;
			}
			result++;
		}
		else{
			result = denseMatrix.size();
		}
		return result;
	}
	
	/**
	 * This method calculates the number of reactions in the matrix
	 * @param sparseMatrix
	 * @param denseMatrix
	 * @return the number of reactions in the matrix
	 */
	private int getReacMatrix(HashMap<Integer, HashMap<Integer, Double>> sparseMatrix, ArrayList<ArrayList<Double>> denseMatrix) {
		int result = Integer.MIN_VALUE;
		if(matrixType == MatrixEnum.SPARSE){
			for(int met : sparseMatrix.keySet()){
				for( int reac : sparseMatrix.get(met).keySet()){
					if(reac > result) result = reac;
				}
			}
			result++;
		}
		else{
			result = denseMatrix.get(0).size();
		}
		return result;
	}

	/**
	 * Loads the metabolites from a text file
	 * @throws Exception 
	 */
	public void loadMetabolites() throws Exception{
		
		Set<String> metabolites = new TreeSet<String>();
		Set<String> duplicated_metabolites = new TreeSet<String>();
		Set<String> comp = new TreeSet<String>();
		seekDuplicatedMetabolites(metabolites, duplicated_metabolites, comp);

		CompartmentCI[] compartments = new CompartmentCI[comp.size()];
		boolean[] created_compartments = new boolean[comp.size()];
		ArrayList<String> comp_by_index = new ArrayList<String>();
		createAuxCompStructures(compartments, created_compartments, comp_by_index, comp);
		
		LinkedList<String> ids = new LinkedList<String>();
		listExternalMetabolites = new ArrayList<Integer>();		
		String str = new String("");
		FileReader f = new FileReader(metabolitesFile);
		BufferedReader r = new BufferedReader(f);
		boolean flag = true;
		str = new String("");
		
		for(int i=0;r.ready();i++){
			str = r.readLine();
			String[] tokens;
			tokens = str.split(USER_METABOLITES_DELIMITER);

			if (tokens.length == 1)		// just metabolite name
				flag = metabolitesOneToken(flag, i, str, ids);
			else if (tokens.length == 2)	// metabolite name and compartment
				metabolitesTwoTokens(duplicated_metabolites, ids, tokens, created_compartments, compartments, comp_by_index, i);
			else throw new Exception("FORMAT ERROR: metabolites file ["+metabolitesFile+"], line "+(i+1)+"\nIt has "+tokens.length+" elements, but should be one (metabolite ID) or two (metabolite ID and compartment ID)\n");
		}
		
		r.close();
		f.close();
		
		metabolitesEliminateSpaces(ids);
	}
	
	/**
	 * This method seeks for duplicated metabolites
	 * @param metabolites Set to be filled with the metabolites
	 * @param duplicated_metabolites Set to be filled with the duplicated metabolites
	 * @param comp Set to be filled with the compartments
	 * @throws IOException
	 * @throws Exception
	 */
	public void seekDuplicatedMetabolites(Set<String> metabolites, Set<String> duplicated_metabolites, Set<String> comp) throws IOException, Exception{
		FileReader f = new FileReader(metabolitesFile);
		BufferedReader r = new BufferedReader(f);
		String str = "";
		
		for (int i=0; r.ready(); i++){
			numMet++;
			str = r.readLine();
			String[] tokens;
			tokens = str.split(USER_METABOLITES_DELIMITER);

			if (tokens.length == 1){				//each line just have the metabolite name
				if(!metabolites.add(str)){			//the metabolite already existed
					duplicated_metabolites.add(str);
				}
			}
			else if (tokens.length == 2)			//each line have the metabolite name and compartment
			{
				if(!metabolites.add(tokens[0])){
					duplicated_metabolites.add(tokens[0]);
				}
				comp.add(tokens[1]);
			}
			else throw new Exception("FORMAT ERROR: metabolites file ["+metabolitesFile+"], line "+(i+1)+"\nIt has "+tokens.length+" elements, but should be one (metabolite ID) or two (metabolite ID and compartment ID)\n");
		}
		if(comp.size() == 1){		//doesn't have external compartment, just have the internal
			comp.add("E");
		}
		r.close();
		f.close();
	}
	
	/**
	 * This method creates a structure where we can access to a compartment by an index
	 * @param compartments
	 * @param created_compartments
	 * @param comp_by_index
	 * @param comp
	 */
	public void createAuxCompStructures(CompartmentCI[] compartments, boolean[] created_compartments, ArrayList<String> comp_by_index, Set<String> comp){
		Iterator<String> it = comp.iterator();
		int index = 0;
		while(it.hasNext()){
			String c = it.next();
			created_compartments[index] = false;
			comp_by_index.add(c);
			index++;
		}
	}
	
	/**
	 * This method deals with metabolites file with just one token (the metabolite ID)
	 * @param flag An auxiliar flag, used to create just one time the internal and external compartments
	 * @param i
	 * @param str The current line of the metabolites file
	 * @param ids Structure to fill with the metabolites ID
	 * @return
	 */
	public boolean metabolitesOneToken(boolean flag, int i, String str, LinkedList<String> ids){
		if(flag){
			CompartmentCI c = new CompartmentCI("I", "I", "I");
			compartmentSet.put(c.getId(), c);
			
			CompartmentCI c2 = new CompartmentCI("E", "E", "E");
			compartmentSet.put(c2.getId(), c2);
			flag = false;
		}
		metabolite_compartment.put(i, compartmentSet.get("I"));
		ids.add(str);
		
		return flag;
	}
	
	/**
	 * This method deals with metabolites file with two tokens (the metabolite ID and its compartment)
	 * @param duplicated_metabolites Structure with the of duplicated metabolites ID (in other words, metabolites with the same ID but in different compartments)
	 * @param ids Structure with all the metabolites ID
	 * @param tokens Array with the tokens of the corresponding line (0: metabolite ID; 1: metabolite compartment)
	 * @param created_compartments Structure with the compartments already created
	 * @param compartments Structure with all the compartments
	 * @param comp_by_index Structure with the compartments accessible by index
	 * @param i
	 */
	public void metabolitesTwoTokens(Set<String> duplicated_metabolites, LinkedList<String> ids, String[] tokens, boolean[] created_compartments, CompartmentCI[] compartments, ArrayList<String> comp_by_index, int i){
		this.externalMetabolitesInFile = true;
		if(duplicated_metabolites.contains(tokens[0])){		// if there are metabolites with the same name (but in different compartments), then the id will be metName_compartment
			ids.add(tokens[0]+"_"+tokens[1]);
		}
		else{												// if there aren't duplicated metabolites 
			ids.add(tokens[0]);
		}
		if (tokens[1].equalsIgnoreCase("E")){
//			System.out.println("TEM EXTERNAL MET");
			listExternalMetabolites.add(i);
		}
		if(!created_compartments[comp_by_index.indexOf(tokens[1])]){
			compartments[comp_by_index.indexOf(tokens[1])] = new CompartmentCI(tokens[1], tokens[1], tokens[1]);
			compartmentSet.put(compartments[comp_by_index.indexOf(tokens[1])].getId(), compartments[comp_by_index.indexOf(tokens[1])]);
			created_compartments[comp_by_index.indexOf(tokens[1])] = true;
		}
		metabolite_compartment.put(i, compartments[comp_by_index.indexOf(tokens[1])]);
		
		if(i == numMet-1 && !allCreatedCompartments(created_compartments)){
			System.out.println("\n\nENTREI\n\n");
			compartments[comp_by_index.indexOf("E")] = new CompartmentCI("E", "E", "E");
			compartmentSet.put(compartments[comp_by_index.indexOf("E")].getId(), compartments[comp_by_index.indexOf("E")]);
		}
	}
	
	/**
	 * This method checks if all compartments are created. If not, that it means that the external compartment is missing
	 * @param created_compartments An array of boolean that indicates which compartments are created
	 * @return A boolean: true if all compartment are created, false if not
	 */
	public boolean allCreatedCompartments(boolean[] created_compartments){
		boolean result = true;
		for(boolean b : created_compartments)
			result &= b;
		return result;
	}
	
	/**
	 * This method removes the white spaces from the metabolites ID
	 * @param ids Structure with all the metabolites ID
	 */
	public void metabolitesEliminateSpaces(LinkedList<String> ids){
		for(String s: ids){
			s = s.replaceAll(" ", "");
			metaboliteSet.put(s, new MetaboliteCI(s, s));
		}
	}
	/**
	 * Loads a dense matrix from file converting it immediatly to sparse format.
	 * @return
	 * @throws Exception
	 */
	public ArrayList<ArrayList<Double>> loadDenseMatrix() throws Exception{
		
		int i, j;
		ArrayList<ArrayList<Double>> matrix = new ArrayList<ArrayList<Double>>();
		String[] tokens;
		int line = 0;
		
		FileReader file = new FileReader(matrixFile);
		BufferedReader buf = new BufferedReader(file);
		
		String string = buf.readLine();
		for(i = 0; string != null; i++){
			line++;
			
			if(i >= numMet){	//if the number of rows in the matrix is bigger than the number of metabolites, an exception is thrown
				throw new Exception("There is a metabolite that doesn't exist in the file ["+metabolitesFile.getName()+"] and is being requested in the file ["+matrixFile.getName()+"], at line "+line+"\n");
			}
			
			matrix.add(new ArrayList<Double>());
			tokens = string.split(USER_MATRIX_DELIMITER);
			for (j = 0; j < tokens.length; j++){
				
				if(j >= numReac){//if the number of columns in the matrix is bigger than the number of reactions, an exception is thrown
					throw new Exception("There is a reaction that doesn't exist in the file ["+reactionsFile.getName()+"] and is being requested in the file ["+matrixFile.getName()+"], at line "+line+"\n");
				
				}
				matrix.get(i).add(Double.parseDouble(tokens[j]));
			}
			string = buf.readLine();
		}
		
		buf.close();
		file.close();
		
		return matrix;	
	}

	
	/**
	* Reads a sparse stoichiometric matrix from file.
	*/
	public HashMap<Integer,HashMap<Integer, Double>> loadSparseMatrix() throws Exception{
		
		HashMap<Integer,HashMap<Integer, Double>> matrix = new HashMap<Integer,HashMap<Integer, Double>>();
		String[] tokens;
		int line = 0;
		
		FileReader file = new FileReader(matrixFile);
		BufferedReader buf = new BufferedReader(file);
		
		String string = buf.readLine();
		while(string != null){
			line++;
			tokens = string.split(USER_MATRIX_DELIMITER);
			
			if(Integer.parseInt(tokens[0]) >= numMet){
				throw new Exception("There is a metabolite that doesn't exist in the file ["+metabolitesFile.getName()+"] and is being requested in the file ["+matrixFile.getName()+"], at line "+line+"\n");
			}
			
			if(Integer.parseInt(tokens[1]) >= numReac){
				throw new Exception("There is a reaction that doesn't exist in the file ["+reactionsFile.getName()+"] and is being requested in the file ["+matrixFile.getName()+"], at line "+line+"\n");
			}
			
			
			if(!matrix.containsKey(Integer.parseInt(tokens[0]))) matrix.put(Integer.parseInt(tokens[0]), new HashMap<Integer, Double>());
			matrix.get(Integer.parseInt(tokens[0])).put(Integer.parseInt(tokens[1]), Double.parseDouble(tokens[2]));
			
			string = buf.readLine();
		}
		
		buf.close();
		file.close();
		
		return matrix;
	}
	
	/**
	 * This method generates metabolites with a default name 
	 * @param numberOfMetabolites
	 * @return
	 */
	public IndexedHashMap<String, MetaboliteCI> generateMetabolites(int numberOfMetabolites){
		
		IndexedHashMap< String, MetaboliteCI> metabolites = new IndexedHashMap<String, MetaboliteCI>();
		
		for(int i = 0; i< numberOfMetabolites; i++){
			MetaboliteCI m = new MetaboliteCI(DEFAULT_METABOLITES_PREFIX+i, DEFAULT_METABOLITES_PREFIX+i);
			metabolites.put(m.getId(),m);
		}
		
		return metabolites;		
	}

	/**
	 * This method populates the structures with the information in the flat files
	 * @throws Exception
	 */
	public void populateInformation() throws Exception{
		this.loadMetabolites();
		this.loadReactions();
		
		for (int m : metabolite_compartment.keySet()){
			compartmentSet.get(metabolite_compartment.get(m).getId()).addMetaboliteInCompartment(metaboliteSet.getKeyAt(m));
		}
		
		metabolitesExtraInfo = new HashMap<String, Map<String,String>>();
		reactionsExtraInfo = new HashMap<String, Map<String,String>>();
	}

	/**
	 * @return The user reactions delimiter
	 */
	public String getUSER_REACTIONS_DELIMITER() {
		return USER_REACTIONS_DELIMITER;
	}

	/**
	 * @return The user metabolites delimiter
	 */
	public String getUSER_METABOLITES_DELIMITER() {
		return USER_METABOLITES_DELIMITER;
	}

	/**
	 * @return The user matrix delimiter
	 */
	public String getUSER_MATRIX_DELIMITER() {
		return USER_MATRIX_DELIMITER;
	}

	/**
	 * @return The user matrix offset. If it doesn't exists, returns the default matrix offset
	 */
	public int getUSER_MATRIX_OFFSET() {
		if(USER_MATRIX_OFFSET<0)
			return DEFAULT_MATRIX_OFFSET;
		else return USER_MATRIX_OFFSET;
	}

	/**
	 * @param user_matrix_offset The user matrix offset to be set
	 */
	public void setUSER_MATRIX_OFFSET(int user_matrix_offset) {
		USER_MATRIX_OFFSET = user_matrix_offset;
	}
	
	/**
	 * @return The reactions file
	 */
	public File getReactionsFile() {
		return reactionsFile;
	}

	/**
	 * @param reactionsFile The reactions file to be set
	 */
	public void setReactionsFile(File reactionsFile) {
		this.reactionsFile = reactionsFile;
	}

	/**
	 * @return The matrix file
	 */
	public File getMatrixFile() {
		return matrixFile;
	}

	/**
	 * @param matrixFile The matrix file to be set
	 */
	public void setMatrixFile(File matrixFile) {
		this.matrixFile = matrixFile;
	}

	/**
	 * @return The metabolites file
	 */
	public File getMetabolitesFile() {
		return metabolitesFile;
	}

	/**
	 * @param metabolitesFile The metabolites file to be set
	 */
	public void setMetabolitesFile(File metabolitesFile) {
		this.metabolitesFile = metabolitesFile;
	}

	/**
	 * @return The model ID
	 */
	public String getModelID() {
		return modelID;
	}

	/**
	 * @param modelID The model ID to be set
	 */
	public void setModelID(String modelID) {
		this.modelID = modelID;
	}

	/**
	 * @return The matrix type
	 */
	public MatrixEnum getMatrixType() {
		return matrixType;
	}

	/**
	 * @param matrixType The matrix type to be set
	 */
	public void setMatrixType(MatrixEnum matrixType) {
		this.matrixType = matrixType;
	}

	/**
	 * @param id The biomass ID to be set
	 */
	public void setbiomassId(String id){
		this.biomassId = id;
	}
	
	/**
	 * @return The model ID
	 */
	@Override
	public String getModelName() {
		return modelID;
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
		return null;
	}

	/**
	 * @return The version
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
	 * @return A map with the reactions
	 */
	@Override
	public Map<String, ReactionCI> getReactions() {
		return reactionSet;
	}

	/**
	 * @return A map with the metabolites
	 */
	@Override
	public Map<String, MetaboliteCI> getMetabolites() {
		return metaboliteSet;
	}

	/**
	 * @return A map with the genes
	 */
	@Override
	public Map<String, GeneCI> getGenes() {
		return geneSet;
	}

	/**
	 * @return The biomass ID
	 */
	@Override
	public String getBiomassId() {
		return biomassId;
	}

	/**
	 * @return A map with the environmental conditions
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
		
//		FIXME: change this method URGENT
		String comp = "E";
		for(String c : compartmentSet.keySet()){
			if(c.startsWith("e") || c.startsWith("E")){
				comp = c;
				break;
			}
		}
		return comp;
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
