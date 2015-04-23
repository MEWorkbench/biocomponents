package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators;

public class CompartmentIDValidator extends AbstractIDValidator {

	@Override
	public String getAtributeId() {
		return "id";
	}

	@Override
	public String getElementName() {
		return "compartment";
	}

}
