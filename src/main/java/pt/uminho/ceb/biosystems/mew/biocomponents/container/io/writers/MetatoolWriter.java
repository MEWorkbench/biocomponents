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
import java.util.List;
import java.util.Map;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionTypeEnum;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;

public class MetatoolWriter{// implements IModelWriter{

	
	protected String filePath;
	protected Container container;
	
	public final static String DEFAULT_METATOOL_EXTERNAL_COMPARTMENT = "e";
	public final static String DEFAULT_METATOOL_INTERNAL_COMPARTMENT = "c";
	
	public MetatoolWriter(String filePathString,Container container) {
		filePath = filePathString;
		this.container = container; 
	}

	public void writeModel() throws IOException{
		FileWriter file = new FileWriter(filePath);
		BufferedWriter writer = new BufferedWriter(file);
		
		createMetaboliteList(writer);
		createReactionListDescription(writer);
		
		writer.close();
	}
	
	/**
	 * This method writes the reactions in the metatool file
	 * @param writer A BufferedWriter object
	 * @throws IOException
	 */
	protected void createReactionListDescription(BufferedWriter writer) throws IOException{
		Map<String, ReactionCI> fluxes = container.getReactions();
		String cAT = "-CAT";
		List<String> enzymeReversibleList = new ArrayList<String>();
		List<String> enzymeIrreversibleList = new ArrayList<String>();
		
		for(String reacId : fluxes.keySet()){
			ReactionCI reaction = fluxes.get(reacId);
			String reactionId = reaction.getId();
			if(reaction.getType() != ReactionTypeEnum.Drain)
				if(reaction.isReversible()) enzymeReversibleList.add(reactionId);
				else enzymeIrreversibleList.add(reactionId);
			List<Pair<String,Double>> reactantMetaboliteList= new ArrayList<Pair<String,Double>>();
			List<Pair<String,Double>> productMetaboliteList = new ArrayList<Pair<String,Double>>();
			fillReactionInformation(reacId,reactantMetaboliteList,productMetaboliteList);
			if((reactantMetaboliteList.size() != 0) && (productMetaboliteList.size() != 0)){
				cAT += "\n";
				String reactionDescriptionString = createReactionString(reactionId,reactantMetaboliteList,productMetaboliteList);
				cAT += reactionDescriptionString;
			}
		}
		writeFileData(writer,enzymeIrreversibleList,enzymeReversibleList,cAT);
	}
	
	/**
	 * This method writes the reversible and reversible reactions IDs and the reactions in the metatool file
	 * @param writer A BufferedWriter object
	 * @param enzymeIrreversibleList A list with the irreversible reactions IDs
	 * @param enzymeReversibleList A list with the reversible reactions IDs
	 * @param cAT A String with all the reactions (the "CAT" section of the metatool files)
	 * @throws IOException
	 */
	protected void writeFileData(BufferedWriter writer,List<String> enzymeIrreversibleList,	List<String> enzymeReversibleList, String cAT) throws IOException {
		writer.write("-ENZIRREV\n");
		writeReactionList(writer,enzymeIrreversibleList);
		writer.write("\n\n");
		writer.write("-ENZREV\n");
		writeReactionList(writer,enzymeReversibleList);
		writer.write("\n\n");
		writer.write(cAT);
		
	}

	/**
	 * This method creates the reactions string
	 * @param reactionId The reaction ID
	 * @param reactantMetaboliteList  A list with the reactants
	 * @param productMetaboliteList A list with the products
	 * @return The equation
	 */
	private String createReactionString(String reactionId,List<Pair<String, Double>> reactantMetaboliteList,List<Pair<String, Double>> productMetaboliteList) {
		String reactionString = reactionId + ": ";
		String reactantReactionTerm = createReactionTerm(reactantMetaboliteList);
		String productReactionTerm = createReactionTerm(productMetaboliteList);
		return reactionString+reactantReactionTerm+" = "+productReactionTerm;
	}

	/**
	 * This method creates a reaction term
	 * @param metaboliteList A list with the metabolites and the stoichiometric value for the reaction term
	 * @return A String with the reaction term
	 */
	private String createReactionTerm(List<Pair<String, Double>> metaboliteList){
		String reactionTerm = new String();
		
		for(Pair<String,Double> stoichiometricInformation:metaboliteList){
			String metaboliteId = stoichiometricInformation.getValue();
			Double stoichiometricCoefficient = stoichiometricInformation.getPairValue();
			if(stoichiometricCoefficient != 1)
				if(reactionTerm.compareTo("") == 0)
					reactionTerm += stoichiometricCoefficient+" "+metaboliteId;
				else
					reactionTerm += " + "+stoichiometricCoefficient+" "+metaboliteId;
			else 
				if(reactionTerm.compareTo("") == 0)
					reactionTerm += metaboliteId;
				else
					reactionTerm += " + "+metaboliteId;
		}
		return reactionTerm;
	}

	/**
	 * This method writes the reaction list
	 * @param writer A BufferedWriter object
	 * @param enzymeList A list with the enzymes
	 * @throws IOException
	 */
	protected void writeReactionList(BufferedWriter writer, List<String> enzymeList) throws IOException {
		String stringList = new String();
		
		for(String enzymeId:enzymeList)
			if(stringList.compareTo("") == 0)
				stringList += enzymeId;
			else
				stringList += " "+enzymeId;
				
		
		writer.write(stringList);
	}

	/**
	 * This method evaluates the reaction constraints
	 * @param reactionId The reaction ID
	 * @param reactionConstraint The reaction constraints
	 * @param enzymeIrreversibleList A list with the irreversible reactions
	 * @param enzymeReverseList A list with the reversible reactions
	 */
	private void evaluateReactionConstraints(String reactionId,ReactionConstraintCI reactionConstraint, List<String> enzymeIrreversibleList,List<String> enzymeReverseList) {
		double reactionUpperLimit = reactionConstraint.getUpperLimit();
		double reactionLowerLimit = reactionConstraint.getLowerLimit();
		
		if(((reactionLowerLimit == 0) || (reactionUpperLimit == 0)) || ((reactionUpperLimit > 0) && (reactionLowerLimit > 0))  
			  || ((reactionUpperLimit < 0) && (reactionLowerLimit < 0)))
			enzymeIrreversibleList.add(reactionId);
		else
			enzymeReverseList.add(reactionId);
			
		
	}

	/**
	 * This method fills the reaction informations
	 * @param reactionId The reaction ID
	 * @param reactantMetaboliteList A list with the reactants
	 * @param productMetaboliteList A list with the products
	 */
	protected void fillReactionInformation(String reactionId,List<Pair<String,Double>> reactantMetaboliteList,List<Pair<String,Double>> productMetaboliteList) {
		ReactionCI reaction = container.getReactions().get(reactionId);
		ReactionTypeEnum reactionType = reaction.getType();
		Map<String, StoichiometryValueCI> reactants = reaction.getReactants();
		Map<String, StoichiometryValueCI> products = reaction.getProducts();
		
		if(reactionType != ReactionTypeEnum.Drain){
			for(String reactant : reactants.keySet()){
				double stoichiometricCoefficient = reactants.get(reactant).getStoichiometryValue();
				evaluateStoichiometricCoefficient(reactantMetaboliteList,stoichiometricCoefficient,reactant);
			}
			
			for(String product : products.keySet()){
				double stoichiometricCoefficient = products.get(product).getStoichiometryValue();
				evaluateStoichiometricCoefficient(productMetaboliteList,stoichiometricCoefficient,product);
			}			
		}
	}

	/**
	 * This method evaluates the stoichiometric values
	 * @param MetaboliteList A list with the metabolites and its stoichiometric value
	 * @param stoichiometricCoefficient A stoichiometric value
	 * @param metaboliteId The metabolite ID
	 */
	protected void evaluateStoichiometricCoefficient(List<Pair<String,Double>> MetaboliteList,double stoichiometricCoefficient, String metaboliteId) {
		
		if(stoichiometricCoefficient == 0)
			return;
		
		MetaboliteList.add(new Pair<String, Double>(metaboliteId,stoichiometricCoefficient));
	}

	
	//FIXME: this method cannot be present in this class, the BioComponents project does not know the Interface ISteadyStateModel
//	protected void addStoichiometricInformationToString(ISteadyStateModel model,int metaboliteIndex, double stoichiometricCoefficient, String metaboliteListString){
//		String metaboliteId = model.getMetaboliteId(metaboliteIndex);
//		
//		if (metaboliteListString.compareTo("") != 0)
//			metaboliteListString += " + ";
//		
//		double value = Math.abs(stoichiometricCoefficient);
//		
//		if(value > 1)
//			metaboliteListString += value+ " " + metaboliteId;
//		else
//			metaboliteListString += metaboliteId;
//	}

	/**
	 * This method creates the metabolite list
	 * @param writer A BufferedWriter object
	 * @throws IOException
	 */
	protected void createMetaboliteList(BufferedWriter writer) throws IOException {
		String externalMetaboliteString = "";
		String internalMetaboliteString = "";
		Map<String, MetaboliteCI> metabolitesHashMap = container.getMetabolites();
		Map<String, CompartmentCI> compartmentsHashMap = container.getCompartments();
		
		for(String meta : metabolitesHashMap.keySet()){
			for(String comp : compartmentsHashMap.keySet()){
				if(compartmentsHashMap.get(comp).getMetabolitesInCompartmentID().contains(meta)){
					if(compartmentsHashMap.get(comp).getId() == DEFAULT_METATOOL_EXTERNAL_COMPARTMENT){
						if(externalMetaboliteString.compareTo("") != 0)
							externalMetaboliteString += " ";
						externalMetaboliteString += meta;
					}
					else{
						if(internalMetaboliteString.compareTo("") != 0)
							internalMetaboliteString += " ";
						internalMetaboliteString += meta;
					}
				}
			}
		}
		writer.write("-METINT\n"+internalMetaboliteString+"\n\n"+"-METEXT\n"+externalMetaboliteString+"\n\n");
	}
}
