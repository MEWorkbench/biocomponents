package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions;

public class ReactionDoesNotExistsException extends EntityDoesNotExistsException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public ReactionDoesNotExistsException(String entityId) {
		super(entityId, ExceptionProperties.REACTION_TYPE);
	}
	
}
