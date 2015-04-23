package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators;

public class StoichiometryValidator extends AbstractNumberValidator{

	@Override
	public String getAtributeId() {
		return "stoichiometry";
	}

	@Override
	public String getElementName() {
		return "speciesReference";
	}

}
