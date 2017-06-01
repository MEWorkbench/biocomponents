package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.writers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public class JSONWriter {
	
	
	// These variables could be in a JSON utils
	public final static String REAC_PREFIX = "R_";
	public final static String META_PREFIX = "M_";
	
	public final static String FIELD_ID = "id";
	public final static String FIELD_NAME = "name";
	public final static String FIELD_SUBSYSTEM = "subsystem";
	public final static String FIELD_OBJ_COEF = "objective_coefficient";
	public final static String FIELD_LOWER_BOUND = "lower_bound";
	public final static String FIELD_UPPER_BOUND = "upper_bound";
	public final static String FIELD_GPR = "gene_reaction_rule";
	public final static String FIELD_NOTES = "notes";
	public final static String FIELD_COMPARTMENT = "compartment";
	public final static String FIELD_CHARGE = "charge";
	public final static String FIELD_FORMULA = "formula";
	public final static String FIELD_NOTES_BIGG_IDS = "original_bigg_ids";
	
	public final static String FIELD_REACTIONS = "reactions";
	public final static String FIELD_METABOLITES = "metabolites";
	public final static String FIELD_COMPARTMENTS = "compartments";
	public final static String FIELD_GENES = "genes";
	public final static String FIELD_VERSION = "version";
	
	public final static String NOTES_SPLIT_CHAR = ";";
	
	private Writer out;
	
	private String path;
	private Container container;
	
	protected boolean addPrefix = true;
	
	
	public JSONWriter(Writer out , Container container){
		
		this.out = out;
		this.container = container;
		if(!container.hasUnicIds()){
			this.container = container.clone();
			this.container.useUniqueIds();
		}
	}
	
	public JSONWriter(String path, Container container) {
		this.path = path;
		this.container = container;
		if(!container.hasUnicIds()){
			this.container = container.clone();
			this.container.useUniqueIds();
		}
		
	}
	
	public void writeToFile() throws IOException {
		this.writeToFile(true);
	}
	
	public void writeToFile(boolean addPerfix) throws IOException {
		this.addPrefix = addPerfix;
		
		if(out == null)
			out = new FileWriter(path);
		
		JsonGenerator node = new JsonFactory().createGenerator(out);
		node.setPrettyPrinter(new DefaultPrettyPrinter());
		node.writeStartObject();
		
		writeReactions(node);
		writeGenes(node);
		writeCompartments(node);
		writeMetabolites(node);
		
		node.writeStringField(FIELD_VERSION, container.getVersion().toString());
		
		node.writeStringField(FIELD_ID, container.getModelName());
		
		node.writeEndObject();
		
		node.flush();
		node.close();
	}
	
	protected void writeReactions(JsonGenerator node) throws IOException{
		node.writeFieldName(FIELD_REACTIONS);
		node.writeStartArray();
		
		for (ReactionCI reaction : container.getReactions().values()) {
			writeSingleReaction(node, reaction);
		}
		node.writeEndArray();
	}
	
	protected void writeSingleReaction(JsonGenerator node, ReactionCI reaction) throws IOException{
		node.writeStartObject();
		String subsystem = reaction.getSubsystem();
		String name = reaction.getName();
		String id = convertWithPrefix(reaction.getId(), REAC_PREFIX);
		ReactionConstraintCI constraints = container.getDefaultEC().get(reaction.getId());
		double upper = constraints.getUpperLimit();
		double lower = constraints.getLowerLimit();
		
		node.writeStringField(FIELD_SUBSYSTEM, subsystem);
		node.writeStringField(FIELD_NAME, name);
		node.writeNumberField(FIELD_UPPER_BOUND, upper);
		node.writeNumberField(FIELD_LOWER_BOUND, lower);
		
		writeReactionNotes(node, reaction);

		writeReactantsAndProducts(node, reaction);
		
		if(reaction.getId().equals(container.getBiomassId())){
			node.writeNumberField(FIELD_OBJ_COEF, 1);
		}
		node.writeStringField(FIELD_ID, id);
		node.writeStringField(FIELD_GPR, reaction.getGeneRuleString());
		node.writeEndObject();
	}
	
	protected void writeReactantsAndProducts(JsonGenerator node, ReactionCI reaction) throws IOException{
		Map<String, StoichiometryValueCI> reactants = reaction.getReactants();
		Map<String, StoichiometryValueCI> products = reaction.getProducts();
		
		node.writeFieldName(FIELD_METABOLITES);
		
		node.writeStartObject();
		for (String react : reactants.keySet()) {
			node.writeNumberField(convertWithPrefix(react, META_PREFIX), -reactants.get(react).getStoichiometryValue());
		}
		
		for (String prod : products.keySet()) {
			node.writeNumberField(convertWithPrefix(prod, META_PREFIX), products.get(prod).getStoichiometryValue());
		}
		node.writeEndObject();
	}
	
	protected void writeReactionNotes(JsonGenerator node, ReactionCI reaction) throws IOException{
		node.writeFieldName(FIELD_NOTES);
		
		node.writeStartObject();
		
		handleNotes(node, reaction.getId(), container.getReactionsExtraInfo());
		
		node.writeEndObject();
	}
	
	protected void handleNotes(JsonGenerator node, String id, Map<String, Map<String, String>> map) throws IOException{
		for (String noteId : map.keySet()) {
			node.writeFieldName(noteId);
			
			node.writeStartArray();
			
			String toWrite = map.get(noteId).get(id);
			if(noteId.equals(FIELD_NOTES_BIGG_IDS)){
				Set<String> splitted = CollectionUtils.split(toWrite, NOTES_SPLIT_CHAR);
				for (String s : splitted) {
					node.writeString(s);
				}
			}else{
				node.writeString(toWrite);
			}
			
			node.writeEndArray();
		}
	}
	
	protected void writeGenes(JsonGenerator node) throws IOException{
		node.writeFieldName(FIELD_GENES);
		node.writeStartArray();
		
		for (GeneCI gene : container.getGenes().values()) {
			writeSingleGene(node, gene);
		}
		node.writeEndArray();
	}
	
	protected void writeSingleGene(JsonGenerator node, GeneCI gene) throws IOException{
		node.writeStartObject();
		String name = gene.getGeneName();
		String id = gene.getGeneId();
		name = (name != null && !name.isEmpty()) ? name : id;

		node.writeStringField(FIELD_NAME, name);
		node.writeStringField(FIELD_ID, id);
		
		node.writeEndObject();
	}
	
	protected void writeCompartments(JsonGenerator node) throws IOException{
		node.writeFieldName(FIELD_COMPARTMENTS);
		
		node.writeStartObject();
		for (CompartmentCI compartment : container.getCompartments().values()) {
			node.writeStringField(compartment.getId(), compartment.getName());
		}
		node.writeEndObject();
	}
	
	protected void writeMetabolites(JsonGenerator node) throws IOException{
		node.writeFieldName(FIELD_METABOLITES);
		node.writeStartArray();
		
		for (MetaboliteCI metabolite : container.getMetabolites().values()) {
			writeSingleMetabolite(node, metabolite);
		}
		node.writeEndArray();
	}
	
	protected void writeSingleMetabolite(JsonGenerator node, MetaboliteCI metabolite) throws IOException{
		node.writeStartObject();
		String formula = metabolite.getFormula();
		String id = convertWithPrefix(metabolite.getId(), META_PREFIX);
		
		// CHANGE THIS!
		String compartment = container.getMetaboliteCompartments(metabolite.getId()).iterator().next();
		
		String name = metabolite.getName();
		
		node.writeStringField(FIELD_FORMULA, formula);
		
		writeMetabolitesNotes(node, metabolite);
		
		node.writeStringField(FIELD_COMPARTMENT, compartment);
		node.writeStringField(FIELD_NAME, name);
		node.writeStringField(FIELD_ID, id);

		node.writeEndObject();
	}
	
	protected void writeMetabolitesNotes(JsonGenerator node, MetaboliteCI metabolite) throws IOException{
		node.writeFieldName(FIELD_NOTES);
		
		node.writeStartObject();
		
		handleNotes(node, metabolite.getId(), container.getMetabolitesExtraInfo());
		
		node.writeEndObject();
	}
	
	protected String convertWithPrefix(String original, String prefix){
		if(addPrefix){
			if(!original.startsWith(prefix)){
				return prefix + original;
			}
			return original;
		}else{
			if(!original.startsWith(prefix)){
				return original;
			}else{
				return original.replaceFirst(prefix, "");
			}
		}
		
	}
}
