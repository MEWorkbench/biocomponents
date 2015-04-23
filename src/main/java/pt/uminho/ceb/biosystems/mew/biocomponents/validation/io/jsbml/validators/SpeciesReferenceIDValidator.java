package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators;


public class SpeciesReferenceIDValidator extends AbstractIDValidator {

	@Override
	public String getAtributeId() {
		return "species";
	}

	@Override
	public String getElementName() {
		return "speciesReference";
	}
}
