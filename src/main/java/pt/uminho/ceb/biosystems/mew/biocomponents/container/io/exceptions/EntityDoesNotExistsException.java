package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions;

public class EntityDoesNotExistsException extends EntityException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public EntityDoesNotExistsException(String entityId, String entityType) {
		super(entityId, entityType);
	}
	
	
	@Override
	public String getMessage() {
		return "The " + getEntityType() + " with id " + getEntityId() + " does not exists!";
	}
}
