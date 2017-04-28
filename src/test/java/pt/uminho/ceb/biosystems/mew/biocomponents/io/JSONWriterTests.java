package pt.uminho.ceb.biosystems.mew.biocomponents.io;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.InvalidBooleanRuleException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLLevel3Reader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSONReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.writers.JSONWriter;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.map.MapUtils;

public class JSONWriterTests {

	protected static String JSON_FOLDER = "./../biocomponents/src/test/resources/JsonModels/JSON/";
	protected static String SBML_FOLDER = "./../biocomponents/src/test/resources/JsonModels/SBML/";
	protected static String WRITE_FOLDER = "./../biocomponents/src/test/resources/JsonModels/Write/";
	
	protected Container contSBML;
	protected Container contJSON;
	
	String modelName;
	
	@Before
	public void setData() throws Exception {
		modelName = "e_coli_core";
//		modelName = "iAF1260";
//		modelName = "iMM904";
//		modelName = "iCHOv1";
		
		JSBMLLevel3Reader readerSBML = new JSBMLLevel3Reader(SBML_FOLDER + modelName + ".xml", "1", false);
//		JSONReader readerJson = new JSONReader(JSON_FOLDER + modelName + ".json");
		
		// Container
		contSBML = new Container(readerSBML);
//		Set<String> met = contSBML.identifyMetabolitesIdByPattern(Pattern.compile(".*_b"));
//		contSBML.removeMetabolites(met);
		
//		contJSON = new Container(readerJson);
		
	}
	
	@Test
	public void writeModelTest01(){
		
		try {
			JSONWriter writer = new JSONWriter(WRITE_FOLDER + modelName + ".json", contSBML);
			
			writer.writeToFile(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void writeAndReadModelTest01(){
		
		try {
			JSONWriter writer = new JSONWriter(WRITE_FOLDER + modelName + ".json", contSBML);
			
			writer.writeToFile(false);
			
			JSONReader readerJson = new JSONReader(WRITE_FOLDER + modelName + ".json", modelName);
			contSBML = new Container(readerJson);
		} catch (IOException | InvalidBooleanRuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void readAndWriteModelTest01(){
		
		try {
			JSONReader readerJson = new JSONReader(JSON_FOLDER + modelName + ".json", modelName);
			Container cont = new Container(readerJson);
			
			MapUtils.prettyPrint(cont.getMetabolitesExtraInfo().get(JSONReader.FIELD_NOTES_BIGG_IDS));
			
			
			MapUtils.prettyPrint(cont.getReactionsExtraInfo().get(JSONReader.FIELD_NOTES_BIGG_IDS));
			
			JSONWriter writer = new JSONWriter(WRITE_FOLDER + modelName + ".json", cont);
			
			writer.writeToFile(false);
		} catch (IOException | InvalidBooleanRuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void compareModelsTest01(){
//		String modelName = "e_coli_core";
//		String modelName = "iAF1260";
//		String modelName = "iMM904";
		String modelName = "iCHOv1";
		
		try {
			JSONReader reader = new JSONReader(WRITE_FOLDER + modelName + ".json", modelName, true);
			Container contJSON = new Container(reader);
			
			JSBMLLevel3Reader readerSBML = new JSBMLLevel3Reader(SBML_FOLDER + modelName + ".xml", "1", false);
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
	
	@Test
	public void compareModelsTest02(){
		String modelName = "e_coli_core";
//		String modelName = "iAF1260";
//		String modelName = "iMM904";
//		String modelName = "iCHOv1";
		
		try {
			JSONReader reader2 = new JSONReader(JSON_FOLDER + modelName + ".json", modelName, true);
			Container contOriginal = new Container(reader2);
			
			JSONReader reader = new JSONReader(WRITE_FOLDER + modelName + ".json", modelName, true);
			Container contJSON = new Container(reader);
			
			Set<String> jsonReac = contJSON.getReactions().keySet();
			Set<String> originalReac = contOriginal.getReactions().keySet();
			handleSets(jsonReac, originalReac, "reactions");
			
			Set<String> jsonMeta = contJSON.getMetabolites().keySet();
			Set<String> originalMeta = contOriginal.getMetabolites().keySet();			
			handleSets(jsonMeta, originalMeta, "metabolites");
			
			String jsonBiomass = contJSON.getBiomassId();
			String originalBiomass = contOriginal.getBiomassId();
			
			Assert.assertTrue("Different biomassId: " +jsonBiomass + " vs " +originalBiomass, jsonBiomass.equals(originalBiomass));
			
			for (String meta : originalMeta) {
				Set<String> reactions = contJSON.getMetabolites().get(meta).getReactionsId();
				for (String reac : reactions) {
					
					double jsonStoich = contJSON.getMetaboliteStoichiometry(reac, meta);
					double originalStoich = contOriginal.getMetaboliteStoichiometry(reac, meta);
					
					Assert.assertTrue("Different stoichiometry for reaction: " +reac+ " and metabolite: "+meta+ " : " +jsonStoich + " vs " +originalStoich, jsonStoich == originalStoich);
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
