package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public abstract class AbstractIDValidator implements ElementValidator{

	private Map<String, String> changedValues;
	private Map<String, String> invalidCarac;

	public abstract String getAtributeId();
	public abstract String getElementName();
	
	
	public AbstractIDValidator() {
		changedValues = new HashMap<String, String>();
	}
	
	public Map<String, String> getInvalidCaracters(){
		if(invalidCarac==null){
			invalidCarac = new HashMap<String, String>();
			invalidCarac.put("-", "_DASH_");
			invalidCarac.put(" ", "_");
			invalidCarac.put("(", "_LPAREN_");
			invalidCarac.put(")", "_RPAREN_");
			invalidCarac.put(",", "_COMMA_");
			invalidCarac.put("+", "_PLUS_");
			invalidCarac.put("[", "_LBRACKET_");
			invalidCarac.put("]", "_RBRACKET_");
			invalidCarac.put("'", "_APOSTR_");
			invalidCarac.put(":", "_COLON_");
			invalidCarac.put(".", "_DOT_");
			invalidCarac.put("/", "_SLASH_");
			invalidCarac.put("\\", "_BACKSLASH_");
			invalidCarac.put("*", "_ASTERISK_");
			invalidCarac.put("!", "_EXCLAM_");
			invalidCarac.put("?", "_QUESTION_");
			invalidCarac.put("#", "_HASH_");
			invalidCarac.put("$", "_DOLLAR_");
			invalidCarac.put("@", "_ATSIGN_");
			invalidCarac.put("%", "_PERCENT_");
			invalidCarac.put("§", "_SECTION_");
			invalidCarac.put("{", "_LCURLY_");
			invalidCarac.put("}", "_RCURLY_");
			invalidCarac.put("=", "_EQUALS_");
			invalidCarac.put("«", "_DLESS_");
			invalidCarac.put("»", "_DGREATER_");
			invalidCarac.put("¨", "_PAIRDOTS_");
			invalidCarac.put("^", "_CIRCUMFLEX_");
			invalidCarac.put("`", "_ACUTE_");
			invalidCarac.put("º", "_MORDINAL_");
			invalidCarac.put("ª", "_FORDINAL_");
			invalidCarac.put(";", "_SEMICOLON_");
			invalidCarac.put("£", "_POUND_");
			invalidCarac.put("|", "_VBAR_");
			
		}
		return invalidCarac;
	}
	
	@Override
	public boolean isValid(Element elem) {
		return !isEmpty(elem) && !unsuportCaracter(elem) && !startWithNumber(elem);
	}

	public boolean startWithNumber(Element elem){
		String toTest = elem.getAttribute(getAtributeId());
		return toTest.matches("\\d+.*");
	}
	
	protected boolean unsuportCaracter(Element elem){
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
			return getElementName() + " has the attribute "+ getAtributeId()+ " empty";
		else if(unsuportCaracter(elem))
			return "Unsuported characters in attribute " + getAtributeId() + " of the element " + getElementName() + " [" + elem.getAttribute(getAtributeId())+ "]";
		
		if(startWithNumber(elem)){
			return "The attribute " + getAtributeId() + " of element " + getElementName() + " could not start with number";
		}
		return null;
	}
	
	public String solveProblem(Document document, Element elem) throws JSBMLValidatorException{
		
		String message = null;
		
		//if(isEmpty(elem)) throw new JSBMLValidatorException();
		
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
			message = getElementName() + " was changed. The atribute " + getAtributeId() + " changed " + old + " to " +id;
		}
		
		return message;
	}
	
	@Override
	public boolean canBeSolved(Element elem) {
		return true;
	}
	
	public Map<String, String> getChangedValues() {
		return changedValues;
	}
	
	public void setChangedValues(Map<String, String> changedValues) {
		this.changedValues = changedValues;
	}
}
