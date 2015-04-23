package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions;

public class MetaboliteDoesNotPresentInReaction extends EntityDoesNotContainEntityException {


	private static final long serialVersionUID = 1L;
	
	public MetaboliteDoesNotPresentInReaction(String metaboliteId, String reactionId) {
		super(metaboliteId, ExceptionProperties.METABOLITE_TYPE, reactionId, ExceptionProperties.REACTION_TYPE);	
	}

}
