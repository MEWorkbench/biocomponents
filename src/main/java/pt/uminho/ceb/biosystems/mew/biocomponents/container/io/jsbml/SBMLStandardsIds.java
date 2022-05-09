package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;

public class SBMLStandardsIds {

	LinkedHashSet<String> warnings = new LinkedHashSet<>();
	
	public LinkedHashSet<String> getWarnings() {
		return warnings;
	}
	
	public Container standardize(Container container) throws Exception {
		Container c = container.clone();
		warnings = new LinkedHashSet<>();
		
		if (!c.hasUnicIds()) {
			c.useUniqueIds();
		}

		Map<String, String> mets = changeIds(c.getMetabolites().keySet(), "M_");
		Map<String, String> reactions = changeIds(c.getReactions().keySet(), "R_");
		
		addInWarnings("Metabolite id \'%s\' was changed to \'%s\' in order to be compatible with SBML specifications! ", mets);
		addInWarnings("Reaction id \'%s\' was changed to \'%s\' in order to be compatible with SBML specifications! ", reactions);
		
		c.changeMetaboliteIds(mets);
		c.changeReactionIds(reactions);
		c.verifyDepBetweenClass();
		String changedId = reactions.get(container.getBiomassId());
		if(changedId!=null)
			c.setBiomassId(reactions.get(container.getBiomassId()));
		
		return c;
	}
	
	private void addInWarnings(String warnFormat, Map<String, String> changes) {
		for(Entry<String, String> entry : changes.entrySet()) {
			warnings.add(String.format(warnFormat, entry.getKey(), entry.getValue()));
		}
	}


	private Map<String, String> changeIds(Collection<String> ids, String prefix){
		Map<String, String> ret = new HashMap<>();
		
		for(String id : ids) {
			addIfIdChange(id, prefix, ret);
		}
		return ret;
	}
	
	
	private void addIfIdChange(String id, String prefix, Map<String, String> map) {
		
		String newName = id;
		if(!id.toUpperCase().startsWith(prefix)) newName = prefix+id;	
		
		newName = newName.replace("-", "_");
		newName = newName.replace("(", "_");
		newName = newName.replace(")", "_");
		newName = newName.replace(",", "_");
		newName = newName.replace(".", "_");
		newName = newName.replace("[", "_");
		newName = newName.replace("]", "_");
		newName = newName.replace(" ", "_");
		newName = newName.replace("=", "");
		newName = newName.replace(":", "_");
		newName = newName.replace("'", "_");
		newName = newName.replace("+", "_");
		newName = newName.replace("/", "_");
		newName = newName.replace("\\", "_");
		newName = newName.replace("*", "_");
		newName = newName.replace("!", "_");
		newName = newName.replace("?", "_");
		newName = newName.replace("#", "_");
		newName = newName.replace("$", "_");
		newName = newName.replace("@", "_");
		newName = newName.replace("%", "_");
		newName = newName.replace("§", "_");
		newName = newName.replace("{", "_");
		newName = newName.replace("}", "_");
		newName = newName.replace("«", "_");
		newName = newName.replace("»", "_");
		newName = newName.replace("¨", "_");
		newName = newName.replace("^", "_");
		newName = newName.replace("`", "_");
		newName = newName.replace("º", "_");
		newName = newName.replace("ª", "_");
		newName = newName.replace(";", "_");
		newName = newName.replace("£", "_");
		newName = newName.replace("|", "_");
		
		
		if(!id.equals(newName)) {
			map.put(id, newName);
		}
	}
	
}
