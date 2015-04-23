package pt.uminho.ceb.biosystems.mew.biocomponents.container.components;

public class InvalidBooleanRuleException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String invalidRule; 
	
	public InvalidBooleanRuleException(String rule){
		this.invalidRule = rule;
	}

	public String getMessage(){
		return "The boolean rule: " + invalidRule + " is invalid";
	}
}
