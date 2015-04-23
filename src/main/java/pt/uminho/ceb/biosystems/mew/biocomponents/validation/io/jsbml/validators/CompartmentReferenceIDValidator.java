package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators;

public class CompartmentReferenceIDValidator extends AbstractIDValidator {

	@Override
	public String getAtributeId() {
		return "compartment";
	}

	@Override
	public String getElementName() {
		return "species";
	}

}
