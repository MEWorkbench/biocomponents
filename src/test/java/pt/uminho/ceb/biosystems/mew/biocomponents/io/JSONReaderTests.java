package pt.uminho.ceb.biosystems.mew.biocomponents.io;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.InvalidBooleanRuleException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLLevel3Reader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSONReader;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

public class JSONReaderTests {
	
	String thisFolder = "./src/test/resources/JsonModels/JSON/";
	String sbmlFolder = "./src/test/resources/JsonModels/SBML/";
	
	@Test
	public void readModelTest01(){
		String modelName = "e_coli_core";
//		String modelName = "iAF1260";
//		String modelName = "iMM904";
//		String modelName = "iCHOv1";
		
		try {
			JSONReader reader = new JSONReader(thisFolder + modelName + ".json", modelName);
			Container cont = new Container(reader);
			
			System.out.println(cont.getReactions().size());
			System.out.println(cont.getMetabolites().size());
			
			System.out.println(cont.getReactions().keySet());
			System.out.println(cont.getMetabolites().keySet());
			
			MapUtils.prettyPrint(cont.getMetabolitesExtraInfo().get(JSONReader.FIELD_NOTES_BIGG_IDS));
			
			
			MapUtils.prettyPrint(cont.getReactionsExtraInfo().get(JSONReader.FIELD_NOTES_BIGG_IDS));
			
		} catch (IOException | InvalidBooleanRuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void compareModelsTest01(){
		String modelName = "e_coli_core";
//		String modelName = "iAF1260";
//		String modelName = "iMM904";
//		String modelName = "iCHOv1";
		
		try {
			JSONReader reader = new JSONReader(thisFolder + modelName + ".json", modelName, true);
			Container contJSON = new Container(reader);
			
			JSBMLLevel3Reader readerSBML = new JSBMLLevel3Reader(sbmlFolder + modelName + ".xml", "1", false);
			Container contSBML = new Container(readerSBML);
			
			Set<String> jsonReac = contJSON.getReactions().keySet();
			Set<String> sbmlReac = contSBML.getReactions().keySet();
			handleSets(jsonReac, sbmlReac, "reactions");
			
			Set<String> jsonMeta = contJSON.getMetabolites().keySet();
			Set<String> sbmlMeta = contSBML.getMetabolites().keySet();			
			handleSets(jsonMeta, sbmlMeta, "metabolites");
			
			String jsonBiomass = contJSON.getBiomassId();
			String sbmlBiomass = contSBML.getBiomassId();
			
			Assert.assertTrue("Different biomassId: " +jsonBiomass + " vs " +sbmlBiomass, jsonBiomass.equals(sbmlBiomass));
			
			for (String meta : sbmlMeta) {
				Set<String> reactions = contJSON.getMetabolites().get(meta).getReactionsId();
				for (String reac : reactions) {
					
					double jsonStoich = contJSON.getMetaboliteStoichiometry(reac, meta);
					double sbmlStoich = contSBML.getMetaboliteStoichiometry(reac, meta);
					
					Assert.assertTrue("Different stoichiometry for reaction: " +reac+ " and metabolite: "+meta+ " : " +jsonStoich + " vs " +sbmlStoich, jsonStoich == sbmlStoich);
				}
			}
			
			System.out.println("Containers are stoichiometrically equals!");
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.assertTrue("UPS!", false);
		}
		
	}
	
	protected void handleSets(Set<String> jsonOriginalSet, Set<String> sbmlOriginalSet, String type){
		Set<String> jsonSet = new HashSet<String>(jsonOriginalSet);
		Set<String> jsonSetAux = new HashSet<String>(jsonSet);
		
		Set<String> sbmlSet = new HashSet<String>(sbmlOriginalSet);
		Set<String> sbmlSetAux = new HashSet<String>(sbmlSet);
		
		jsonSetAux.removeAll(sbmlSet);
		if(!jsonSetAux.isEmpty()){
			System.out.println("Not removed "+ type +" from json container: " + jsonSetAux);
		}else{
			System.out.println("All "+ type +" were removed from json container");
		}
		
		sbmlSetAux.removeAll(jsonSet);
		if(!sbmlSetAux.isEmpty()){
			System.out.println("Not removed " + type + " from sbml container: " + sbmlSetAux);
		}else{
			System.out.println("All " + type + " were removed from sbml container");
		}
		
		Assert.assertTrue("Different " + type + "!", jsonSet.equals(sbmlSet));
	}

}
