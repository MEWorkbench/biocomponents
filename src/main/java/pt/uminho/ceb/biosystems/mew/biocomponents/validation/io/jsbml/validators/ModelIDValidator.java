package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ModelIDValidator extends AbstractIDValidator {

	@Override
	public String getAtributeId() {
		return "id";
	}

	@Override
	public String getElementName() {
		return "model";
	}
	
	@Override
	public boolean isValid(Element elem) {
		return !unsuportCaracter(elem) && !startWithNumber(elem);
	}
	
	@Override
	public String solveProblem(Document document, Element elem) throws JSBMLValidatorException {
		String message = null;
		
		try {
			message = super.solveProblem(document, elem);
		} catch (Exception e) {
			message = "Model does not have atribute id";
		}
		
		return message;
	}

	@Override
	public boolean canBeSolved(Element elem) {
		return false;
	}

}
