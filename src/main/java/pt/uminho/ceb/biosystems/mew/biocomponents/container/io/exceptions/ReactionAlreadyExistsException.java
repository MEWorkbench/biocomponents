package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions;

public class ReactionAlreadyExistsException extends EntityAlreadyExistsException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public ReactionAlreadyExistsException(String entityId) {
		super(entityId, ExceptionProperties.REACTION_TYPE);
	}

}
