package pt.uminho.ceb.biosystems.mew.biocomponents.validation.reactions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;

public class ReactionUtils {
	
	public static boolean isSameSoiq(Map<String, StoichiometryValueCI>s1, Map<String, StoichiometryValueCI>s2, boolean ignoreComp, boolean ignoreSValue, Collection<String> metToignore){
	
		Set<String> idsS1 = new HashSet<String>(s1.keySet());
		Set<String> idsS2 = new HashSet<String>(s2.keySet());
		
		idsS1.removeAll(metToignore);
		idsS2.removeAll(metToignore);
		
		boolean ret = idsS1.size() == idsS2.size();
		
		if(ret){
			
			for(String id : idsS1){
				
				StoichiometryValueCI st1 =s1.get(id);
				StoichiometryValueCI st2 = s2.get(id);
				
				ret = compareStoichiometryValueCI(st1, st2, ignoreComp, ignoreSValue);
				if(!ret)
					break;
			}
		}
		
		return ret;
	}

	public static boolean compareStoichiometryValueCI(
			StoichiometryValueCI st1, StoichiometryValueCI st2,
			boolean ignoreComp, boolean ignoreSValue) {
		boolean ret = false;
		if(st1!=null && st2!=null){
			ret = st1.getMetaboliteId().equals(st2.getMetaboliteId());
			
			if(ret && !ignoreComp) ret = st1.getCompartmentId().equals(st2.getCompartmentId());
			if(ret && !ignoreSValue) ret = st1.getStoichiometryValue().equals(st2.getStoichiometryValue());
		}
		return ret;
	}

	public static boolean isCyclic(boolean revR1, boolean revR2, boolean sameDir){
		
		boolean ret = revR1 || revR2;
		
		if(!ret){
			ret= !sameDir;
		}
		return ret;
	}
	
}
