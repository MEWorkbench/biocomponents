package pt.uminho.ceb.biosystems.mew.biocomponents.validation.reactions;

import java.util.Collection;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;

public class IsCycleParReactions implements IEqualReaction{
	
	private Collection<String> cofactores;
	private boolean ignoreComp;
	private boolean ignoreSValue;
	
	public IsCycleParReactions(Collection<String> cofactores, boolean ignoreComp,
			boolean ignoreSValue) {
		
		this.cofactores = cofactores;
		this.ignoreComp = ignoreComp;
		this.ignoreSValue = ignoreSValue;
	}

	public IsCycleParReactions(Collection<String> cofactores){
		this(cofactores, false, true);
	}
	
	@Override
	public boolean isEqualReaction(ReactionCI r, ReactionCI r2) {
		
		boolean ret = false;
		
		boolean samedir = ReactionUtils.isSameSoiq(r.getReactants(),r2.getReactants(), ignoreComp, ignoreSValue, cofactores);
		samedir = samedir && ReactionUtils.isSameSoiq(r.getProducts(),r2.getProducts(), ignoreComp, ignoreSValue, cofactores);
		
		boolean reversedir =false;
		if(!samedir){
			reversedir = ReactionUtils.isSameSoiq(r.getReactants(),r2.getProducts(), ignoreComp, ignoreSValue, cofactores);
			reversedir = reversedir && ReactionUtils.isSameSoiq(r.getProducts(),r2.getReactants(), ignoreComp, ignoreSValue, cofactores);
		}
		
		
		if(samedir || reversedir){
			ret = r.getReversible() || r2.getReversible();
			ret= ret || reversedir;
		}
		
		return ret;
	}

	
}
