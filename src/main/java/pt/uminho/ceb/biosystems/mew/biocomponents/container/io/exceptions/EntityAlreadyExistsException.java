package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions;

public class EntityAlreadyExistsException extends EntityException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EntityAlreadyExistsException(String entityId, String entityType) {
		super(entityId, entityType);
	}
	
	@Override
	public String getMessage() {
		return "The " + entityType + " with id " + entityId + " already exists!";
	}
	
}
