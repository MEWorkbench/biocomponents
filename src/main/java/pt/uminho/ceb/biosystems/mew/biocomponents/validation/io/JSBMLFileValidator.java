package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public class JSBMLFileValidator {
	
	static private Map<String, String> invalidCarac= null;
	
	static private Map<String, String> getInvalidCaracters(){
		if(invalidCarac==null){
			invalidCarac = new HashMap<String, String>();
			invalidCarac.put("-", "_DASH_");
			invalidCarac.put(" ", "_");
			invalidCarac.put("(", "_LPAREN_");
			invalidCarac.put(")", "_RPAREN_");
			invalidCarac.put(",", "_COMA_");
			invalidCarac.put("+", "_PLUS_");
			invalidCarac.put("[", "_LBRACKET_");
			invalidCarac.put("]", "_RBRACKET_");
			invalidCarac.put("'", "");
		}
		return invalidCarac;
	}
	
	static public Document readStream(File f) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException{
		return readStream(new FileInputStream(f));
	}
	
	static public Document readStream(InputStream is) throws ParserConfigurationException, SAXException, IOException{
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(is);
		return doc;
	}
	
	
	static public NodeList getSpecies(Document doc){
		return doc.getElementsByTagName("species");
	}
	
	static public NodeList getSpeciesReference(Document doc){
		return doc.getElementsByTagName("speciesReference");
	}
	
	static Set<Element> getProblemNodes(NodeList nl, String attr){
		
		Set<Element> invalidElements = new HashSet<Element>();
		int imax = nl.getLength();
		for(int i =0; i < imax; i++){
			
			Node nNode = nl.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				if(!validElement((Element) nNode, attr)){
					invalidElements.add((Element) nNode);
					
//					System.out.println(((Element) nNode).getAttribute("id"));
				}
			}
		}
		return invalidElements;
	}
	
	
	static private boolean validElement(Element e, String attr){
		String toTest = e.getAttribute(attr);
		return !toTest.matches(".*[\\"+CollectionUtils.join(getInvalidCaracters().keySet(), "\\")+"].*");
	}
	
	static Map<String, String> solveElements(Collection<Element> elems, String attr){
		return solveElements(elems, attr, null);
	}
	
	static Map<String, String> solveElements(Collection<Element> elems, String attr, Map<String, String> oldNew){
		
		Map<String, String> idsSolved = (oldNew==null)?new TreeMap<String, String>():oldNew;
		for(Element e : elems){
			solveElement(e, attr, idsSolved);
		}
		return idsSolved;
	}
	
	static void solveElement(Element e, String attr, Map<String, String> map){
		String id = e.getAttribute(attr);
		String old = id;
		if(map.get(old) == null){
			for(String erro : getInvalidCaracters().keySet()){
				id = id.replaceAll("\\"+erro, getInvalidCaracters().get(erro));
				System.out.println(id + "\t" + id);
			}
			map.put(old, id);
		}else
			id = map.get(old);
		e.setAttribute(attr, id);
		
	}
	
	static void saveDocumentInFile(Document doc, File file) throws TransformerException, FileNotFoundException{
		saveDocumentInFile(doc, new FileOutputStream(file));
	}
	
	static void saveDocumentInFile(Document doc, OutputStream os) throws TransformerException{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(os);
 
		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);
 
		transformer.transform(source, result);
	}
	
	static void solveIdSpecies(Document doc){
		NodeList nl = getSpecies(doc);
		Set<Element> erros = getProblemNodes(nl, "id");
		solveElements(erros, "id");
	}
	
	static void solveSpeciesReference(Document doc){
		NodeList nl = getSpeciesReference(doc);
		
		System.out.println(nl.getLength());
		Set<Element> erros = getProblemNodes(nl, "species");
		System.out.println(erros.size());
		solveElements(erros, "species");
	}
	
	static void solveSpecies(Document doc){
		solveIdSpecies(doc);
		solveSpeciesReference(doc);
	}
	
	static public void solveSBMLProblems(String inFile, String outFile) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException{
		Document doc = readStream(new File(inFile));
		solveSpecies(doc);
		saveDocumentInFile(doc, new File(outFile));
	}
	
	static public void main(String... str) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException{
		
		Document doc = readStream(new File("../paper_models/model_files/model_with_problems/iAK692.xml"));
		solveSpecies(doc);
		
		
		saveDocumentInFile(doc, System.out);
	}

}
