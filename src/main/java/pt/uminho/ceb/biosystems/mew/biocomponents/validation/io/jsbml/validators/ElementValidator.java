package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface ElementValidator {
	
	boolean isValid(Element elem);
	String reason(Element elem);
	boolean canBeSolved(Element elem);
	String solveProblem(Document doc, Element elem) throws JSBMLValidatorException;
	//String solveProblem(Element elem) throws JSBMLValidatorException;
	String getElementName();
}
