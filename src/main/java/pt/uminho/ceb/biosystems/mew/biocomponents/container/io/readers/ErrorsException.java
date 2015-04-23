package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.sbml.jsbml.SBMLDocument;


public class ErrorsException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String messageError = "";
	
	protected SBMLDocument document;
	
	public ErrorsException(SBMLDocument document){
		
		messageError = "Invalid SBML, reasons:\n\n";
		this.document = document;
		
		
	
//		this.setStackTrace();
	}
	
	@Override
	public String getMessage(){
		return messageError;
	}
	
	
	public void printStackTrace(PrintStream s){
		
		for(int i =0; i < document.getNumErrors(); i++){
			if(document.getError(i).getSeverity().equals("Error"))
				s.append( document.getError(i).getMessage()+ " in line " +document.getError(i).getLine()+"\n");
		}
		
	}
	
	public void printStackTrace(PrintWriter s){
		for(int i =0; i < document.getNumErrors(); i++){
			if(document.getError(i).getSeverity().equals("Error"))
				s.append( document.getError(i).getMessage()+ " in line " +document.getError(i).getLine()+"\n");
		}
	}
	
}
