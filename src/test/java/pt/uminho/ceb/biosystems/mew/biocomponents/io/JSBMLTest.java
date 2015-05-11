package pt.uminho.ceb.biosystems.mew.biocomponents.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.xml.sax.SAXException;

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

}
