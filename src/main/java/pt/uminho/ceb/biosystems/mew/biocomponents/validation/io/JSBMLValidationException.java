package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators.ElementValidator;

public class JSBMLValidationException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private  Set<String> problems;
	private Map<Class<ElementValidator>, Set<String>> problemsByValidator;
	private JSBMLValidator sbmlvalidator;
	private boolean isSBMLResolvable = true;
	
	
	public JSBMLValidationException(JSBMLValidator sbmlvalidator) {
		super("The SBML file is not valid");
		this.sbmlvalidator = sbmlvalidator;
		this.problems = new LinkedHashSet<String>();
		this.problemsByValidator = new HashMap<Class<ElementValidator>, Set<String>>();
	}
	
	public JSBMLValidator getSbmlvalidator() {
		return sbmlvalidator;
	}
	
	public Set<String> getProblems() {
		return problems;
	}
	
	public void addProblem(String problem, Class<ElementValidator> klass){
		problems.add(problem);
		
		Set<String> problemsByClass = problemsByValidator.get(klass);
		if(problemsByClass == null){
			problemsByClass = new HashSet<String>();
			problemsByValidator.put(klass, problemsByClass);
		}
		problemsByClass.add(problem);
	}
	
	public Map<Class<ElementValidator>, Set<String>> getProblemsByClass(){
		return problemsByValidator;
	}
	
	public void setIsSBMLResolvable(boolean isSBMLResolvable) {
		this.isSBMLResolvable = isSBMLResolvable;
	}
	
	public boolean isSBMLResolvable() {
		return isSBMLResolvable;
	}
	
	public void printStackTrace(PrintStream s){
		for(String p : problems)
			s.append(p +"\n");
	}

	public void printStackTrace(PrintWriter s){
		for(String p : problems)
			s.append(p + "\n");
	}
}
