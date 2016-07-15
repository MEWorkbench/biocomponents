package pt.uminho.ceb.biosystems.mew.biocomponents.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.xml.sax.SAXException;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.ErrorsException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.writers.JSBMLWriter;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidationException;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.JSBMLValidatorException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public class JSBMLTest {

	private String getFile(String fileName){
		URL nyData = getClass().getClassLoader().getResource(fileName);
		return nyData.getFile();
	}
	
	@Test
	public void newValidator() throws ParserConfigurationException, SAXException, IOException, TransformerException, JSBMLValidatorException {
		File fFolder = new File(getFile("model_with_problems"));
		//File fFolder = new File(getFile("one_solved");
		
		File[] allFiles = fFolder.listFiles();
		
		for(File f : allFiles){
			
			String name = f.getName();
			
			if(name.endsWith(".xml")){
				System.out.println("Reading: " + name);
				name = name.replace(".xml", "");
				
				JSBMLValidator validator = new JSBMLValidator(f);
				try {
					validator.validate();
					System.out.println("SBML is valid!");
				} catch (JSBMLValidationException e) {
					System.out.println("SBML not valid!");
					System.out.println(CollectionUtils.join(e.getProblems(), "\n"));
					
					if(e.isSBMLResolvable())
					{
						System.out.println("---------------------- // ------------------------");
						Set<String> out = validator.validate(getFile("solved_problem_models/"+name+".xml"));
						//Set<String> out = validator.validate(getFile("one_solved/"+name+".xml");
	////					
	//					System.out.println("Problems: " + out);
						System.out.println(CollectionUtils.join(out, "\n"));
						System.out.println("---------------------- // ------------------------");
						System.out.println("SBML is solved!");
					}
					else
						System.out.println("SBML cannot be solved!");
				}
			}
			System.out.println();
		}
	}
	
//	@Test
	public void correctIdentation() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException{
		JSBMLValidator.correctIdentation(new FileInputStream(getFile("model_with_problems/iJP815.xml")), new FileOutputStream(getFile("solved_problem_models/iJP815.xml")));
	}
	
//	public static void main(String[] args) throws IOException, XMLStreamException, ErrorsException, ParserConfigurationException, SAXException, JSBMLValidationException {
//		JSBMLReader reader = new JSBMLReader("/home/pmaia/ownCloud/documents/INVISTA/INVISTA_20160118_landscape_tests/model/iJO1366_AHPT.xml", "ecoli",false);
//		Container container = new Container(reader);
//	}
	
	public static void main(String[] args) throws Exception {
		
		String baseDir = "/home/pmaia/ownCloud/documents/INVISTA/INVISTA_20160118_landscape_tests/";
		
		String conf = baseDir + "configurations/conf_20160118_landscape_test1.conf";
		
		JSBMLReader reader = new JSBMLReader("/home/pmaia/ownCloud/documents/INVISTA/INVISTA_20160118_landscape_tests/model/iJO1366_GLUDy_AHPT_gapA.xml", "ecoli",false);
		Container container = new Container(reader);
		container.setBiomassId("R_Ec_biomass_iJO1366_core_53p95M");
		
//		OptimizationConfiguration config = new OptimizationConfiguration(conf);
//		config.setCurrentState(0);
//		Container container = config.getContainer();
//		container.setBiomassId(config.getModelBiomass());
		
//		ContainerEnv env = new ContainerEnv(container);
		int numFake = 3000;
		
		MetaboliteCI metFake1 = new MetaboliteCI("M_metFake1", "metaboliteFake1");
		MetaboliteCI metFake2 = new MetaboliteCI("M_metFake2", "metaboliteFake2");
		
		container.getMetabolites().put("M_metFake1", metFake1);
		container.getMetabolites().put("M_metFake2", metFake2);
		
		for (int i = 1; i <= numFake; i++) {
			if(i % 100 == 0) System.out.print(i+",");
			Map<String, StoichiometryValueCI> reacMap = new HashMap<>();
			reacMap.put("M_metFake1", new StoichiometryValueCI("M_metFake1", 1.0, "c"));
			Map<String, StoichiometryValueCI> prodMap = new HashMap<>();
			prodMap.put("M_metFake2", new StoichiometryValueCI("M_metFake2", 1.0, "c"));
			
			ReactionCI reac = new ReactionCI("R_reacFake"+i, "reaction fake"+i, false, reacMap, prodMap);
			container.getReactions().put("R_reacFake"+i, reac);
		}
		container.verifyDepBetweenClass(false);
		
		JSBMLWriter writer = new JSBMLWriter("/home/pmaia/ownCloud/documents/INVISTA/INVISTA_20160118_landscape_tests/model/iJO1366_GLUDy_AHPT_gapA_tuned.xml", container);
		writer.writeToFile();
		
		System.out.println("...Done");
	}

}
