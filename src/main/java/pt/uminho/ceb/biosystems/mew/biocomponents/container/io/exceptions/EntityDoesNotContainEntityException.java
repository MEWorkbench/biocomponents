package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions;

public class EntityDoesNotContainEntityException extends EntityException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String containerEntityId;
	private String containerEntityType;
	
	public EntityDoesNotContainEntityException(String entityId, String entityType, String containerEntityId, String containerEntityType) {
		super(entityId, entityType);
		this.containerEntityId = containerEntityId;
		this.containerEntityType = containerEntityType;
	}
	
	
	@Override
	public String getMessage() {
		return "The " + getEntityType() + " with id " + getEntityId() + " does not exists in "+ getContainerEntityType()+" "+getContainerEntityId()+" !";
	}
	
	public String getContainerEntityId() {
		return containerEntityId;
	}
	
	public String getContainerEntityType() {
		return containerEntityType;
	}
}
