package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions;

public class MetaboliteDoesNotExistsException extends EntityDoesNotExistsException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public MetaboliteDoesNotExistsException(String entityId) {
		super(entityId, ExceptionProperties.METABOLITE_TYPE);
	}
	
}
