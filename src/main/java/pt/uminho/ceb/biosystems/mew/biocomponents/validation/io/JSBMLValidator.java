package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.CompartmentDuplicatedIDValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.CompartmentIDValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.CompartmentReferenceIDValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.CompartmentUnknownIDValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.ElementValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.JSBMLValidatorException;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.ModelIDValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.ReactionsDuplicatedIDValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.ReactionsIDValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.SpeciesDuplicatedIDValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.SpeciesIDValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.SpeciesReferenceIDValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.SpeciesReferenceUnknownIDValidator;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.StoichiometryValidator;

public class JSBMLValidator {
	
	private Document doc;
	private Set<ElementValidator> validators = null;
	private boolean enableIDValidators = true;
	private boolean enableDuplicatedIDValidators = true;
	private boolean enableUnknownIDValidators = true;
	private boolean enableModelIDValidator = true;
	private boolean enableStoichiometryValidator = true;
	
	public JSBMLValidator(InputStream is) throws ParserConfigurationException, SAXException, IOException{
		doc = readStream(is);
		validators = new LinkedHashSet<ElementValidator>();
	}
	
	public JSBMLValidator(File f) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException{
		this(new FileInputStream(f));
	}
	
	public JSBMLValidator(String pathFile) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException{
		this(new File(pathFile));
	}
	
	private void setAllValidators()
	{
		if(enableUnknownIDValidators) setUnknownIDValidators();
		
		if(enableDuplicatedIDValidators) setDuplicatedIDValidators();
		
		if(enableIDValidators)	setIDValidators();
		
		if(enableModelIDValidator) validators.add(new ModelIDValidator());
		
		if(enableStoichiometryValidator) validators.add(new StoichiometryValidator());
	}
	
	private void setIDValidators()
	{
		CompartmentIDValidator civ = new CompartmentIDValidator();
		CompartmentReferenceIDValidator criv = new CompartmentReferenceIDValidator();
		SpeciesReferenceIDValidator srv = new SpeciesReferenceIDValidator();
		SpeciesIDValidator sv = new SpeciesIDValidator();
		
		srv.setChangedValues(sv.getChangedValues());
		criv.setChangedValues(civ.getChangedValues());
		
		validators.add(new ReactionsIDValidator());
		validators.add(civ);
		validators.add(srv);
		validators.add(sv);
		validators.add(criv);
	}
	
	private void setDuplicatedIDValidators()
	{
		validators.add(new CompartmentDuplicatedIDValidator());
		validators.add(new SpeciesDuplicatedIDValidator());
		validators.add(new ReactionsDuplicatedIDValidator());
	}
	
	private void setUnknownIDValidators()
	{
		validators.add(new SpeciesReferenceUnknownIDValidator(doc));
		validators.add(new CompartmentUnknownIDValidator(doc));
	}
	
	public void enableDuplicatedIDValidators(boolean enableDuplicatedIDValidators) {
		this.enableDuplicatedIDValidators = enableDuplicatedIDValidators;
	}
	
	public void enableIDValidators(boolean enableIDValidators) {
		this.enableIDValidators = enableIDValidators;
	}
	
	public void enableUnknownIDValidators(boolean enableUnknownIDValidators) {
		this.enableUnknownIDValidators = enableUnknownIDValidators;
	}
	
	public void enableModelIDValidator(boolean enableModelIDValidator) {
		this.enableModelIDValidator = enableModelIDValidator;
	}
	
	public void enableStoichiometryValidator(boolean enableStoichiometryValidator) {
		this.enableStoichiometryValidator = enableStoichiometryValidator;
	}
	
	public void enableAllValidators(boolean enableAllValidators)
	{
		this.enableDuplicatedIDValidators = enableAllValidators;
		this.enableIDValidators = enableAllValidators;
		this.enableModelIDValidator = enableAllValidators;
		this.enableStoichiometryValidator = enableAllValidators;
		this.enableUnknownIDValidators = enableAllValidators;
	}
	
	private NodeList getNodeList(String nodeList){
		return getNodeList(nodeList, doc);
	}
	
	private NodeList getNodeList(String nodeList, Document doc){
		return doc.getElementsByTagName(nodeList);
	}
	
	public void validate() throws JSBMLValidationException{
		
		setAllValidators();
		
		JSBMLValidationException problems = new JSBMLValidationException(this);
		
		boolean hasProblems = false;
		for(ElementValidator v : validators){
			
			NodeList nl = getNodeList(v.getElementName());
			boolean validatorHasProblems = hasProblems(nl, v, problems);
			hasProblems = validatorHasProblems || hasProblems;
			if(!v.canBeSolved(null) && validatorHasProblems)
				problems.setIsSBMLResolvable(false);
		}
		if(hasProblems) throw problems;
	}
	
	public Set<String> validate(String newFile) throws JSBMLValidatorException, FileNotFoundException, TransformerException{
		
		LinkedHashSet<String> set = new LinkedHashSet<String>();
		for(ElementValidator v : validators){
			NodeList nl = getNodeList(v.getElementName());
			set.addAll(validateGeneric(nl, v));
		}
		saveDocumentInFile(doc, new FileOutputStream(newFile));
		return set;
	}
	
	private Set<String> validateGeneric(NodeList nl, ElementValidator validator) throws JSBMLValidatorException{
		
		LinkedHashSet<String> set = new LinkedHashSet<String>();
		
		int imax = nl.getLength();
		for(int i =0; i < imax; i++){
			Node nNode = nl.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element elem = (Element) nNode;
				if(!validator.isValid(elem) ){
					String msg = validator.solveProblem(doc, elem);
					if(msg != null)
						set.add(msg);
				}
			}
		}
		return set;
	}
	
	private boolean hasProblems(NodeList nl, ElementValidator validator, JSBMLValidationException problems){
		
		boolean ret = false;
		
		int imax = nl.getLength();
		for(int i =0; i < imax; i++){
			Node nNode = nl.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element elem = (Element) nNode;
				if(!validator.isValid(elem)){
					ret = true;
					problems.addProblem(validator.reason(elem), (Class<ElementValidator>) validator.getClass());
				}
			}
		}
		return ret;
	}
	
	
	static public void removeWhiteSpaces(Document doc) {
		
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']",
				doc,
				XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); ++i) {
		        Node node = nodeList.item(i);
		        node.getParentNode().removeChild(node);
		    }
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	static public Document readStream(InputStream is) throws ParserConfigurationException, SAXException, IOException{
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(is);
		return doc;
	}
	
	public static void saveDocumentInFile(Document doc, OutputStream os) throws TransformerException{
		removeWhiteSpaces(doc);
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(
		   "{http://xml.apache.org/xslt}indent-amount", "3");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(os);
 
		transformer.transform(source, result);
	}
	
	public static void correctIdentation(InputStream is, OutputStream os) throws ParserConfigurationException, SAXException, IOException, TransformerException{
		Document doc = readStream(is);
		saveDocumentInFile(doc, os);
	}
}
