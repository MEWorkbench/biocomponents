package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.writers;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLWriter;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.JSBMLIOBase;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.plugins.JBMLBaseInformation;

public class JSBMLPluginableWriter extends JSBMLIOBase{
	
	
	
	private static final int DEFAULT_SBML_LEVEL = 3;
	private static final int DEFAULT_SBML_VERSION = 1;
	
	private int version;
	private int level;
	private String fileName;

	public JSBMLPluginableWriter(String fileName){
		this(fileName, DEFAULT_SBML_LEVEL, DEFAULT_SBML_VERSION);
	}
	
	
	public JSBMLPluginableWriter(String fileName, int level, int version ){
		this.version = version;
		this.level = level;
		this.fileName = fileName;
		addPlugin(new JBMLBaseInformation());
	}

	
	public void write(Container c) throws Exception {
		
		setContainer(c);
		createNewModel(level, version, c.getModelName(), "");
		SBMLDocument doc = writePlugin();
		
		SBMLWriter writer = new SBMLWriter();
		
		writer.writeSBMLToFile(doc, fileName);
		
	}
}
