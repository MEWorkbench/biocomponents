package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.JSBMLIOPlugin;

public class JSBMLRDFAnnotation implements JSBMLIOPlugin<Object>{

	Map<String, String> dbMiriantIds;
	
	public JSBMLRDFAnnotation(IdentifiersResources ids){
		dbMiriantIds = ids.metabolites;
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
			
			String[] links = getMetaboliteMirianLinks(container, s.getId());
			if(links.length > 0)s.getAnnotation().addCVTerm(new CVTerm(Qualifier.BQB_IS,links));
		}
		
		for(int i =0; i < sbmlModel.getReactionCount(); i++) {
			
			Reaction r = sbmlModel.getReaction(i);
			String reactionId = r.getId();
			
			Set<String> links = getReactionIdentifiers(container, reactionId);
			if(links.size()>0) r.getAnnotation().addCVTerm(new CVTerm(Qualifier.BQB_IS, links.toArray(new String[] {})));
		}
	}

	
	private Set<String> getReactionIdentifiers(Container container, String reactionId) {
		Set<String> links = new HashSet<>();
		
		ReactionCI rci = container.getReaction(reactionId);
		String ec = rci.getEc_number();
		
		if(ec != null && !ec.isEmpty()) links.add("http://identifiers.org/ec-code/"+ec);
 		return links;
	}

	private String[] getMetaboliteMirianLinks(Container container, String id) {

		Map<String, Map<String, String>> extraInfo = container.getMetabolitesExtraInfo();
		ArrayList<String> ret = new ArrayList<>();
		
		for(String dbId : dbMiriantIds.keySet()) {
			String info = extraInfo.getOrDefault(dbId, new HashMap<String, String>()).get(id);
			if(info != null) {
				
				String[] idsToAdd = info.split("\\s*\\|\\s*");
				for(String idToAdd: idsToAdd)
					if(!idToAdd.isEmpty()) ret.add("http://identifiers.org/" + dbMiriantIds.get(dbId) +"/"+idToAdd);
			}
		}
		return ret.toArray(new String[] {});
	}

	
	
	
	
	static public class IdentifiersResources{
		
		public static IdentifiersResources defaultIdentifiers() {
			Map<String, String> metabolites = new HashMap<>();
			metabolites.put("KEGG_CPD", "kegg.compound");
			metabolites.put("METACYC_CPD", "biocyc");
			metabolites.put("SEED_CPD", "seed.compound");
			metabolites.put("CHEBI", "chebi");
			metabolites.put("metanetx", "metanetx.chemical");
		
			Map<String, String> reactions = new HashMap<>();
			return new IdentifiersResources(metabolites, reactions);
		}
		
		
		Map<String, String> metabolites;
		Map<String, String> reactions;
		
		
		public IdentifiersResources(Map<String, String> metabolites, Map<String, String> reactions) {
			super();
			this.metabolites = metabolites;
			this.reactions = reactions;
		}


		public Map<String, String> getMetabolites() {
			return metabolites;
		}


		public Map<String, String> getReactions() {
			return reactions;
		}
		
		
	}
	
}
