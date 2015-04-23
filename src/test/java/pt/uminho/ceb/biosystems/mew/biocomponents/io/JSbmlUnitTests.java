package pt.uminho.ceb.biosystems.mew.biocomponents.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Test;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.ErrorsException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidationException;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.JSBMLValidatorException;

public class JSbmlUnitTests {
	
	private String getFile(String fileName){
		URL nyData = getClass().getClassLoader().getResource(fileName);
		return nyData.getFile();
	}
	
	@Test
	public void validateModelDuplicatedIDInvalid() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException
	{
		System.out.println("\n-----------------------" + new Object(){}.getClass().getEnclosingMethod().getName() + "-----------------------");
		boolean solvable = true;
		//JSBMLValidator validator = new JSBMLValidator(new File(getFile("test_unit_models/Ec_iAF1260_DuplicatedID_NO_SOLUTION.xml"));
		JSBMLValidator validator = new JSBMLValidator(new File(getFile("test_unit_models/Ec_iAF1260_BasicUnknown_NOT_SOLVABLE.xml")));
		validator.enableAllValidators(true);
		//validator.enableDuplicatedIDValidators(true);
		
		try {
			validator.validate();
		} catch (JSBMLValidationException e) {
			System.out.println(CollectionUtils.join(e.getProblems(), "\n"));
			solvable = e.isSBMLResolvable();
		}
		
		//Assert.assertEquals("With these duplicated IDs the SBML must be unsolvable!", false, solvable);
	}
	
	@Test
	public void validateModelDuplicatedIDValid() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, JSBMLValidatorException, TransformerException
	{
		System.out.println("\n-----------------------" + new Object(){}.getClass().getEnclosingMethod().getName() + "-----------------------");
		boolean solvable = false;
		JSBMLValidator validator = new JSBMLValidator(new File(getFile("test_unit_models/Ec_iAF1260_DuplicatedID_WITH_SOLUTION.xml")));
		
		validator.enableAllValidators(false);
		validator.enableDuplicatedIDValidators(true);
		
		try {
			validator.validate();
		} catch (JSBMLValidationException e) {
			System.out.println(CollectionUtils.join(e.getProblems(), "\n"));
			if(e.isSBMLResolvable())
			{
				solvable = true;
				System.out.println("---------------------- // ------------------------");
				System.out.println("\t\t Correcting SBML");
				Set<String> out = validator.validate(getFile("test_unit_models_solved/Ec_iAF1260_DuplicatedID_WITH_SOLUTION.xml"));
				System.out.println(CollectionUtils.join(out, "\n"));
				System.out.println("---------------------- // ------------------------");
				System.out.println("SBML is solved!");
				
//				Document doc = JSBMLValidator.readStream(new FileInputStream(new File(getFile("test_unit_models_solved/Ec_iAF1260_StrangeChar_SOLVABLE.xml")));
//				Assert.assertEquals("Expected Reaction ID R_1_HASH_2DGR120tipp", true, attributeValueExist(doc, "R_1_HASH_2DGR120tipp", "reaction", "id"));
//				
//				Assert.assertEquals("Expected Species ID M_POUND__12dgr140_c", true, attributeValueExist(doc, "M_POUND__12dgr140_c", "species", "id"));
//				
//				Assert.assertEquals("Expected SpeciesReference Species M_EQUALS__12ppd_R_e", true, attributeValueExist(doc, "M_EQUALS__12ppd_R_e", "speciesReference", "species"));
//				
//				Assert.assertEquals("Expected Compartment ID E_CIRCUMFLEX_xtra_organism", true, attributeValueExist(doc, "E_CIRCUMFLEX_xtra_organism", "compartment", "id"));
//				
//				Assert.assertEquals("Expected Species Compartment P_ACUTE_eriplasm", true, attributeValueExist(doc, "P_ACUTE_eriplasm", "species", "compartment"));
			}
			else
				System.out.println("SBML cannot be solved!");
		}
		Assert.assertEquals("With these duplicated IDs it must be possible to correct the SBML!", true, solvable);
		
		try {
			JSBMLReader reader = new JSBMLReader(getFile("test_unit_models_solved/Ec_iAF1260_DuplicatedID_WITH_SOLUTION.xml"), "organism", true);
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ErrorsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSBMLValidationException e) {
			e.printStackTrace();
		} 
	}
	
	@Test
	public void validateModelStrangeCharIDInvalid() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException
	{
		System.out.println("\n-----------------------" + new Object(){}.getClass().getEnclosingMethod().getName() + "-----------------------");
		JSBMLValidator validator = new JSBMLValidator(new File(getFile("test_unit_models/Ec_iAF1260_StrangeChar_NOT_SOLVABLE.xml")));
		boolean solvable = true;
		
		validator.enableAllValidators(false);
		validator.enableModelIDValidator(true);
		validator.enableIDValidators(true);
		
		try {
			validator.validate();
		} catch (JSBMLValidationException e) {
			System.out.println(CollectionUtils.join(e.getProblems(), "\n"));
			solvable = e.isSBMLResolvable();
		}
		
		Assert.assertEquals("With these strange IDs the SBML must be unsolvable!", false, solvable);
	}
	
	@Test
	public void validateModelStrangeCharIDCorrect() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, JSBMLValidatorException, TransformerException
	{
		System.out.println("\n-----------------------" + new Object(){}.getClass().getEnclosingMethod().getName() + "-----------------------");
		JSBMLValidator validator = new JSBMLValidator(new File(getFile("test_unit_models/Ec_iAF1260_StrangeChar_SOLVABLE.xml")));
		boolean solvable = false;
		
		validator.enableAllValidators(false);
		validator.enableIDValidators(true);
		
		try {
			validator.validate();
		} catch (JSBMLValidationException e) {
			System.out.println(CollectionUtils.join(e.getProblems(), "\n"));
			
			if(e.isSBMLResolvable())
			{
				solvable = true;
				System.out.println("---------------------- // ------------------------");
				System.out.println("\t\t Correcting SBML");
				Set<String> out = validator.validate(getFile("test_unit_models_solved/Ec_iAF1260_StrangeChar_SOLVABLE.xml"));
				System.out.println(CollectionUtils.join(out, "\n"));
				System.out.println("---------------------- // ------------------------");
				System.out.println("SBML is solved!");
				
				Document doc = JSBMLValidator.readStream(new FileInputStream(new File(getFile("test_unit_models_solved/Ec_iAF1260_StrangeChar_SOLVABLE.xml"))));
				Assert.assertEquals("Expected Reaction ID R_1_HASH_2DGR120tipp", true, attributeValueExist(doc, "R_1_HASH_2DGR120tipp", "reaction", "id"));
				
				Assert.assertEquals("Expected Species ID M_POUND__12dgr140_c", true, attributeValueExist(doc, "M_POUND__12dgr140_c", "species", "id"));
				
				Assert.assertEquals("Expected SpeciesReference Species M_EQUALS__12ppd_R_e", true, attributeValueExist(doc, "M_EQUALS__12ppd_R_e", "speciesReference", "species"));
				
				Assert.assertEquals("Expected Compartment ID E_CIRCUMFLEX_xtra_organism", true, attributeValueExist(doc, "E_CIRCUMFLEX_xtra_organism", "compartment", "id"));
				
				Assert.assertEquals("Expected Species Compartment P_ACUTE_eriplasm", true, attributeValueExist(doc, "P_ACUTE_eriplasm", "species", "compartment"));
			}
			else
				System.out.println("SBML cannot be solved!");
		}
		
		Assert.assertEquals("With these strange chars it must be possible to correct the SBML!", true, solvable);
	}
	
	@Test
	public void validateModelUnknownIDValid() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, JSBMLValidatorException, TransformerException
	{
		System.out.println("\n-----------------------" + new Object(){}.getClass().getEnclosingMethod().getName() + "-----------------------");
		JSBMLValidator validator = new JSBMLValidator(new File(getFile("test_unit_models/Ec_iAF1260_BasicUnknown_SOLVABLE.xml")));
		boolean solvable = false;
		
		validator.enableAllValidators(false);
		validator.enableUnknownIDValidators(true);
		
		try {
			validator.validate();
		} catch (JSBMLValidationException e) {
			System.out.println(CollectionUtils.join(e.getProblems(), "\n"));
			if(e.isSBMLResolvable()){
				solvable = true;
				
				System.out.println("---------------------- // ------------------------");
				System.out.println("\t\t Correcting SBML");
				Set<String> out = validator.validate(getFile("test_unit_models_solved/Ec_iAF1260_BasicUnknown_SOLVABLE.xml"));
				System.out.println(CollectionUtils.join(out, "\n"));
				System.out.println("---------------------- // ------------------------");
				System.out.println("SBML is solved!");
				
				Document doc = JSBMLValidator.readStream(new FileInputStream(new File(getFile("test_unit_models_solved/Ec_iAF1260_BasicUnknown_SOLVABLE.xml"))));
				
				Assert.assertEquals("Expected new Compartment ID Extra_organismX", true, attributeValueExist(doc, "Extra_organismX", "compartment", "id"));
			}
		}
		
		Assert.assertEquals("With these unknown IDs the SBML must be solvable!", true, solvable);
	}
	
	@Test
	public void validateModelUnknownIDInvalid() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, JSBMLValidatorException, TransformerException
	{
		System.out.println("\n-----------------------" + new Object(){}.getClass().getEnclosingMethod().getName() + "-----------------------");
		JSBMLValidator validator = new JSBMLValidator(new File(getFile("test_unit_models/Ec_iAF1260_BasicUnknown_NOT_SOLVABLE.xml")));
		boolean solvable = true;
		
		validator.enableAllValidators(false);
		validator.enableUnknownIDValidators(true);
		
		try {
			validator.validate();
		} catch (JSBMLValidationException e) {
			System.out.println(CollectionUtils.join(e.getProblems(), "\n"));
			solvable = e.isSBMLResolvable();
		}
		
		Assert.assertEquals("With these unknown IDs the SBML must be unsolvable!", false, solvable);
	}
	
	@Test
	public void validateModelStoichiometryInvalid() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException
	{
		System.out.println("\n-----------------------" + new Object(){}.getClass().getEnclosingMethod().getName() + "-----------------------");
		boolean solvable = true;
		JSBMLValidator validator = new JSBMLValidator(new File(getFile("test_unit_models/Ec_iAF1260_Stoichiometry_NOT_SOLVABLE.xml")));
		
		validator.enableAllValidators(false);
		validator.enableStoichiometryValidator(true);
		
		try {
			validator.validate();
		} catch (JSBMLValidationException e) {
			System.out.println(CollectionUtils.join(e.getProblems(), "\n"));
			solvable = e.isSBMLResolvable();
		}
		
		Assert.assertEquals("With these stoichiometric values the SBML must be unsolvable!", false, solvable);
	}
	
	private boolean attributeValueExist(Document doc, String attributeValueExpected, String nodeName, String attributeID)
	{
		
		NodeList nlElement = doc.getElementsByTagName(nodeName);
		for (int i = 0; i < nlElement.getLength(); i++) {
			if (nlElement.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element elem = (Element) nlElement.item(i);
				if(attributeValueExpected.equals(elem.getAttribute(attributeID)))
					return true;
			}
		}
		return false;
	}

	
	@Test
	public void validateSBMLModel(){
		try {
			JSBMLReader reader = new JSBMLReader(getFile("model_with_problems/iHZ565.xml"), "NoName");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ErrorsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSBMLValidationException e) {
			System.out.println(e.getMessage());
			for (String problem : e.getProblems()) {
				System.out.println(problem);
			}
			
			System.out.println(e.isSBMLResolvable());
		}
		
		
//		JSBMLValidator validator;
//		try {
//			validator = new JSBMLValidator(new File(getFile("model_with_problems/GSMN-TB.xml"));
//			validator.enableAllValidators(true);
//			try {
//				validator.validate();
//			} catch (JSBMLValidationException e) {
//				System.out.println(CollectionUtils.join(e.getProblems(), "\n"));
//			}
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (ParserConfigurationException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (SAXException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
	}
	
	@Test//(timeout=3000)
	public void validateSBMLModelRecon2(){
	
		
		long timeinitMY = System.currentTimeMillis();
		JSBMLValidator validator;
		try {
			validator = new JSBMLValidator(new File(getFile("test_unit_models/recon2.v02.xml")));
			validator.enableAllValidators(true);
			try {
				validator.validate();
			} catch (JSBMLValidationException e) {
				System.out.println(CollectionUtils.join(e.getProblems(), "\n"));
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		long finaltimerMY = System.currentTimeMillis();
		System.out.println(finaltimerMY-timeinitMY);
	}
	
	@Test(timeout=3000)
	public void validateSBMLModelReconModel2(){
	
		
		long timeinitMY = System.currentTimeMillis();
		JSBMLValidator validator;
		try {
			validator = new JSBMLValidator(new File(getFile("test_unit_models/recon2model.v02.xml")));
			validator.enableAllValidators(true);
			try {
				validator.validate();
			} catch (JSBMLValidationException e) {
				System.out.println(CollectionUtils.join(e.getProblems(), "\n"));
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		long finaltimerMY = System.currentTimeMillis();
		System.out.println(finaltimerMY-timeinitMY);
	}
	
	@Test
	public void testPerformence() {
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new FileInputStream(new File(getFile("test_unit_models/recon2model.v02.xml"))));
			
			long timeinitMY = System.currentTimeMillis();
			
			NodeList nlElement = doc.getElementsByTagName("species");
			System.out.println("AQUI!");
			System.out.println(nlElement.getClass());
			
			int imax = nlElement.getLength();
			for (int i = 0; i < imax; i++) {

			}
			
			long finaltimerMY = System.currentTimeMillis();
			System.out.println(finaltimerMY-timeinitMY);
			
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	
	
	@Test
	public void validateSBMLModel2() throws JSBMLValidatorException, TransformerException, ParserConfigurationException, SAXException, IOException{
		try {
			JSBMLReader reader = new JSBMLReader("/home/hgiesteira/Downloads/iRP911.xml", "NoName");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ErrorsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSBMLValidationException e) {
			System.out.println(e.getMessage());
			for (String problem : e.getProblems()) {
				System.out.println(problem);
			}
			
			System.out.println(e.isSBMLResolvable());
			
			if(e.isSBMLResolvable())
			{
				//solvable = true;
				System.out.println("---------------------- // ------------------------");
				System.out.println("\t\t Correcting SBML");
				Set<String> out = e.getSbmlvalidator().validate("/home/hgiesteira/Downloads/iRP911_new.xml");
				System.out.println(CollectionUtils.join(out, "\n"));
				System.out.println("---------------------- // ------------------------");
				System.out.println("SBML is solved!");
				
				Document doc = JSBMLValidator.readStream(new FileInputStream(new File("/home/hgiesteira/Downloads/iRP911_new.xml")));
			}
			else
				System.out.println("SBML cannot be solved!");
			
		}
		
		
//		JSBMLValidator validator;
//		try {
//			validator = new JSBMLValidator(new File(getFile("model_with_problems/GSMN-TB.xml"));
//			validator.enableAllValidators(true);
//			try {
//				validator.validate();
//			} catch (JSBMLValidationException e) {
//				System.out.println(CollectionUtils.join(e.getProblems(), "\n"));
//			}
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (ParserConfigurationException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (SAXException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
	}
	
	@Test
	public void validateSBMLModelNew() throws JSBMLValidatorException, TransformerException, ParserConfigurationException, SAXException, IOException{
		try {
			JSBMLReader reader = new JSBMLReader("/home/hgiesteira/Downloads/short_model (copy).xml", "NoName");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ErrorsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSBMLValidationException e) {
			System.out.println(e.getMessage());
			for (String problem : e.getProblems()) {
				System.out.println(problem);
			}
			
			System.out.println(e.isSBMLResolvable());
			
			if(e.isSBMLResolvable())
			{
				//solvable = true;
				System.out.println("---------------------- // ------------------------");
				System.out.println("\t\t Correcting SBML");
//				Set<String> out = e.getSbmlvalidator().validate("/home/hgiesteira/Desktop/Models/msb200815-s8_CORRECTED.xml");
//				System.out.println(CollectionUtils.join(out, "\n"));
//				System.out.println("---------------------- // ------------------------");
//				System.out.println("SBML is solved!");
//				
//				Document doc = JSBMLValidator.readStream(new FileInputStream(new File("/home/hgiesteira/Desktop/Models/msb200815-s8_CORRECTED.xml")));
			}
			else
				System.out.println("SBML cannot be solved!");
			
		}
		
		
//		JSBMLValidator validator;
//		try {
//			validator = new JSBMLValidator(new File(getFile("model_with_problems/GSMN-TB.xml"));
//			validator.enableAllValidators(true);
//			try {
//				validator.validate();
//			} catch (JSBMLValidationException e) {
//				System.out.println(CollectionUtils.join(e.getProblems(), "\n"));
//			}
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (ParserConfigurationException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (SAXException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
	}
}
