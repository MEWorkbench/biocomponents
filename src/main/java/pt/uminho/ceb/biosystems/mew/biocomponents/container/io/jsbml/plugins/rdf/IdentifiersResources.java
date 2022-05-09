package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.plugins.rdf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class IdentifiersResources implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public static IdentifiersResources defaultIdentifiers() {
		Map<String, String> metabolites = new HashMap<>();
		metabolites.put("KEGG_CPD", "kegg.compound");
		metabolites.put("METACYC_CPD", "biocyc");
		metabolites.put("SEED_CPD", "seed.compound");
		metabolites.put("CHEBI", "chebi");
		metabolites.put("MetaNetX", "metanetx.chemical");
		metabolites.put("BiGG", "bigg.metabolite");

		Map<String, String> reactions = new HashMap<>();
		reactions.put("MetaNetX", "metanetx.reaction");
		reactions.put("SEED", "seed.reaction");
		reactions.put("KEGG", "kegg.reaction");
		reactions.put("BiGG", "bigg.reaction");
		reactions.put("MetaCyc", "biocyc");
		return new IdentifiersResources(metabolites, reactions);
	}


	protected Map<String, String> metabolites;
	protected Map<String, String> reactions;


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

	public void addMetaboliteLink(String externalId, String miriamId) {
		metabolites.values().remove(miriamId);
		metabolites.put(externalId, miriamId);
	}

	public void addReactionLinks(String externalId, String miriamId) {
		reactions.values().remove(miriamId);
		metabolites.put(externalId, miriamId);
	}


}