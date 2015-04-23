package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions;

public class MetaboliteAlreadyExistsException extends EntityAlreadyExistsException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public MetaboliteAlreadyExistsException(String entityId) {
		super(entityId, ExceptionProperties.METABOLITE_TYPE);
	}

}
