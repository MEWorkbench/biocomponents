package pt.uminho.ceb.biosystems.mew.biocomponents.io.sbml;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ContainerException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.writers.JSBMLPluginableWriter;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.writers.JSBMLWriter;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public class SBMLCompatibleIds {

	
	
	private static final String COMPARTMENT = "c";

	
	@Test
	public void testIds() throws Exception {
		Container c = containerWithProblems();
		
		JSBMLWriter w = new JSBMLWriter("test.v2.sbml", c);
		w.writeToFile();
		
		System.out.println("V2 writer");
		JSBMLPluginableWriter base = new JSBMLPluginableWriter("test.v3.sbml");
		base.write(c);
		
		System.out.println("V3 writer");
		System.out.println(CollectionUtils.join(base.getWarnings(), "\n"));
		
		
		
	}
	
	Container containerWithProblems() throws ContainerException, IOException {
		Container c = new Container();
		c.addCompartment(new CompartmentCI(COMPARTMENT, COMPARTMENT, null));
		addMetabolite("a.a", c);
		addMetabolite("b#b", c);
		addMetabolite("c|c", c);
		addReaction(c, "r.c", Arrays.asList("a.a"), Arrays.asList("b#b"));
		c.verifyDepBetweenClass();
		
		return c;
	}
	
	void addMetabolite(String id, Container c){
		c.getMetabolites().put(id, new MetaboliteCI(id, id));
	}
	
	void addReaction(Container c, String id, Collection<String> reactants, Collection<String> products) {
		
		ReactionCI r = new ReactionCI(id, id, true, stoiq(reactants), stoiq(products));
		c.getReactions().put(id, r);
	}
	
	Map<String, StoichiometryValueCI> stoiq(Collection<String> s){
		Map<String, StoichiometryValueCI> ret =new HashMap<>();
		for(String x : s) {
			StoichiometryValueCI sv = new StoichiometryValueCI(x, 1.0, COMPARTMENT);
			ret.put(sv.getMetaboliteId(), sv);
		}
		return ret;
	}
	
	
}
