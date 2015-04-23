package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions;

public class GeneDoesNotExistsException extends EntityDoesNotExistsException{

	public GeneDoesNotExistsException(String entityId) {
		super(entityId, ExceptionProperties.GENE);
	}

}
