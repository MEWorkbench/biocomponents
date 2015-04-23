package pt.uminho.ceb.biosystems.mew.biocomponents.validation.reactions;

import java.util.Collection;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;

public class CompareSameStoiqR implements IEqualReaction{

	private Collection<String> cofactores;
	private boolean ignoreComp;
	private boolean ignoreSValue;
	
	
	
	public CompareSameStoiqR(Collection<String> cofactores, boolean ignoreComp,
			boolean ignoreSValue) {
		this.cofactores = cofactores;
		this.ignoreComp = ignoreComp;
		this.ignoreSValue = ignoreSValue;
	}

	public CompareSameStoiqR(Collection<String> cofactores){
		this(cofactores, true, true);
	}
	
	@Override
	public boolean isEqualReaction(ReactionCI r, ReactionCI r2) {
		
		
		
		
		boolean ret = ReactionUtils.isSameSoiq(r.getReactants(),r2.getReactants(), ignoreComp, ignoreSValue, cofactores);
		ret = ret && ReactionUtils.isSameSoiq(r.getProducts(),r2.getProducts(), ignoreComp, ignoreSValue, cofactores);
		
		if(!ret){
			ret = ReactionUtils.isSameSoiq(r.getReactants(),r2.getProducts(), ignoreComp, ignoreSValue, cofactores);
			ret = ret && ReactionUtils.isSameSoiq(r.getProducts(),r2.getReactants(), ignoreComp, ignoreSValue, cofactores);
		}
		return ret;
	}

}
