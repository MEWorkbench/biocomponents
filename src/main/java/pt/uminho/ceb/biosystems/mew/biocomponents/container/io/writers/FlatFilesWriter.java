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
package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.writers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionTypeEnum;
import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;

public class FlatFilesWriter {

	protected Container container;
	protected Boolean hasGeneRules;
	
	protected Boolean externalMetabolitesFile = false;
	protected Boolean useCompartments = true;
	protected Boolean includeDrains = true;
	protected String[] filesPath;
	protected Map<String, Delimiter> delimiters;
	
	protected String[] saveMetabolites;
	protected String[] saveReactions;
	protected double[][] saveMatrix;
	protected ArrayList<String> reactionsToRemove;
	
	public static final double DEFAULT_UPPER_LIMIT = 10000;
	public static final double DEFAULT_LOWER_LIMIT = -10000;
	
	public static final String MATRIX_DELIMITER = "matrixDelimiter";
	public static final String REACTIONS_DELIMITER = "reactionsDelimiter";
	public static final String METABOLITES_DELIMITER = "metabolitesDelimiter";
	public static final String GENES_DELIMITER = "genesDelimiter";
	
	public FlatFilesWriter(String matrix, String reactions, String metabolites, String genes, boolean hasGene, Container container, Boolean useCompartments, boolean includeDrains) {
		this(matrix, reactions, metabolites, genes, hasGene, container, useCompartments, includeDrains, new HashMap<String, Delimiter>());
		delimiters.put(MATRIX_DELIMITER, Delimiter.TAB);
		delimiters.put(REACTIONS_DELIMITER, Delimiter.COMMA);
		delimiters.put(METABOLITES_DELIMITER, Delimiter.TAB);
		delimiters.put(GENES_DELIMITER, Delimiter.EQUALS);
	}
	
	public FlatFilesWriter(String matrix, String reactions, String metabolites, String genes, boolean hasGene, Container container, Boolean useCompartments, boolean includeDrains, Map<String, Delimiter> delimiters) {
		this.container = container;
		this.useCompartments = useCompartments;
		this.includeDrains = includeDrains;
		
		hasGeneRules = hasGene;
		
		filesPath = new String[4];
		filesPath[0] = matrix;
		filesPath[1] = reactions;
		filesPath[2] = metabolites;
		filesPath[3] = genes;
		this.delimiters = delimiters;

	}
	
	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
		
		filesPath[0] = container.getModelName()+ ".mat";
		filesPath[2] = container.getModelName()+".flu";
		filesPath[0] = container.getModelName()+".met";
		filesPath[3] = container.getModelName()+".grr";
	}


	public String getMatrixFile() {
		return filesPath[0];
	}

	public void setMatrixFile(String matrixFile) {
		filesPath[0] = container.getModelName()+ ".mat";
	}

	public String getFluxFile() {
		return filesPath[1];
	}

	public void setFluxFile(String fluxFile) {
		filesPath[1] = container.getModelName()+".flu";
	}

	public String getMetaboliteFile() {
		return filesPath[2];
	}

	public void setMetaboliteFile(String metaboliteFile) {
		filesPath[2] = container.getModelName()+".met";
	}

	public Boolean getExternalMetabolitesFile() {
		return externalMetabolitesFile;
	}

	public void setExternalMetabolitesFile(Boolean externalMetabolitesFile) {
		this.externalMetabolitesFile = externalMetabolitesFile;
	}
		
	public Boolean getIncludeDrains() {
		return includeDrains;
	}

	public void setIncludeDrains(Boolean includeDrains) {
		this.includeDrains = includeDrains;
	}
	
	public String getGeneRulesFile() {
		return filesPath[3];
	}

	public void setGeneRulesFile(String geneRulesFile) {
		filesPath[3] = container.getModelName()+".grr";
	}

	/**
	 * This method creates the sparse matrix file
	 * @throws IOException IOException
	 */
	private void createSparseMatrixFile() throws IOException{
		
		FileWriter file = new FileWriter(getMatrixFile());
		BufferedWriter writer = new BufferedWriter(file);
		
		ArrayList<Integer> row = new ArrayList<Integer>();
		ArrayList<Integer> column = new ArrayList<Integer>();
		ArrayList<Double> value = new ArrayList<Double>();

		getNonZeros(row, column, value);
		
		for(int i =0; i < row.size(); i++){
			writer.write(row.get(i)+delimiters.get(MATRIX_DELIMITER).toString()+column.get(i)+delimiters.get(MATRIX_DELIMITER).toString()+value.get(i)+"\n");
		}
		writer.close();
		file.close();
	}
	
	/**
	 * This method gets all the values different from 0 in the matrix saveMatrix, and saves it in a ArrayList structure
	 * @param row An ArrayList object with the indexes of the rows
	 * @param column An ArrayList object with the indexes of the columns
	 * @param value An ArrayList object where the values different from 0 will be saved
	 */
	private void getNonZeros(ArrayList<Integer> row, ArrayList<Integer> column, ArrayList<Double> value){
		for(int i=0; i<saveMatrix.length; i++){
			for(int j=0; j<saveMatrix[0].length; j++){
				if(saveMatrix[i][j] != 0){
					row.add(i);
					column.add(j);
					value.add(saveMatrix[i][j]);
				}
			}
		}
	}

	/**
	 * This method creates the reactions file
	 * @throws IOException IOException
	 */
	private void createFluxFile() throws IOException{
		
		FileWriter fileFluxes = new FileWriter(getFluxFile());
		BufferedWriter writerFluxes = new BufferedWriter(fileFluxes);
		
		Map<String, ReactionCI> fluxes =  container.getReactions();		
		System.out.println("numero de fluxos: "+fluxes.size());
		reactionsToRemove = new ArrayList<String>();
		if(!includeDrains) this.saveReactions = new String[container.getReactionsNotDrains().values().size()];
		else this.saveReactions = new String[fluxes.values().size()];
		
		int i = 0;
		
		for(ReactionCI r : fluxes.values()){
			
			if(!(r.getType().equals(ReactionTypeEnum.Drain) && !includeDrains)||!r.getType().equals(ReactionTypeEnum.Drain)){
					String id = r.getId();
					ReactionConstraintCI constrains = null;
					if(container.getDefaultEC().get(r.getId())!=null){
						constrains = container.getDefaultEC().get(r.getId());
					}
			
				double lo, up;
				if(constrains != null){
					lo = constrains.getLowerLimit();
					up = constrains.getUpperLimit();
				}
				else{
					if(r.getReversible())
						lo = DEFAULT_LOWER_LIMIT;
					else
						lo = 0;
					up = DEFAULT_UPPER_LIMIT;
				}
				
				writerFluxes.write(id+delimiters.get(REACTIONS_DELIMITER).toString()+lo+delimiters.get(REACTIONS_DELIMITER).toString()+up+"\n");
				this.saveReactions[i] = id;
				i++;
			}
		}
		writerFluxes.close();
		fileFluxes.close();
	}
	
	/**
	 * This method removes a column from the matrix saveMatrix
	 * @param index The inex of the column that will be removed
	 */
	private void removeColumn(int index){
		double[][] mat = new double[saveMatrix.length][saveMatrix[0].length-1];
		int new_j = 0;
		
		for(int i=0; i<saveMatrix.length; i++){
			for(int j=0; j<saveMatrix[0].length;j++){
				if(j != index){
					mat[i][new_j] = saveMatrix[i][j];
					new_j++;
				}
			}
		}
		this.saveMatrix = mat;
	}
	
	/**
	 * This method creates the GeneRules file
	 * @throws IOException IOException
	 */
	private void createReactioRulesFile() throws IOException{
		
		FileWriter file = new FileWriter(getGeneRulesFile());
		BufferedWriter writer = new BufferedWriter(file);
		
		for(String reaction : container.getReactions().keySet()){
			String stringRule = "";
			
			if(container.getReactions().get(reaction).getGeneRule() != null && container.getReactions().get(reaction).getGeneRule().getRootNode() != null){
				stringRule = container.getReactions().get(reaction).getGeneRule().toString();
			}
			
			if(stringRule != "") writer.write(reaction +delimiters.get(GENES_DELIMITER)+stringRule+"\n");
		}
		
		writer.close();
		file.close();
	}
	
	/**
	 * This method creates the metabolites file
	 * @throws IOException IOException
	 */
	private void createMetabolitesFile() throws IOException{
		
		FileWriter file = new FileWriter(getMetaboliteFile());
		BufferedWriter writer = new BufferedWriter(file);
		Map<String, MetaboliteCI> metabolites = container.getMetabolites();
		
		this.saveMetabolites = new String[metabolites.size()];
		int i = 0;
		for(MetaboliteCI met : metabolites.values()){
			String id = met.getId();
			writer.write(id);
			this.saveMetabolites[i] = id;
			i++;
			
			if(useCompartments){
				String x = "";
				boolean found = false;
				for(String comp : container.getCompartments().keySet()){
					if(!found){
						if(container.getCompartments().get(comp).getMetabolitesInCompartmentID().contains(met.getId())){
							//FIXME: CHECK THIS!
							x = comp.substring(0, 1);
							found = true;
						}
					}
				}
				writer.write(delimiters.get(METABOLITES_DELIMITER).toString()+x);
			}
			writer.write("\n");
		}
		writer.close();
		file.close();
	}
	
	public void setDelimiters(Map<String,Delimiter> delimiters){
		this.delimiters = delimiters;
	}
	
	/**
	 * This method writes the Flat Files
	 * @throws IOException IOException
	 */
	public void writeFlatFiles() throws IOException{
		
		
		createFluxFile();
		createMetabolitesFile();
		saveMatrix = new double[saveMetabolites.length][saveReactions.length];
		createSaveMatrix();
		createSparseMatrixFile();
		if(hasGeneRules)
			createReactioRulesFile();
		
	}
	
	/**
	 * This method creates a dense matrix and saves it in the class variable saveMatrix
	 */
	private void createSaveMatrix() {
//		boolean first, firstall = true;
//		System.out.println("saveReactions.length: |"+saveReactions.length+"| container.getReactions().size(): |"+container.getReactions().size()+"|");
//		System.out.println("saveMetabolites.length: |"+saveMetabolites.length+"| container.getMetabolites().size(): |"+container.getMetabolites().size()+"|");
		
//		System.out.println("container.getReactions().get(saveReactions[0]): |"+container.getReactions().get(saveReactions[0]).getId()+"|");
//		System.out.println("container.getReactions().get(saveReactions[1755]): |"+container.getReactions().get(saveReactions[1755]).getId()+"|");
//		System.out.println("container.getReactions().get(saveReactions[2112]): |"+container.getReactions().get(saveReactions[2112]).getId()+"|");
		for(int i=0; i<this.saveReactions.length; i++){
//			first = true;
//			System.out.println("container.getReactions()./home/vitorget(saveReactions["+i+"]): |"+container.getReactions().get(saveReactions[i]).getId()+"|");
			for(int j=0; j<this.saveMetabolites.length; j++){
//				System.out.println("o i is |"+i+"| e o j is |"+j+"|");
//				if(first) System.out.println("saveReactions[i]: |"+saveReactions[i]+"|");
//				System.out.println("saveMetabolites[j]: |"+saveMetabolites[j]+"|");
//				if(firstall) System.out.println("container: |"+container+"|");
//				if(firstall) System.out.println("container.getReactions(): |"+container.getReactions()+"|");
//				if(first) System.out.println("container.getReactions().get(saveReactions[i]): |"+container.getReactions().get(saveReactions[i])+"|");
//				if(first) System.out.println("container.getReactions().get(saveReactions[i]).getReactants(): |"+container.getReactions().get(saveReactions[i]).getReactants()+"|");
//				System.out.println("container.getReactions().get(saveReactions[i]).getReactants().containsKey(saveMetabolites[j]): |"+container.getReactions().get(saveReactions[i]).getReactants().containsKey(saveMetabolites[j])+"|\n");
//				first = false;
//				firstall = false;
				if(container.getReactions().get(saveReactions[i]).getReactants().containsKey(saveMetabolites[j])){
					saveMatrix[j][i] = container.getReactions().get(saveReactions[i]).getReactants().get(saveMetabolites[j]).getStoichiometryValue() * -1;
				}
				else if(container.getReactions().get(saveReactions[i]).getProducts().containsKey(saveMetabolites[j])){
					saveMatrix[j][i] = container.getReactions().get(saveReactions[i]).getProducts().get(saveMetabolites[j]).getStoichiometryValue();
				}
				else saveMatrix[j][i] = 0;
			}
		}
	}

	/*
	 * This method creates a dense matrix and saves it in the class variable saveMatrix
	 * @param model  model
	 * @param isMetEIBand isMetEIBand
	 * @param filePaths list of file Paths position 0 matrix file Path
	 *                        					    1 flux file path
	 *                          					2 metabolites file path
	 *                          					3 gene reaction rules file Path
	 * @param delim list of file delimiters position 0 matrix file Path
	 *                        					     1 flux file path
	 *                          					 2 metabolites file path
	 * @throws IOException IOException
	 */
	static public void writeFlatFiles(String matrix, String reactions, String metabolites, String genes, boolean hasGene, Container container, boolean isMetEIBand, ArrayList<String> filePaths,Map<String, Delimiter>delim,Boolean isbandMetEI, boolean includeDrains) throws IOException{
		
		
		FlatFilesWriter writer = new FlatFilesWriter(matrix, reactions, metabolites, genes, hasGene, container, isbandMetEI, includeDrains);
		
		writer.setIncludeDrains(includeDrains);
		writer.setExternalMetabolitesFile(isMetEIBand);
		writer.setFilePaths(filePaths);
		writer.setDelimiters(delim);
		
		
		writer.writeFlatFiles();
		
	}

	protected void setFilePaths(ArrayList<String> filePaths) {
		this.filesPath = filePaths.toArray(filesPath);
	}	
}
