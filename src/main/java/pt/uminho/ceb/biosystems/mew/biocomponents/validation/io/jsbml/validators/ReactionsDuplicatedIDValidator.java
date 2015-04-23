package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators;

public class ReactionsDuplicatedIDValidator extends AbstractDuplicatedValidator{
	
	@Override
	public String getAtributeId() {
		return "id";
	}

	@Override
	public String getElementName() {
		return "reaction";
	}

}
