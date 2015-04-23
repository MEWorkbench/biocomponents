package pt.uminho.ceb.biosystems.mew.biocomponents.container.components;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class GeneCI implements Serializable, Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String geneId;
	protected String geneName;
	
	protected Set<String> reactionIds;
	
	public GeneCI(String geneId, String geneName) {
		this.geneId = geneId;
		this.geneName = geneName;
		this.reactionIds = new HashSet<String>();
	}

	public GeneCI(GeneCI geneCI) {
		this.geneId = geneCI.geneId;
		this.geneName = geneCI.geneName;
		this.reactionIds = new HashSet<String>(geneCI.reactionIds);
	}

	public String getGeneId() {
		return geneId;
	}

	public void setGeneId(String geneId) {
		this.geneId = geneId;
	}

	public String getGeneName() {
		return geneName;
	}

	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}

	public void addReactionId(String reactionId) {
		reactionIds.add(reactionId);
	}

	public Set<String> getReactionIds() {
		return reactionIds;
	}
	
	
	@Override
	public GeneCI clone(){
		return new GeneCI(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GeneCI [geneId=" + geneId + ", geneName=" + geneName
				+ ", reactionIds=" + reactionIds + "]";
	}
	
	
}
