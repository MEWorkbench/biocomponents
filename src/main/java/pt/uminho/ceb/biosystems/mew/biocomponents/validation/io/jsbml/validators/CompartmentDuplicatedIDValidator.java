package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators;

import org.w3c.dom.Element;

public class CompartmentDuplicatedIDValidator extends AbstractDuplicatedValidator{

	@Override
	public String getAtributeId() {
		return "id";
	}

	@Override
	public String getElementName() {
		return "compartment";
	}
	
	@Override
	public boolean canBeSolved(Element elem) {
		return false;
	}

}
