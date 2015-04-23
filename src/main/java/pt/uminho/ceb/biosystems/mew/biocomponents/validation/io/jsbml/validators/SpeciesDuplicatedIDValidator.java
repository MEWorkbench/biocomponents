package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators;

import org.w3c.dom.Element;

public class SpeciesDuplicatedIDValidator extends AbstractDuplicatedValidator{

	@Override
	public String getAtributeId() {
		return "id";
	}

	@Override
	public String getElementName() {
		return "species";
	}
	
	@Override
	public boolean canBeSolved(Element elem) {
		return false;
	}

}
