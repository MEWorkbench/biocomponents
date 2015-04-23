package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public abstract class AbstractValidator implements ElementValidator{

	private Map<String, String> changedValues;
	private Map<String, String> invalidCarac;

	public abstract String getAtributeId();
	public abstract String getElementName();
	
	
	public AbstractValidator() {
		changedValues = new HashMap<String, String>();
	}
	
	public Map<String, String> getInvalidCaracters(){
		if(invalidCarac==null){
			invalidCarac = new HashMap<String, String>();
			invalidCarac.put("-", "_DASH_");
			invalidCarac.put(" ", "_");
			invalidCarac.put("(", "_LPAREN_");
			invalidCarac.put(")", "_RPAREN_");
			invalidCarac.put(",", "_COMA_");
			invalidCarac.put("+", "_PLUS_");
			invalidCarac.put("[", "_LBRACKET_");
			invalidCarac.put("]", "_RBRACKET_");
			invalidCarac.put("'", "");
			invalidCarac.put(":", "");
			invalidCarac.put(".", "_");
		}
		return invalidCarac;
	}
	
	@Override
	public boolean isValid(Element elem) {
		return !isEmpty(elem) && !unsuportCaracter(elem) && !startWithNumber(elem);
	}

	public boolean startWithNumber(Element elem){
		String toTest = elem.getAttribute(getAtributeId());
//		System.out.println( toTest + "\t" + toTest.matches("\\d+.*"));
		return toTest.matches("\\d+.*");
	}
	
	private boolean unsuportCaracter(Element elem){
		String toTest = elem.getAttribute(getAtributeId());
		return toTest.matches(".*[\\"+CollectionUtils.join(getInvalidCaracters().keySet(), "\\")+"].*");
	}
	
	private boolean isEmpty(Element elem){
		String toTest = elem.getAttribute(getAtributeId());
		return toTest == null || toTest.trim().isEmpty();
	}
	
	@Override
	public String reason(Element elem) {
		
		if(isEmpty(elem))
			return getElementName() + " has the atribute "+ getAtributeId()+ " empty";
		else if(unsuportCaracter(elem))
			return "Unsuported caracters in atribute " + getAtributeId() + " of the element " + getElementName() + " [" + elem.getAttribute(getAtributeId())+ "]";
		
		if(startWithNumber(elem)){
			return "The atribute " + getAtributeId() + " of element " + getElementName() + " could not start with number";
		}
		return null;
	}
	
	public String solveProblem(Element elem) throws JSBMLValidatorException{
		
		String message = null;
		
		if(isEmpty(elem)) throw new JSBMLValidatorException();
		
		if(unsuportCaracter(elem) || startWithNumber(elem)){
			String old = elem.getAttribute(getAtributeId());
			String id = getChangedValues().get(old);
			
			if(id == null){
				id = old;
				for(String erro : getInvalidCaracters().keySet()){
					id = id.replaceAll("\\"+erro, getInvalidCaracters().get(erro));
				}
				if(startWithNumber(elem))
					id = "_"+id;
				
				getChangedValues().put(old, id);
			}
			
			elem.setAttribute(getAtributeId(), id);
			message = getElementName() + " was changed the atribute " + getAtributeId() + " from " + old + " to " +id;
		}
		
		
		return message;
	}
	
	public Map<String, String> getChangedValues() {
		return changedValues;
	}
	
	public void setChangedValues(Map<String, String> changedValues) {
		this.changedValues = changedValues;
	}
}
