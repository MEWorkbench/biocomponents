package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AbstractNumberValidator implements ElementValidator{

	public abstract String getAtributeId();
	public abstract String getElementName();
	
	
	@Override
	public boolean isValid(Element elem) {
		return isNull(elem) || isNumber(elem);
	}

	private boolean isNull(Element elem) {
		String value = getStringValue(elem);
		return value == null || value.trim().equals("");
	}
	
	private boolean isNumber(Element elem) {
		boolean valid = true;
		Double d = null;
		try {
			d = Double.parseDouble(getStringValue(elem));
		} catch (Exception e) {
		}
		
		if(d == null)
			valid = false;
		return valid;
	}
	
	private String getStringValue(Element elem){
		return elem.getAttribute(getAtributeId());
	}
	
	@Override
	public String reason(Element elem) {
		
		if(!isNull(elem) && !isNumber(elem))
			return "Unsuported number atribute " + getAtributeId() + " of the element " + getElementName() + " [" + elem.getAttribute(getAtributeId()) +  "]";
		
		return null;
	}
	
	public String solveProblem(Document document, Element elem) throws JSBMLValidatorException{
		
		String message = null;
		
//		if(isEmpty(elem)) throw new JSBMLValidatorException();
//		
//		if(unsuportCaracter(elem) || startWithNumber(elem)){
//			String old = elem.getAttribute(getAtributeId());
//			String id = getChangedValues().get(old);
//			
//			if(id == null){
//				id = old;
//				for(String erro : getInvalidCaracters().keySet()){
//					id = id.replaceAll("\\"+erro, getInvalidCaracters().get(erro));
//				}
//				if(startWithNumber(elem))
//					id = "_"+id;
//				
//				getChangedValues().put(old, id);
//			}
//			
//			elem.setAttribute(getAtributeId(), id);
//			message = getElementName() + " was changed the atribute " + getAtributeId() + " from " + old + " to " +id;
//		}
		
		
		return message;
	}
	
	public boolean canBeSolved(Element elem) {
		return false;
	};
	
//	public Map<String, String> getChangedValues() {
//		return changedValues;
//	}
//	
//	public void setChangedValues(Map<String, String> changedValues) {
//		this.changedValues = changedValues;
//	}
}
