package pt.uminho.ceb.biosystems.mew.biocomponents.validation.reactions;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;

public interface IEqualReaction {
	
	boolean isEqualReaction(ReactionCI r, ReactionCI r2);

}
