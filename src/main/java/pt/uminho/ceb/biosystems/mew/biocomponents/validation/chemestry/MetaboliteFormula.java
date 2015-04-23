package pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaboliteFormula implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final static Pattern pattern = Pattern.compile("([A-Z][a-z]*)(\\d*)");
	protected String originalFormula;
	protected Map<String, Integer> elements;
	protected Boolean isGeneric = null;

	public MetaboliteFormula(String formula) {

		this.originalFormula = formula;
		this.elements = parserFormula(formula);
	}
	
	public MetaboliteFormula(String formula, Map<String, Integer> elements ){
		this.originalFormula = formula;
		this.elements = elements;
	}


	public Integer getValue(String comp) {

		Integer ret = elements.get(comp);
		ret = (ret != null) ? ret : 0;
		return ret;
	}

	public Set<String> getComponets() {
		return elements.keySet();
	}

	public String toString() {
		String ret = originalFormula + "\t=>\t";

		for (String comp : elements.keySet()) {

			ret += comp + getValue(comp) + " | ";
		}

		return ret;
	}

	public String getOriginalFormula() {
		return originalFormula;
	}

	public Boolean isGeneric() {
		if (isGeneric == null)
			isGeneric = elements.containsKey("R");
		return isGeneric;
	}

	static public Map<String, Integer> parserFormula(String formula) {
		Map<String, Integer> map = new HashMap<String, Integer>();

//		System.out.println("Parser formula: " + formula);
		Matcher matcher = pattern.matcher(formula);
		while (matcher.find()) {

			String comp = matcher.group(1);
			String value = matcher.group(2);

			Integer n = 1;
			if (value != null && !value.equals("")) {
				n = Integer.parseInt(value);
			}

			Integer correntValue = map.get(comp);
			n += (correntValue != null ? correntValue : 0);

			map.put(comp, n);
		}

		return map;
	}
	
	public MetaboliteFormula changeCharge(Integer charge) {
		Map<String, Integer> map = new HashMap<String, Integer>(elements);
		String element = "H";
		String formula = "";
		if(map.containsKey(element) && map.size() > 2 && map.get("C") > 1) {
			charge += map.get(element);
		
			if(charge <= 0)
				map.remove(element);
			else
				map.put(element, charge);
			
			for(String e : map.keySet())
				formula += e + (map.get(e) > 0 ? map.get(e) : "");
		}
		else
			formula = originalFormula;
		
		return new MetaboliteFormula(formula);

	}
	
	public static String formulaToString(Map<String, Integer> elements){
		
		String f = "";
		
		for(String e : elements.keySet()){
			Integer n = elements.get(e);
			if(n!=0)
				f+=e+""+n;
			
		}
		
		return f;
	}
	
	public MetaboliteFormula addFormula(MetaboliteFormula toAdd){
		
		LinkedHashMap<String, Integer> newMap = new LinkedHashMap<String, Integer>(this.elements);
		
		Set<String> elemToAdd= toAdd.getComponets();
		
		for(String e : elemToAdd){
			Integer value = newMap.get(e);
			if(value==null) value = 0;
			value+= toAdd.getValue(e);
			newMap.put(e, value);
		}
		
		return new MetaboliteFormula(formulaToString(newMap), newMap);
	}
	
	public MetaboliteFormula removeFormula(MetaboliteFormula toRemove) throws MetaboliteFormulaExeption{
		
		LinkedHashMap<String, Integer> newMap = new LinkedHashMap<String, Integer>(this.elements);
		
		Set<String> elemToRem= toRemove.getComponets();
		
		Map<String, Integer> negativeElements = new HashMap<String, Integer>();
		for(String e : elemToRem){
			Integer value = newMap.get(e);
			if(value==null) value = 0;
			value-= toRemove.getValue(e);
			
			if(value<0)
				negativeElements.put(e, value);
			newMap.put(e, value);
		}
		
		if(negativeElements.size()>0)
			throw new MetaboliteFormulaExeption("The remove opperation is not possible: negative elements " + negativeElements.toString());
		
		return new MetaboliteFormula(formulaToString(newMap), newMap);
	}

}
