package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.InvalidBooleanRuleException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.interfaces.IContainerBuilder;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public class JSONReader implements IContainerBuilder {
	
	private static final long serialVersionUID = 1L;
	
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
	
	public static final String DEFAULT_COMPARTMENT = "cytosol";
	public static final String DEFAULT_EXTERNAL_COMPARTMENT = "external";
	
	
	private InputStream stream;
	private boolean closeStream;
	
	protected String modelId;
	protected int modelVersion;
	protected String biomassId;
	protected String organismName;
	
	protected boolean addPrefix = false;
	
	protected HashMap<String, ReactionConstraintCI> defaultEC = null;
	protected HashMap<String, CompartmentCI> compartmentList = null;
	protected HashMap<String, MetaboliteCI> metaboliteList = null;
	protected HashMap<String, ReactionCI> reactionList = null;
	protected HashMap<String, GeneCI> genes = null;
	protected HashMap<String, String> mapMetaboliteIdCompartment = null;
	
	protected Map<String, Map<String, String>> metabolitesExtraInfo = new HashMap<String, Map<String,String>>();
	protected Map<String, Map<String, String>> reactionsExtraInfo = new HashMap<String, Map<String,String>>();
	
	protected String notes;
	
	
	public JSONReader(InputStream stream, String organismName, boolean addPrefix) throws JsonProcessingException, IOException, InvalidBooleanRuleException{
		this.organismName = organismName;
		this.addPrefix = addPrefix;
		
		this.stream = stream;
		
		buildModel();
		
		this.closeStream = false;
	}
	
	public JSONReader(InputStream stream, String organismName) throws JsonProcessingException, IOException, InvalidBooleanRuleException{
		this(stream, organismName, false);
	}
	
	public JSONReader(String filePath, String organismName, boolean addPrefix) throws JsonProcessingException, IOException, InvalidBooleanRuleException{
		this(new File(filePath), organismName, addPrefix);
	}
	
	public JSONReader(String filePath, String organismName) throws JsonProcessingException, IOException, InvalidBooleanRuleException{
		this(new File(filePath), organismName);
	}
	
	public JSONReader(File filePath, String organismName) throws JsonProcessingException, IOException, InvalidBooleanRuleException{
		this(new BufferedInputStream(new FileInputStream(filePath)), organismName);
	}
	
	public JSONReader(File filePath, String organismName, boolean addPrefix) throws JsonProcessingException, IOException, InvalidBooleanRuleException{
		this(new BufferedInputStream(new FileInputStream(filePath)), organismName, addPrefix);
	}
	
	protected void buildModel() throws JsonProcessingException, IOException, InvalidBooleanRuleException{
		initModel();
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode map = mapper.readTree(stream);
		
		parseCompartments(map.get(FIELD_COMPARTMENTS));
		
		parseMetabolites(map.get(FIELD_METABOLITES));
		
		parseReactions(map.get(FIELD_REACTIONS));
		
		parseGenes(map.get(FIELD_GENES));
		
		parseVersion(map.get(FIELD_VERSION));
		
		parseId(map.get(FIELD_ID));
		
		if(closeStream) this.stream.close();
	}
	
	protected void initModel(){
		compartmentList = new HashMap<String, CompartmentCI>();
		genes = new HashMap<String, GeneCI>();
		metaboliteList = new HashMap<String, MetaboliteCI>();
		defaultEC = new HashMap<String, ReactionConstraintCI>();
		reactionList = new HashMap<String, ReactionCI>();
		
		mapMetaboliteIdCompartment = new HashMap<String, String>();
		
		metabolitesExtraInfo = new HashMap<String, Map<String,String>>();
		reactionsExtraInfo = new HashMap<String, Map<String,String>>();
		
		this.notes = "";
	}
	
	protected void parseReactions(JsonNode reactions) throws InvalidBooleanRuleException {
		int numReactions = reactions.size();
		
		for (int i = 0; i < numReactions; i++) {
			JsonNode singleReaction = reactions.get(i);
			ReactionCI reactionCI = handleSingleReaction(singleReaction);
			reactionList.put(reactionCI.getId(), reactionCI);
		}
	}
	
	protected ReactionCI handleSingleReaction(JsonNode singleReaction) throws InvalidBooleanRuleException{
		
		String id = singleReaction.get(FIELD_ID).textValue();
		
		String subsystem = null;
		if(singleReaction.has(FIELD_SUBSYSTEM)){
			subsystem = singleReaction.get(FIELD_SUBSYSTEM).textValue();
		}
		if(singleReaction.has(FIELD_OBJ_COEF)){
			biomassId = convertWithPrefix(id, REAC_PREFIX);
		}
		
		String name = singleReaction.get(FIELD_NAME).textValue();
		Double ub = singleReaction.get(FIELD_UPPER_BOUND).asDouble();
		Double lb = singleReaction.get(FIELD_LOWER_BOUND).asDouble();
		
		if(singleReaction.has(FIELD_NOTES)){
			parseNotesByReactionId(singleReaction.get(FIELD_NOTES), id);
		}
		
		String geneRule = singleReaction.get(FIELD_GPR).asText();
		Map<String, Double> stoichMap = parseMetabolitesByReactionId(singleReaction.get(FIELD_METABOLITES), id);
		
		Map<String, StoichiometryValueCI> reactants = filterMetabolites(stoichMap, true);
		Map<String, StoichiometryValueCI> products = filterMetabolites(stoichMap, false);
		
		boolean reversible = (lb<0.0 && ub>0.0)? true : false; 
		
		ReactionCI toRet = new ReactionCI(convertWithPrefix(id, REAC_PREFIX), name, reversible, reactants, products);
		
		toRet.setSubsystem(subsystem);
		toRet.setGeneRule(geneRule);
		
		defaultEC.put(toRet.getId(), new ReactionConstraintCI(lb, ub));
		
		return toRet;
	}
	
	protected Map<String, StoichiometryValueCI> filterMetabolites(Map<String, Double> stoichMap, boolean byReactants){
		Map<String, StoichiometryValueCI> toRet = new HashMap<>();
		
		for (String metaboliteId : stoichMap.keySet()) {
			Double stoichValue = stoichMap.get(metaboliteId);
			boolean isReactant = (stoichValue < 0.0);
			
			if((isReactant && byReactants) || (!isReactant && !byReactants)){
				String containerMetaboliteId = convertWithPrefix(metaboliteId, META_PREFIX); 
				toRet.put(containerMetaboliteId, new StoichiometryValueCI(containerMetaboliteId, Math.abs(stoichValue.doubleValue()), mapMetaboliteIdCompartment.get(containerMetaboliteId)));
			}
		}
		
		return toRet;
	}
	
	protected void parseNotesByReactionId(JsonNode notesNode, String reactionId){
		handleNotes(notesNode, reactionId, reactionsExtraInfo);
	}
	
	protected Map<String, Double> parseMetabolitesByReactionId(JsonNode metabolitesNode, String reactionId) {
		
		Map<String, Double> toRet = new HashMap<>();
		
		Iterator<Entry<String, JsonNode>> it = metabolitesNode.fields();
		
		while (it.hasNext()) {
			Entry<String, JsonNode> entry = it.next();
			String metaboliteId = entry.getKey(); 
			Double stoich = entry.getValue().asDouble();
			toRet.put(convertWithPrefix(metaboliteId, META_PREFIX), stoich);
		}
		
		return toRet;
	}
	
	protected void parseGenes(JsonNode genesNode) {
		if(genesNode!= null){
			int numGenes = genesNode.size();
		
			for (int i = 0; i < numGenes; i++) {
				JsonNode singleGene = genesNode.get(i);
				GeneCI gene = handleSingleGene(singleGene);
				genes.put(gene.getGeneId(), gene);
			}
		}
	}
	
	protected GeneCI handleSingleGene(JsonNode singleGene) {
		String id = singleGene.get(FIELD_ID).asText();
		String name = singleGene.get(FIELD_NAME).asText();
		
//		parseGeneNotes(singleGene);
		
		GeneCI toRet = new GeneCI(id, name);
		
		return toRet;
	}

	protected void parseCompartments(JsonNode compartments) {
		
		Iterator<Entry<String, JsonNode>> it = compartments.fields();
		while (it.hasNext()) {
			Entry<String, JsonNode> singleCompartment = it.next();
			CompartmentCI compartment = handleSingleCompartment(singleCompartment);
			compartmentList.put(compartment.getId(), compartment);
		}
	}
	
	protected CompartmentCI handleSingleCompartment(Entry<String, JsonNode> singleCompartment) {
		
		String compartmentId = singleCompartment.getKey();
		String compartmentName = singleCompartment.getValue().textValue();
		
		String external = (compartmentId.equals(DEFAULT_COMPARTMENT) ? DEFAULT_EXTERNAL_COMPARTMENT : null);
		CompartmentCI toRet = new CompartmentCI(compartmentId, compartmentName, external);
		
		return toRet;
	}
	
	protected void parseMetabolites(JsonNode metabolites) {
		int numMetabolites = metabolites.size();
		
		for (int i = 0; i < numMetabolites; i++) {
			JsonNode singleMetabolites = metabolites.get(i);
			MetaboliteCI metaboliteCI = handleSingleMetabolite(singleMetabolites);
			metaboliteList.put(metaboliteCI.getId(), metaboliteCI);
		}	
	}
	
	protected MetaboliteCI handleSingleMetabolite(JsonNode singleMetabolite) {
		
		String id = singleMetabolite.get(FIELD_ID).textValue();
		String name = singleMetabolite.get(FIELD_NAME).textValue();
		
		Integer charge = null;
		if(singleMetabolite.has(FIELD_CHARGE)){
			charge = singleMetabolite.get(FIELD_CHARGE).asInt();
		}
		
		String formula = null;
		if(singleMetabolite.has(FIELD_FORMULA)){
			formula = singleMetabolite.get(FIELD_FORMULA).textValue();
		}
		
		String compartmentId = singleMetabolite.get(FIELD_COMPARTMENT).textValue();
		
		if(singleMetabolite.has(FIELD_NOTES)){
			parseNotesByMetaboliteId(singleMetabolite.get(FIELD_NOTES), id);
		}
		
		String idInContainer = convertWithPrefix(id, META_PREFIX);
		
		MetaboliteCI toRet = new MetaboliteCI(idInContainer, name);
		
		compartmentList.get(compartmentId).addMetaboliteInCompartment(idInContainer);
		mapMetaboliteIdCompartment.put(idInContainer, compartmentId);
		
		if(formula != null){
			toRet.setFormula(formula);
		}
		if(charge != null){
			toRet.setCharge(charge);
		}
		
		return toRet;
	}
	
	protected void parseNotesByMetaboliteId(JsonNode notesNode, String metaboliteId){
		
		handleNotes(notesNode, metaboliteId, metabolitesExtraInfo);
		
	}
	
	protected void handleNotes(JsonNode notesNode, String elementId, Map<String, Map<String, String>> extraInfoMap){
		Iterator<Entry<String, JsonNode>> it = notesNode.fields();
		while (it.hasNext()) {
			Entry<String, JsonNode> singleNote = it.next();
			
			String noteKey = singleNote.getKey();
			JsonNode noteValue = singleNote.getValue();
			
			Map<String, String> metabByKey = extraInfoMap.get(noteKey);
			
			if(metabByKey == null){
				extraInfoMap.put(noteKey, new HashMap<String, String>());
			}
			
			String notesToAdd = "";
			if(noteValue.isArray()){
				List<String> list = new ArrayList<String>();
				for (int i = 0; i < noteValue.size(); i++) {
					list.add(noteValue.get(i).asText());
				}
				notesToAdd = CollectionUtils.join(list, NOTES_SPLIT_CHAR);
			}else{
				notesToAdd = noteValue.asText();
			}
			
			extraInfoMap.get(noteKey).put(elementId, notesToAdd);
		}
	}
	
	protected void parseVersion(JsonNode version) {
		modelVersion = version.intValue();
		
	}
	
	protected void parseId(JsonNode id) {
		modelId = id.textValue();
	}

	@Override
	public String getModelName() {
		return modelId;
	}

	public String getOrganismName() {
		return organismName;
	}
	
	/**
	 * @param organismName The organism name to be set
	 */
	public void setOrganismName(String organismName) {
		this.organismName = organismName;
	}

	@Override
	public String getNotes() {
		return notes;
	}

	@Override
	public Integer getVersion() {
		return modelVersion;
	}

	@Override
	public Map<String, CompartmentCI> getCompartments() {
		return compartmentList;
	}

	@Override
	public Map<String, ReactionCI> getReactions() {
		return reactionList;
	}

	@Override
	public Map<String, MetaboliteCI> getMetabolites() {
		return metaboliteList;
	}

	@Override
	public Map<String, GeneCI> getGenes() {
		return genes;
	}

	@Override
	public Map<String, Map<String, String>> getMetabolitesExtraInfo() {
		return metabolitesExtraInfo;
	}

	@Override
	public Map<String, Map<String, String>> getReactionsExtraInfo() {
		return reactionsExtraInfo;
	}

	@Override
	public String getBiomassId() {
		return biomassId;
	}

	@Override
	public Map<String, ReactionConstraintCI> getDefaultEC() {
		return defaultEC;
	}

	@Override
	public String getExternalCompartmentId() {
		String ret = null;
		String altrn = null;
		for(CompartmentCI comp : compartmentList.values()){
			if(comp.getOutside()==null || comp.getOutside().equals("")){
				ret = comp.getId();
				break;
			}
			
			if(comp.getId().startsWith("e")||comp.getId().startsWith("E"))
				altrn = comp.getId();
		}
		
		if(ret == null)
			ret = altrn;
		return ret;
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
