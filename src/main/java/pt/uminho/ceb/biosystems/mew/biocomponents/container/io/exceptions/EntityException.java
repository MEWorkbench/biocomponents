package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions;

public class EntityException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	protected String entityId;
	protected String entityType;
	
	public EntityException(String entityId, String entityType) {
		super();
		this.entityId = entityId;
		this.entityType = entityType;
	}
	
	public String getEntityId() {
		return entityId;
	}
	
	public String getEntityType() {
		return entityType;
	}
}
