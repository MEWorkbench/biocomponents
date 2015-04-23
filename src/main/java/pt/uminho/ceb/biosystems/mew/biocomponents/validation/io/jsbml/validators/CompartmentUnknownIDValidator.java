package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators;

import org.w3c.dom.Document;


public class CompartmentUnknownIDValidator extends AbstractUnknownValidator{

	public CompartmentUnknownIDValidator(Document document) {
		super(document);
	}

	@Override
	public String getAtributeId() {
		return "compartment";
	}

	@Override
	public String getElementName() {
		return "species";
	}
	
	@Override
	public String getDependentAttribute() {
		return "compartment";
	}

	@Override
	public String getDependentNode() {
		return "listOfCompartments";
	}

	@Override
	public String getDependentAttributeId() {
		return "id";
	}
	
}
