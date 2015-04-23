package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions;

public class StoichiometryDoesNotExistsException extends EntityDoesNotExistsException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String metaboliteId;
	
	public StoichiometryDoesNotExistsException(String reactionId, String metaboliteId) {
		super(reactionId, ExceptionProperties.STOICHIOMETRY);
		this.metaboliteId = metaboliteId;
	}
	
	public String getReactionId(){
		return getEntityId();
	}
	
	public String getMetaboliteId(){
		return this.metaboliteId;
	}
	
	@Override
	public String getMessage() {
		return "The Metabolite Id " + getMetaboliteId() + " does not exists in reaction " + getReactionId();
	}
}
