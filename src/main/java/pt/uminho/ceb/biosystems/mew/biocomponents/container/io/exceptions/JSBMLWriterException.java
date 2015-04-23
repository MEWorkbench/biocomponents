package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions;

import java.util.Map;

public class JSBMLWriterException extends RuntimeException{
	
	Map<String, Exception> exceptionMap;
	String message;

	public JSBMLWriterException() {
		
	}
	
	public JSBMLWriterException(Exception e) {
	}
	
	public JSBMLWriterException(Map<String, Exception> exceptionMap) {
		this.exceptionMap = exceptionMap;
		message = "Unable to export model to SBML.\n";
		for (String reaction : exceptionMap.keySet()) {
			message += "There is a problem with element '"+reaction +"': " +exceptionMap.get(reaction).getMessage() +"\n";
		}
	}
	
	public Map<String, Exception> getExceptionMap() {
		return exceptionMap;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
	
}
