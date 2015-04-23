package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SpeciesReferenceUnknownIDValidator extends AbstractUnknownValidator{

	public SpeciesReferenceUnknownIDValidator(Document document) {
		super(document);
	}

	@Override
	public String getAtributeId() {
		return "species";
	}

	@Override
	public String getElementName() {
		return "speciesReference";
	}

	@Override
	public String getDependentAttribute() {
		return "species";
	}

	@Override
	public String getDependentAttributeId() {
		return "id";
	}
	
	@Override
	public String getDependentNode() {
		return "listOfSpecies";
	}
	
	@Override
	public boolean canBeSolved(Element elem) {
		return false;
	}
}
