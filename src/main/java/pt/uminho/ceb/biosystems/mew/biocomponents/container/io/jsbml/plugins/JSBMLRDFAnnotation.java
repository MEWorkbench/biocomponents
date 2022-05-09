package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.JSBMLIOPlugin;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.plugins.rdf.IdentifiersResources;

public class JSBMLRDFAnnotation implements JSBMLIOPlugin<Object>{

	IdentifiersResources dbMiriantIds;
	
	public JSBMLRDFAnnotation(IdentifiersResources ids){
		dbMiriantIds = ids;
	}
	
	public JSBMLRDFAnnotation() {
		this(IdentifiersResources.defaultIdentifiers());
	}

	@Override
	public String getName() {
		return "rdf.information";
	}

	@Override
	public Object read(Model sbmlModel, Container container, Collection<String> warnings) {
		
		
		return null;
	}

	@Override
	public void write(Model sbmlModel, Container container, Object pluginInfo, SBMLDocument doc) {
		for(int i =0; i < sbmlModel.getSpeciesCount(); i++) {
			Species s = sbmlModel.getSpecies(i);
			
			Set<String> links = getMetaboliteMirianLinks(container, s.getId());
			if(links.size() > 0)s.getAnnotation().addCVTerm(new CVTerm(Qualifier.BQB_IS,links.toArray(new String[] {})));
		}
		
		for(int i =0; i < sbmlModel.getReactionCount(); i++) {
			
			Reaction r = sbmlModel.getReaction(i);
			String reactionId = r.getId();
			
			Set<String> links = getReactionIdentifiers(container, reactionId);
			if(links.size()>0) r.getAnnotation().addCVTerm(new CVTerm(Qualifier.BQB_IS, links.toArray(new String[] {})));
		}
	}

	
	private Set<String> getReactionIdentifiers(Container container, String reactionId) {
		Set<String> links = getMiriantByExtraInfo(reactionId, container.getReactionsExtraInfo(), dbMiriantIds.getReactions());
		
		ReactionCI rci = container.getReaction(reactionId);
		String ec = rci.getEc_number();
		
		if(ec != null && !ec.isEmpty()) links.add("http://identifiers.org/ec-code/"+ec);
 		return links;
	}

	private Set<String> getMetaboliteMirianLinks(Container container, String id) {

		Map<String, Map<String, String>> extraInfo = container.getMetabolitesExtraInfo();
		return getMiriantByExtraInfo(id, extraInfo, dbMiriantIds.getMetabolites());
	}
	
	
	private Set<String> getMiriantByExtraInfo(String id, Map<String, Map<String, String>> extraInfo, Map<String, String> miriant) {
		Set<String> ret = new TreeSet<>();
		
		for(String dbId : miriant.keySet()) {
			String info = extraInfo.getOrDefault(dbId, new HashMap<String, String>()).get(id);
			if(info != null) {
				
				String[] idsToAdd = info.split("\\s*\\|\\s*");
				for(String idToAdd: idsToAdd)
					if(!idToAdd.isEmpty()) ret.add("http://identifiers.org/" + miriant.get(dbId) +"/"+idToAdd);
			}
		}
		return ret;
	}
	
}
