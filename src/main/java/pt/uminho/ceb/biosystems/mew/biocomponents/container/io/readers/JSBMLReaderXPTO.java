package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.util.converters.ExpandFunctionDefinitionConverter;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.interfaces.IContainerBuilder;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.JSBMLIOBase;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.plugins.JBMLBaseInformation;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidationException;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidator;
import pt.uminho.ceb.biosystems.mew.utilities.io.IOUtils;

public class JSBMLReaderXPTO extends JSBMLIOBase implements IContainerBuilder{

	
	boolean expandFunction = false;
	
	public JSBMLReaderXPTO(InputStream stream) throws JSBMLValidationException{
		readSBMLModel(stream);
	}
	
	
	@Override
	public Container getContainer() {
		Container c = super.getContainer();
		if(c == null){
			createEmptyContainer();
			readPlugins();
			c = super.getContainer();
		} 
		
		return c;
	}
	
	@Override
	public String getModelName() {
		return getContainer().getModelName();
	}

	@Override
	public String getOrganismName() {
		return getContainer().getOrganismName();
	}

	@Override
	public String getNotes() {
		return getContainer().getNotes();
	}

	@Override
	public Integer getVersion() {
		return 0;
	}

	@Override
	public Map<String, CompartmentCI> getCompartments() {
		return getContainer().getCompartments();
	}

	@Override
	public Map<String, ReactionCI> getReactions() {
		return getContainer().getReactions();
	}

	@Override
	public Map<String, MetaboliteCI> getMetabolites() {
		return getContainer().getMetabolites();
	}

	@Override
	public Map<String, GeneCI> getGenes() {
		return getContainer().getGenes();
	}

	@Override
	public Map<String, Map<String, String>> getMetabolitesExtraInfo() {
		return getContainer().getMetabolitesExtraInfo();
	}

	@Override
	public Map<String, Map<String, String>> getReactionsExtraInfo() {
		return getContainer().getReactionsExtraInfo();
	}

	@Override
	public String getBiomassId() {
		return getContainer().getBiomassId();
	}

	@Override
	public Map<String, ReactionConstraintCI> getDefaultEC() {
		return getContainer().getDefaultEC();
	}

	@Override
	public String getExternalCompartmentId() {
		return getContainer().getExternalCompartmentId();
	}

	protected void readSBMLModel(InputStream stream ) throws JSBMLValidationException{
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			IOUtils.copy(stream, baos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStream is1 = new ByteArrayInputStream(baos.toByteArray()); 
		InputStream is2 = new ByteArrayInputStream(baos.toByteArray()); 
		
		SBMLReader reader = new SBMLReader();
		SBMLDocument doc = null;
		try {
			doc = reader.readSBMLFromStream(is1);
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	
		
		SBMLDocument docToValidate = doc;
		
		if (expandFunction && doc.isSetModel() && doc.getModel().getFunctionDefinitionCount() > 0) {
	        ExpandFunctionDefinitionConverter converter = new ExpandFunctionDefinitionConverter();
	        docToValidate = converter.convert(doc);
	      }
		
//		int l = doc.getLevel();
//		int v = doc.getVersion();
//		LoggingValidationContext ctx = new LoggingValidationContext(l, v);
//		ctx.loadConstraints(Species.class);
//		ctx.enableCheckCategory(CHECK_CATEGORY.GENERAL_CONSISTENCY, true);
//		ctx.enableCheckCategory(CHECK_CATEGORY.IDENTIFIER_CONSISTENCY, true);
//		ctx.enableCheckCategory(CHECK_CATEGORY.MODELING_PRACTICE, true);
//		ctx.enableCheckCategory(CHECK_CATEGORY.SBO_CONSISTENCY, true);
//		
//		
//		Boolean b = ctx.validate(docToValidate);
//		
//		
//		SBMLErrorLog erros = ctx.getErrorLog();
//		System.out.println("VALID: "+ b + erros.getValidationErrors().size());
//		for(int i =0; i < erros.getErrorCount(); i++){
//			SBMLError x = erros.getError(i);
//			String error = String.format("[%d][%s] %s %d:%d", x.getSeverity(), x.getCategory(), x.getShortMessage(), x.getLine(), x.getColumn());
//			warnings.add(error);
//		}
		
		JSBMLValidator validator = new JSBMLValidator(is2);
		validator.validate();
		
		setSbmlModel(docToValidate.getModel());
	}
}
