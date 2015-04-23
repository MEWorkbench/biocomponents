package pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.jsbml.validators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AbstractDuplicatedValidator implements ElementValidator{

	private Set<String> uniqueElementList;
	private List<String> duplicatedElemetList;
	
	private Set<String> extraUniqueElementList;
	private List<String> extraDuplicatedElemetList;
	
	private String DUPLICATED_SUFIX = "_CLONE_";
	private int ITERATOR = 1;
	
	public abstract String getAtributeId();
	public abstract String getElementName();
	
	public AbstractDuplicatedValidator() {
		uniqueElementList = new HashSet<String>();
		duplicatedElemetList = new ArrayList<String>();
		
		extraUniqueElementList = new HashSet<String>();
		extraDuplicatedElemetList = new ArrayList<String>();
	}
		
	@Override
	public boolean isValid(Element elem) {
		
		if(isDuplicated(elem))
		{
			duplicatedElemetList.add(elem.getAttribute(getAtributeId()));
			return false;
		}
		uniqueElementList.add(elem.getAttribute(getAtributeId()));
		return true;
	}
	
	private boolean isDuplicated(Element elem) {
		return uniqueElementList.contains(elem.getAttribute(getAtributeId()));
	}
	
	public Set<String> getAllElementsList() {
		return uniqueElementList;
	}
	
	@Override
	public String reason(Element elem) {
		if(isDuplicated(elem))
			return "The " + getElementName() + " " + getAtributeId()+ " '" +elem.getAttribute(getAtributeId()) + "' is duplicated";
		
		return null;
	}
	
	public String solveProblem(Document document, Element elem) throws JSBMLValidatorException{
		if(!canBeSolved(elem))
			return null;
		
		String value = elem.getAttribute(getAtributeId());
		
		String message = null;
		if(extraUniqueElementList.contains(value))
		{
			String generatedID = idGenerator(elem.getAttribute(getAtributeId()));
			message = "Changed duplicated " + getAtributeId() + " from: '"+ elem.getAttribute(getAtributeId()) + "' to '" + generatedID+"'";
			elem.setAttribute(getAtributeId(), generatedID);
		}
		else
			extraUniqueElementList.add(value);
		
		return message;
	}
	
	@Override
	public boolean canBeSolved(Element elem) {
		return true;
	}
	
	private String idGenerator(String value)
	{
		return idGenerator(value, ITERATOR);
	}
	
	private String idGenerator(String value, int index)
	{
		String newValue = value + DUPLICATED_SUFIX + index;
		if(extraUniqueElementList.contains(newValue) && extraDuplicatedElemetList.contains(newValue))
			return idGenerator(value, index+1);
		else
		{
			extraUniqueElementList.add(newValue);
			extraDuplicatedElemetList.add(newValue);
			return newValue;
		}
	}
}
