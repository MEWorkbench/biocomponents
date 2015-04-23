package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions;

public class CompartmentDoesNotExistsException extends EntityDoesNotExistsException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public CompartmentDoesNotExistsException(String entityId) {
		super(entityId, ExceptionProperties.COMPARTMENT_TYPE);
	}
	
}
