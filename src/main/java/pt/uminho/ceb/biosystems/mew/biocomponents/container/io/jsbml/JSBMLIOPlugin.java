package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml;

import java.util.Collection;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;

public interface JSBMLIOPlugin<T> {

	String getName();
	T read(Model sbmlModel, Container container, Collection<String> warnings);
	void write(Model sbmlModel, Container container, T pluginInfo, SBMLDocument doc);
}
