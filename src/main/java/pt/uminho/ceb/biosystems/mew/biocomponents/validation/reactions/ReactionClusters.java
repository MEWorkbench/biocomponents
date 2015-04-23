package pt.uminho.ceb.biosystems.mew.biocomponents.validation.reactions;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;

public class ReactionClusters{
	
	
	protected String id;
	protected IEqualReaction comparator;
	protected Container cont;
	protected List<Set<String>> reactionsIds;
		
	public ReactionClusters(IEqualReaction comparator, Container cont){
		this.cont = cont;
		this.comparator = comparator;
	}
	
	public List<Set<String>> getReactionIdClusters(){
		
		if(reactionsIds==null)
			reactionsIds = calculateClusters();
		
		return reactionsIds;
	}

	private List<Set<String>> calculateClusters() {
		
		ArrayList<Set<String>> ret = new ArrayList<Set<String>>();
		Set<String> _2It = new HashSet<String>(cont.getReactions().keySet());
		
		for(ReactionCI r : cont.getReactions().values()){
			String rid = r.getId();
			_2It.remove(r.getId());
			
			Set<String> cluster = new HashSet<String>();
			cluster.add(rid);
			for(String rToCompare : _2It){
				ReactionCI r2 = cont.getReaction(rToCompare);
				if(comparator.isEqualReaction(r, r2)) cluster.add(rToCompare);
			}
			
			
			if(cluster.size()>1){
				_2It.removeAll(cluster);
				ret.add(cluster);
			}
		}
		
		return ret;
	}
	
}
