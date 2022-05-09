package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.util.ModelBuilder;
import org.sbml.jsbml.util.converters.ExpandFunctionDefinitionConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.plugins.JBMLBaseInformation;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidationException;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidator;
import pt.uminho.ceb.biosystems.mew.utilities.io.IOUtils;

public class JSBMLIOBase {

	
	private static final Logger logger = LoggerFactory.getLogger(JSBMLIOBase.class);
	Model sbmlModel;
	Container container;
	Map<String, JSBMLIOPlugin> plugins;
	Map<String,Object> pluginsInfo;
	ArrayList<String> warnings;
	ModelBuilder mb;
	
	
	
	public JSBMLIOBase() {
		plugins = new LinkedHashMap<>();
		pluginsInfo = new HashMap<>();
		warnings = new ArrayList<String>();
		addPlugin(new JBMLBaseInformation());
	}
	
	public void addPlugin(JSBMLIOPlugin sbmlPlugin){
		plugins.put(sbmlPlugin.getName(), sbmlPlugin);
	}
	
	
	public <T extends Object> void addpluginsInfo(String pluginId, T info){
		if(plugins.containsKey(pluginId)) throw new IllegalArgumentException("Plugin id " + pluginId + " was not registered!!!");
		pluginsInfo.put(pluginId, info);
	}
	
	public Model getSbmlModel() {
		return sbmlModel;
	}
	
	
	protected void readPlugins(){
		
		
		for(String id : plugins.keySet()){
			pluginsInfo.put(id, plugins.get(id).read(sbmlModel, container, warnings));
		}
		
	}
	
	
	/**
	 * need to call createNewModel before
	 * @return
	 */
	protected SBMLDocument writePlugin(){
		
		SBMLDocument doc = getSbmlModel().getSBMLDocument();
		for(String id : plugins.keySet()){
			
			System.out.println("WRITE " + id);
			plugins.get(id).write(sbmlModel, container,pluginsInfo.get(id), doc);
		}
		return doc;
	}
	
	
	protected void createNewModel(int level, int version, String id, String name){
		mb = new ModelBuilder(level, version);
		sbmlModel = mb.buildModel(id, name);
	}
	
	
	
	protected void createEmptyContainer(){
		container = new Container();
	}
	
	public Container getContainer() {
		return container;
	}
	
	public ArrayList<String> getWarnings() {
		return warnings;
	}
	
	public void setSbmlModel(Model sbmlModel) {
		this.sbmlModel = sbmlModel;
	}
	
	public void setContainer(Container container) {
		this.container = container;
	}
}
