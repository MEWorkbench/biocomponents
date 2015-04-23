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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.interfaces.IContainerBuilder;


public class BioOptFileReader implements IContainerBuilder{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean generateIDs = false;
	
	private ArrayList<String> reactionsLine;
	private ArrayList<String> externalMetaboliteLine;
	private ArrayList<String> constraintsLine;
	
	private Pattern patterFraction = Pattern.compile("(\\d+)/(\\d+)");
	private Pattern patterValue = Pattern.compile("\\d+\\.?\\d*");
	private Pattern defaultEcPatter = Pattern.compile("(.+)\\[(.+),(.+)\\]");
	private Pattern splitStoiq = Pattern.compile(".*\\s.*");
	
	private String biomassId;
	private String organismName;
	private String modelName;
	private int version;
	
	private int globlalMetIdx;
//	private String caractersToReplace = "[() -,;+]";
	
	private Map<String,Integer> repetedReactions;
	
	protected HashMap<String, ReactionConstraintCI> defaultEC = null;
	protected HashMap<String, CompartmentCI> compartmentList = null;
	protected HashMap<String, MetaboliteCI> metaboliteList = null;
	protected HashMap<String, ReactionCI> reactionList = null;
	
	protected Map<String, String> dicMetabolites;

	private Map<String, Map<String, String>> metExtraInfo;

	private Map<String, Map<String, String>> reactionExtraInfo;
	
	public BioOptFileReader(String path) throws IOException, Exception{
		this(path, false);
	}
	public BioOptFileReader(String path, boolean generateIds) throws IOException, Exception{
		
		this.generateIDs = generateIds;
		globlalMetIdx = 0;
		dicMetabolites = new HashMap<String, String>();
		separateLineFile(path);
		parserReactions();
		parserDefaultEC();
		
		metExtraInfo = new HashMap<String, Map<String,String>>();
		reactionExtraInfo = new HashMap<String, Map<String,String>>();
	}
	
	public void separateLineFile(String path) throws IOException, Exception{
		
		
		reactionsLine = new ArrayList<String>();
		externalMetaboliteLine = new ArrayList<String>();
		constraintsLine = new ArrayList<String>();
		repetedReactions = new HashMap<String, Integer>();
		
		Set<String> aux = new TreeSet<String>();
		
		FileReader f = new FileReader(path);
		BufferedReader r = new BufferedReader(f);
		
		/** read reversible reactions */
		String str = null;
		boolean inReactions = false;
		boolean inConstraints = false;
		boolean inExtMeta = false;
		boolean inObjective = false;
		
		boolean isCommet = false;
//		int reaction_n = 0;
		for(; r.ready();)
		{

			str = r.readLine().trim();
			
			if(str.startsWith("%")){
				isCommet = !isCommet;
//				System.out.println("Detect % Comment " + str);
			}else if(str.startsWith("#") || isCommet || str.equals("")){
//				System.out.println("Comment " + str);
			}else{
//				System.out.println(str);
				if(str.equals("-REACTIONS")){
					inReactions = true;
					inConstraints = false;
					inExtMeta = false;
					inObjective = false;
					
				}else if(str.equals("-EXTERNAL METABOLITES") || str.equals("-UNCONSTRAINED METABOLITES") 
						|| str.equals("-EXTRACELLULAR METABOLITES (UNCONSTRAINED)") || str.equals("-EXTERNAL METABOLITES (UNCONSTRAINED)")){
					inReactions = false;
					inConstraints = false;
					inExtMeta = true;
					inObjective = false;
				}else if(str.equals("-CONSTRAINTS")){
					inReactions = false;
					inConstraints = true;
					inExtMeta = false;
					inObjective = false;
				}else if(str.equals("-MAXIMIZE") || str.equals("-MINIMIZE") ||  str.equals("-OBJECTIVE") ||  str.equals("-OBJ")){
					inReactions = false;
					inConstraints = false;
					inExtMeta = false;
					inObjective = true;
				}else{
					
					
					if(inReactions){
//						reaction_n++;
//						System.out.println(str+"_"+reaction_n);
						String reactionId = str.split(":")[0].trim();
						
						if(aux.contains(reactionId))
							repetedReactions.put(reactionId,1);
						else
							aux.add(reactionId);
						
						reactionsLine.add(str);
					}
					if(inConstraints)
						constraintsLine.add(str);
					if(inExtMeta)
						externalMetaboliteLine.add(str);
					if(inObjective){
						biomassId = str.replaceAll("\\s*min\\s+|\\*max\\s+", "");
						
					}
					
//					System.out.println(str);
				}
			}
		}
		
		r.close();
		f.close();
	}
	
	private String generateNextMetaboliteId(){
		globlalMetIdx++;
		return "M"+globlalMetIdx;
	}
	
	private void parserReactions(){
		
		
		metaboliteList = new HashMap<String, MetaboliteCI>();
		reactionList = new HashMap<String, ReactionCI>();
		
		compartmentList = new HashMap<String, CompartmentCI>();
		CompartmentCI extComp = new CompartmentCI("External", "External", null);
	
		for(String extMet : externalMetaboliteLine){
			
			String metId = extMet;
			if(generateIDs) metId=generateNextMetaboliteId();
			dicMetabolites.put(extMet, metId);
//			String safeId = "_"+extMet.replaceAll(caractersToReplace, "_");
			extComp.addMetaboliteInCompartment(metId);
			metaboliteList.put(metId, new MetaboliteCI(metId, extMet));
		}
		compartmentList.put("External", extComp);
		
		CompartmentCI comp = new CompartmentCI("Internal", "Internal", "External");
		compartmentList.put("Internal", comp);
		
		for(String reactionStr : reactionsLine){
			String[] data = reactionStr.split(":");
			
			String reactionId = data[0].trim();
			
			if(repetedReactions.containsKey(reactionId)){
				Integer value = repetedReactions.get(reactionId);
				String newReactionId= reactionId+"_"+value;
				value++;
				repetedReactions.put(reactionId, value);
				reactionId = newReactionId;
			}
			
			
			String reactionFormual = data[1].trim();
			
//			System.out.print(reactionId+" : ");
			
			
			boolean reversibility = true;
			if(reactionFormual.contains(" -> ")) reversibility = false;
			
				
			data = reactionFormual.split("\\s+<->\\s+|\\s+->\\s+");
			String reactants = data[0];
			String products = data[1];
			
//			System.out.print(reactants);
//			if(reversibility)
//				System.out.print(" <-> ");
			
//			System.out.println(reactionId+"\t"+reactionFormual + "\t" +reversibility + "\t" + reactants + "\t" + products);
			
//			System.out.println(reactionStr);
//			System.out.println("id: " + reactionId);
//			System.out.println("Reverse?: "+ reversibility);
//			System.out.println("reactants: " + data[0]);
//			System.out.println("Profducts: " + data[1]);
			
			data = reactants.split("\\s+\\+\\s+");
			
			Map<String, StoichiometryValueCI> reactantsValues = new HashMap<String, StoichiometryValueCI>();
			for(int i =0; i < data.length ; i++){
				String info = data[i].trim();
				StoichiometryValueCI value = contructStoiquiometry(info);
//				if( value!= null){
					reactantsValues.put(value.getMetaboliteId(), value);
					metaboliteList.get(value.getMetaboliteId()).addReaction(reactionId);
//				}
			}
			
			Map<String, StoichiometryValueCI> productsValues = new HashMap<String, StoichiometryValueCI>();
			data = products.split("\\s+\\+\\s+");
			for(int i =0; i < data.length ; i++){
				String info = data[i].trim();
				StoichiometryValueCI value = contructStoiquiometry(info);
//				if( value!= null){
					productsValues.put(value.getMetaboliteId(), value);
					metaboliteList.get(value.getMetaboliteId()).addReaction(reactionId);
//				}
			}
			
			
			
			ReactionCI reactionValue = new ReactionCI(reactionId, reactionId, reversibility, reactantsValues, productsValues);
			
			if(!reactionList.containsKey(reactionId))
			reactionList.put(reactionId, reactionValue);
			
//			System.out.println();
			
			
		}
	}
	
	
	private StoichiometryValueCI contructStoiquiometry(String info){
		
//		System.out.println("."+info+".");
		
		
		String stoiqStr = null;
		Double stoi = null;
		String metaboliteId = null;
		
		
		Matcher matcher = splitStoiq.matcher(info);
		
		String[] data = info.split("\\s");
		if(matcher.find())
			stoiqStr = data[0];
		
		

		if(stoiqStr != null){
			matcher = patterFraction.matcher(stoiqStr);
			
			if(matcher.find()){
				String firtnumber = matcher.group(1).trim();
				String secondNumber = matcher.group(2).trim();
				
				double num1 = Double.parseDouble(firtnumber);
				double num2 = Double.parseDouble(secondNumber);
				
				stoi = num1/num2;
//				System.out.println("Fraction");
			}else{
				
				matcher = patterValue.matcher(stoiqStr);
				if(matcher.find()){
					try{
						stoi = Double.parseDouble(stoiqStr);
					}catch (NumberFormatException e) {
						stoi = null;
//						System.out.println(e);
					}
				}
			}
		}
		
		
		if(stoi == null){
			stoi = 1.0;
			metaboliteId = info;
		}else{
			metaboliteId = data[1];
			for(int i = 2; i < data.length; i++)
			metaboliteId += " "+data[i];
		}
		
//		System.out.println(stoi + " ==> "+ metaboliteId);
		
		StoichiometryValueCI value = null;
		
		String metId = metaboliteId;
		if(dicMetabolites.containsKey(metaboliteId))
			metId = dicMetabolites.get(metaboliteId);
		else{
			if(generateIDs)
			metId = generateNextMetaboliteId();
			dicMetabolites.put(metaboliteId, metId);
			metaboliteList.put(metId, new MetaboliteCI(metId, metaboliteId));
		}
		
		if(!externalMetaboliteLine.contains(metaboliteId)){
			
			compartmentList.get("Internal").addMetaboliteInCompartment(metId);
			value = new StoichiometryValueCI(metId, stoi, "Internal");
		}else
			value = new StoichiometryValueCI(metId, stoi, "External");

		

		
		return value;
	}
	
	private void parserDefaultEC(){
		
		defaultEC = new HashMap<String, ReactionConstraintCI>();
		for(String info : constraintsLine){
			
			
			Matcher matcher = defaultEcPatter.matcher(info);
			
			if(matcher.find()){
				String reactionId = matcher.group(1).trim();
				String value1 = matcher.group(2).trim();
				String value2 = matcher.group(3).trim();
				
//				System.out.println(reactionId + "\t" + value1 + "\t" + value2);
				
				Double lb = Double.parseDouble(value1);
				Double up = Double.parseDouble(value2);
				
				defaultEC.put(reactionId, new ReactionConstraintCI(lb, up));
				
			}
		}
	}

	@Override
	public String getBiomassId() {
		return biomassId;
	}

	@Override
	public HashMap<String, ReactionConstraintCI> getDefaultEC() {
		return defaultEC;
	}

	@Override
	public Map<String, GeneCI> getGenes() {
		return null;
	}

	@Override
	public HashMap<String, CompartmentCI> getCompartments() {
		return compartmentList;
	}

	@Override
	public HashMap<String, MetaboliteCI> getMetabolites() {
		return metaboliteList;
	}

	@Override
	public HashMap<String, ReactionCI> getReactions() {
		return reactionList;
	}

	@Override
	public String getModelName() {
		return modelName;
	}

	@Override
	public String getNotes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOrganismName() {
		return organismName;
	}

	@Override
	public Integer getVersion() {
		return version;
	}
	
	
	public Map<String, Integer> getRepetedReactions() {
		return repetedReactions;
	}

	@Override
	public String getExternalCompartmentId() {
		return "External";
	}

	@Override
	public Map<String, Map<String, String>> getMetabolitesExtraInfo() {
		return metExtraInfo;
	}

	@Override
	public Map<String, Map<String, String>> getReactionsExtraInfo() {
		return reactionExtraInfo;
	}

	

	
	
}
