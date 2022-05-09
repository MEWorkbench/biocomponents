package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLError;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.validator.SBMLValidator.CHECK_CATEGORY;
import org.sbml.jsbml.xml.parsers.MathMLStaxParser;
import org.sbml.jsbml.xml.parsers.SBMLCoreParser;
import org.xml.sax.SAXException;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.GeneCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.InvalidBooleanRuleException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionTypeEnum;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.interfaces.IContainerBuilder;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidationException;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidator;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.parser.ParseException;

public class JSBMLReader implements IContainerBuilder{

    static {
//    	Logger.getLogger(JSBML.class).setLevel(Level.OFF);
    	Logger.getLogger(SBMLCoreParser.class).setLevel(Level.ERROR);
		Logger.getLogger(MathMLStaxParser.class).setLevel(Level.OFF);
		Logger.getLogger(SBMLCoreParser.class).setLevel(Level.ERROR);
    }

	private static final long serialVersionUID = 1L;

	private Model jsbmlmodel;
	private String biomassId;
	private String organismName;
	
	protected HashMap<String, ReactionConstraintCI> defaultEC = null;
	protected HashMap<String, CompartmentCI> compartmentList = null;
	protected HashMap<String, MetaboliteCI> metaboliteList = null;
	protected HashMap<String, ReactionCI> reactionList = null;
	protected HashMap<String, GeneCI> genes = null;
	protected Map<String, Map<String, String>> metabolitesExtraInfo = new HashMap<String, Map<String,String>>();
	protected Map<String, Map<String, String>> reactionsExtraInfo = new HashMap<String, Map<String,String>>();
	protected HashMap<String, String> mapMetaboliteIdCompartment = null;
	protected SBMLDocument document;

	protected ArrayList<String> warnings;
	protected boolean checkConsistency;
	protected boolean isPalsson = false;
	protected boolean hasFormula = false;

	private Double defaultLB;
	private Double defaultUB;

	public JSBMLReader(String filePath, String organismName) throws FileNotFoundException, XMLStreamException, ErrorsException, IOException, ParserConfigurationException, SAXException, JSBMLValidationException{
		this(new FileInputStream(filePath), organismName, true, 0.0, 10000.0);
	}
	
	public JSBMLReader(String filePath, String organismName, boolean checkConsistency) throws FileNotFoundException, XMLStreamException, ErrorsException, IOException, ParserConfigurationException, SAXException, JSBMLValidationException{
		this(new FileInputStream(filePath), organismName, checkConsistency, 0.0, 10000.0);
	}
	
	public JSBMLReader(String filePath, String organismName, Double defLb, Double defUb) throws XMLStreamException, ErrorsException, IOException, ParserConfigurationException, SAXException, JSBMLValidationException {
		this(new FileInputStream(filePath), organismName, true, defLb, defUb);
	}
	
	public JSBMLReader(String filePath, String organismName, boolean checkConsistency, Double defLb, Double defUb) throws XMLStreamException, ErrorsException, IOException, ParserConfigurationException, SAXException, JSBMLValidationException {
		this(new FileInputStream(filePath), organismName, checkConsistency, defLb, defUb);
	}
	
	public JSBMLReader(InputStream data, String organismName, boolean checkConsistency) throws XMLStreamException, ErrorsException, IOException, ParserConfigurationException, SAXException, JSBMLValidationException {
		this(data, organismName, checkConsistency, 0.0, 10000.0);
	}
	
	public JSBMLReader(InputStream data, String organismName, boolean checkConsistency, Double defLb, Double defUb) throws XMLStreamException, ErrorsException, IOException, ParserConfigurationException, SAXException, JSBMLValidationException {
		
		// Clone InputStream
		// Necessary thus the JSBMLValidator close the stream after use it
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int len;
		while ((len = data.read(buffer)) > -1 )
		    baos.write(buffer, 0, len);
		baos.flush();

		InputStream is1 = new ByteArrayInputStream(baos.toByteArray()); 
		InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
		
		JSBMLValidator validator = new JSBMLValidator(is1);
		validator.enableAllValidators(true);
		validator.validate();
		
		this.checkConsistency = checkConsistency;
		SBMLReader reader = new SBMLReader();
		try {
			document = reader.readSBMLFromStream(is2);
		} catch (Exception e) {
			throw new IOException("The file is not a valid SBML! Please validate your file at SBML Validator (www.sbml.org/validator)");
		}
		
		this.getJSBMLModel();
		this.organismName = organismName;
		warnings = new ArrayList<String>();
		
		this.defaultLB = defLb;
		this.defaultUB = defUb;
		
		metabolitesExtraInfo = new HashMap<String, Map<String,String>>();
		reactionsExtraInfo = new HashMap<String, Map<String,String>>();
		populateInformation();
	}

	/**
	 * This method checks the consistency from the JSBML file and gets the JSBML model
	 * @throws ErrorsException ErrorsException
	 */
	private void getJSBMLModel() throws ErrorsException {

	    if(checkConsistency){
	    	document.setConsistencyChecks(CHECK_CATEGORY.GENERAL_CONSISTENCY, true);
	    	document.setConsistencyChecks(CHECK_CATEGORY.MODELING_PRACTICE, true);
	    	document.setConsistencyChecks(CHECK_CATEGORY.UNITS_CONSISTENCY, false);
	    	document.setConsistencyChecks(CHECK_CATEGORY.IDENTIFIER_CONSISTENCY, true);
	    	document.setConsistencyChecks(CHECK_CATEGORY.SBO_CONSISTENCY, false);
	    	document.setConsistencyChecks(CHECK_CATEGORY.OVERDETERMINED_MODEL, false);
	    	document.setConsistencyChecks(CHECK_CATEGORY.MATHML_CONSISTENCY, true);
	    }
	    
	    boolean hasErrors = false;
	    
	    for(int i=0; i<document.getListOfErrors().getNumErrors() && !hasErrors; i++){
	    	SBMLError e = document.getError(i);
	    	if(e.getSeverity().equals("Error")) hasErrors = true;
	    }
	    
	    if(hasErrors){
	    	throw new ErrorsException(document);
	    }
	    
	    Model model = document.getModel();
	    
	    System.out.println(document.getListOfErrors().getNumErrors());
	    this.jsbmlmodel = model;
	}
	
	/**
	 * @return The model name
	 */
	public String getModelName() {			
		return jsbmlmodel.getId();
	}

	/**
	 * @return The model notes
	 */
	public String getNotes() {
		try {
			return jsbmlmodel.getNotesString();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * @return The organism name
	 */
	public String getOrganismName() {
		return organismName;
	}
	
	/**
	 * @param organismName The organism name to be set
	 */
	public void setOrganismName(String organismName) {
		this.organismName = organismName;
	}

	/**
	 * @return The model version
	 */
	public Integer getVersion() {
		return jsbmlmodel.getVersion();
	}
	
	/**
	 * This method populates the structures with the information from the JSBML file
	 * @throws Exception Exception
	 */
	private void populateInformation(){

		compartmentList = new HashMap<String, CompartmentCI>();
		genes = new HashMap<String, GeneCI>();
		metaboliteList = new HashMap<String, MetaboliteCI>();
		defaultEC = new HashMap<String, ReactionConstraintCI>();
		reactionList = new HashMap<String, ReactionCI>();
		mapMetaboliteIdCompartment = new HashMap<String, String>();
		
		readCompartments();
		readMetabolites();
		readReactions();
	}

	/**
	 * This method verifies if the metaboliteID has the metabolite formula built-in
	 */
	private void verifyFormula(int index) {
		ListOf<Species> sbmlspecies = jsbmlmodel.getListOfSpecies();
//		for(int i=0; i<sbmlspecies.size(); i++){
		Species species = sbmlspecies.get(index);
		String name = species.getName();
		if(name.matches("^[Mm]_.*_[a-zA-Z0-9]*$")){	//if starts with M, the '_', then something, then '_' and finally the formula
			isPalsson = true;
			hasFormula = true;
		}
		else{
			String notes="";
			try {
				notes = species.getNotesString();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(notes != "" && notes.contains("FORMULA"))		//if the formula are at the species notes
				hasFormula = true;
		}
//		}
	}
	
	/**
	 * This method reads the compartments from the SBML file
	 */
	public void readCompartments(){
		ListOf<Compartment> sbmllistofcomps = jsbmlmodel.getListOfCompartments();
		for(int i =0;i<sbmllistofcomps.size();i++){
			Compartment comp = sbmllistofcomps.get(i);
			CompartmentCI ogcomp = new CompartmentCI(comp.getId(),comp.getName(),comp.getOutside());
			compartmentList.put(comp.getId(),ogcomp);
		}
	}
	
	/**
	 * This method reads the metabolites from the SBML file
	 */
	@SuppressWarnings("deprecation")
	public void readMetabolites(){
		ListOf<Species> sbmlspecies = jsbmlmodel.getListOfSpecies();
		for(int i =0;i<sbmlspecies.size();i++){
			isPalsson = false;
			hasFormula = false;
			verifyFormula(i);
			
			Species species = sbmlspecies.get(i);
			String idInModel = species.getId();
			String nameInModel = species.getName();
			String compartmentId = species.getCompartment();
			
			compartmentList.get(compartmentId).addMetaboliteInCompartment(idInModel);
			
			mapMetaboliteIdCompartment.put(idInModel, compartmentId);
			MetaboliteCI ogspecies = new MetaboliteCI(idInModel, nameInModel);
			
			Integer charge = species.getCharge();

			String formula = null;
			String notes = "";
			try {
				notes = species.getNotesString();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(isPalsson){	//formula is in species name
				int index = nameInModel.lastIndexOf("_");
				formula = nameInModel.substring(index+1);
			}
			
			if(notes != "" && formula ==null){
				Pattern pattern = Pattern.compile("(<html:p>|<p>)FORMULA:(.*?)(</html:p>|</p>)");
				Matcher matcher = pattern.matcher(notes);

				if(matcher.find())
					formula = matcher.group(2).trim();
				
				
//				System.out.println(formula);
			}
			
			if(charge == 0){
				Integer chargeNotes = getChargeByNotes(notes);
				if(chargeNotes!=null)
					charge = chargeNotes;
			}
			
			getGenericInfo(idInModel, notes, metabolitesExtraInfo);
			ogspecies.setCharge(charge);
			
//			formula = metabolitesExtraInfo.get("FORMULA").get(idInModel);
			if(formula != null)
				ogspecies.setFormula(formula);
			
			metaboliteList.put(idInModel, ogspecies);
		}
	}
	
	private Integer getChargeByNotes(String notes){
		
		Integer charge = null;
		if(notes != ""){
			
			Pattern pattern = Pattern.compile("(<html:p>|<p>)CHARGE:(.*?)(</html:p>|</p>)");
			Matcher matcher = pattern.matcher(notes);

			if(matcher.find()){
				String chargeS = matcher.group(2).trim();
				try {
					charge = Integer.valueOf(chargeS);
				} catch (Exception e) {
				}
				
			}
		}
		
		return charge;
	}
	
	private void getGenericInfo(String id, String notes, Map<String, Map<String, String>> info){
		
		if(notes != ""){
			
			Pattern pattern = Pattern.compile("(<html:p>|<p>)(.*?):(.*?)(</html:p>|</p>)");
			Matcher matcher = pattern.matcher(notes);

			while(matcher.find()){
				String extraInfoId = matcher.group(2).trim();
				String infoS = matcher.group(3).trim();
				Map<String, String> map = info.get(extraInfoId);
				
				if(map==null){
					map = new HashMap<String, String>();
				}
				
//				System.out.println(id + "\t"+ extraInfoId + "\t" + infoS);
				map.put(id, infoS);
				info.put(extraInfoId, map);
			}
		}
		
//		return charge;
	}
	
	/**
	 * This method reads the reactions from the SBML file
	 */
	public void readReactions(){
		Set<String> speciesInReactions = new TreeSet<String>();
		long maxMetabInReaction = 0;
		ListOf<Reaction> sbmlreactions = jsbmlmodel.getListOfReactions();
		for(int i =0;i<sbmlreactions.size();i++){
			
			Reaction sbmlreaction = sbmlreactions.get(i);
			String reactionId = sbmlreaction.getId();
			
			ListOf<SpeciesReference> products = sbmlreaction.getListOfProducts();
			ListOf<SpeciesReference> reactants = sbmlreaction.getListOfReactants();
			
			/** add mappings for products */
			Map<String, StoichiometryValueCI> productsCI = addMapping(products, reactionId, speciesInReactions);
			
			/** add mappings for reactants */
			Map<String, StoichiometryValueCI> reactantsCI = addMapping(reactants, reactionId, speciesInReactions);
			
			boolean isReversible = sbmlreaction.getReversible();
			
			maxMetabInReaction = kinetic(sbmlreaction, isReversible, reactantsCI, productsCI, maxMetabInReaction);
			
			ReactionCI ogreaction = new ReactionCI(sbmlreaction.getId(), sbmlreaction.getName(),
					isReversible,reactantsCI,productsCI);
			
			
			ogreaction.setType(ReactionTypeEnum.Internal);
			
			
			String notes="";

			
			try {
				notes = sbmlreaction.getNotesString();
				parserNotes(notes, ogreaction);
			} catch (InvalidBooleanRuleException e) {
				warnings.add("Problem in reaction "+ ogreaction.getId() + ": " + e.getMessage());
			} catch (XMLStreamException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			
			reactionList.put(reactionId,ogreaction);
			
		}

		reactionList.get(biomassId).setType(ReactionTypeEnum.Biomass);
		removeSpeciesNonAssociatedToReactions(speciesInReactions);
	}
	
	/**
	 * This method adds mapping to reactants and products
	 * @param list List of reactants or products
	 * @param reactionId The reaction ID
	 * @param speciesInReactions A set with all the metabolites that participate in some reaction
	 * @return The mapping of reactants or products
	 */
	public Map<String, StoichiometryValueCI> addMapping(ListOf<SpeciesReference> list, String reactionId, Set<String> speciesInReactions){
		Map<String, StoichiometryValueCI> result = new HashMap<String, StoichiometryValueCI>();
		for(int l = 0;l<list.size();l++){
			SpeciesReference ref = (SpeciesReference)list.get(l);
			String idInModel = ref.getSpecies();
				
			result.put(idInModel,new StoichiometryValueCI(idInModel,ref.getStoichiometry(), mapMetaboliteIdCompartment.get(idInModel)));
//TODO: verificar se o metabolito existe
			//			metaboliteList.get(idInModel).addReaction(reactionId);
			speciesInReactions.add(idInModel);
		}
		
		return result;
	}
	
	/*
	 * This method handles with the kinetic law of the reaction, if it exists
	 * @param sbmlreaction The reaction
	 * @param isReversible The reaction reversibility
	 * @param reactantsCI The reactants
	 * @param productsCI The products
	 */
	public long kinetic(Reaction sbmlreaction, boolean isReversible, Map<String, StoichiometryValueCI> reactantsCI, Map<String, StoichiometryValueCI> productsCI, long maxMetabInReaction){
		KineticLaw kineticlaw = sbmlreaction.getKineticLaw();
		
		double lower = defaultLB;
		double upper = defaultUB;
		
		boolean haskinetic = false;
		if(kineticlaw!=null){
			ListOf<LocalParameter> params = kineticlaw.getListOfLocalParameters();// getListOfParameters();
								
			if(params!=null && params.size()>0){
				for(int j = 0; j< params.size();j++){
					LocalParameter p = params.get(j);
					if(p.getId().equalsIgnoreCase("LOWER_BOUND")){
						lower = p.getValue();
						haskinetic = true;
					}else if(p.getId().equalsIgnoreCase("UPPER_BOUND")){
						upper = p.getValue();
						haskinetic = true;
					}
				}
			}
		}
		
		if(!isReversible && !haskinetic){
			lower = 0.0;
		}
		
		long nMetabolitesInReaction = reactantsCI.size()+productsCI.size();
		if(nMetabolitesInReaction>maxMetabInReaction){
			maxMetabInReaction = nMetabolitesInReaction;
			biomassId = sbmlreaction.getId();
		}
			
		if(haskinetic)
			defaultEC.put(sbmlreaction.getId(), new ReactionConstraintCI(lower,upper));
		
		return maxMetabInReaction;
	}

	/**
	 * This method removes the species that aren't associated with any reaction
	 * @param speciesInReactions A set with all the species that are associated to one ore more reactions
	 */
	private void removeSpeciesNonAssociatedToReactions(
			Set<String> speciesInReactions) {
		
		List<String> toRemove = new ArrayList<String>();
		
		for(String metId : metaboliteList.keySet())
			if(!speciesInReactions.contains(metId))
				toRemove.add(metId);
		
		
		for(String metId : toRemove){
			metaboliteList.remove(metId);
		}
	}

	/**
	 * This method returns the external compartment
	 * @return A String (the external compartment)
	 */
	public String getExternalCompartment() {
		String toReturn = "";
		
		for(String compId : compartmentList.keySet()){
			
			CompartmentCI comp = compartmentList.get(compId);
			if(comp.getOutside() == null || comp.getOutside().equals("")){
				toReturn = compId;
				break;
			}
		}
		
		return toReturn;
	}

	/**
	 * This method parses the notes
	 * @param notes A String with the notes
	 * @param ogreaction A ReactionCI object 
	 * @throws InvalidBooleanRuleException InvalidBooleanRuleException

	 */
	private void parserNotes(String notes, ReactionCI ogreaction) throws InvalidBooleanRuleException{
		
		getGenericInfo(ogreaction.getId(), notes, reactionsExtraInfo);
		ogreaction.setEc_number(getProteinClass(notes, reactionsExtraInfo));
		
		ogreaction.setGeneRule(getGeneRule(notes, reactionsExtraInfo));
		ogreaction.setSubsystem(getSubstystem(notes, reactionsExtraInfo));
		
		
		
		for(String geneId : ogreaction.getGenesIDs()){
			
			if(!genes.containsKey(geneId)){
				genes.put(geneId, new GeneCI(geneId, null));
			}
		}
		
	}
	
	/**
	 * This method gets the gene rule from a String
	 * @param notes The String with the gene rule
	 * @param reactionsExtraInfo2 
	 * @return A String with the gene rule parsed
	 */
	private String getGeneRule(String notes, Map<String, Map<String, String>> reactionsExtraInfo2){
		
		Pattern pattern = Pattern.compile("(<html:p>|<p>)(GENE[ _]ASSOCIATION):(.*?)(</html:p>|</p>)");
		Matcher matcher = pattern.matcher(notes);

		String geneReactionAssociation = null;
		if(matcher.find()){
			String extra =  matcher.group(2).trim();
			reactionsExtraInfo2.remove(extra);
			geneReactionAssociation = matcher.group(3).trim();
		}

		//Strange characters found in some models
		if(geneReactionAssociation!= null){
//			System.out.println("Gene Rule: ." + geneReactionAssociation+".");
			geneReactionAssociation = geneReactionAssociation.trim();
			geneReactionAssociation = geneReactionAssociation.replaceAll("\u00A0"," ");
			geneReactionAssociation = geneReactionAssociation.replaceAll("^(\\d+)$","g$1");
			try {
				geneReactionAssociation = geneReactionAssociation.replaceAll("([ (])(\\d+)([ )])","$1g$2$3");
			} catch (Exception e) {
			}
			
//			System.out.println("Gene Rule: " + geneReactionAssociation);
//			System.out.println();
		}
		return geneReactionAssociation;
	}
	
	/**
	 * This method gets the protein rule from a String
	 * @param notes The String with the protein rule
	 * @return A String with the protein rule parsed
	 */
	@SuppressWarnings("unused")
	private String getProteinRule(String notes){
		
		Pattern pattern = Pattern.compile("(<html:p>|<p>)(PROTEIN[ _]ASSOCIATION):(.*?)(</html:p>|</p>)");
		Matcher matcher = pattern.matcher(notes);

		String proteinReactionAssociation = null;
		if(matcher.find()){
//			String extra =  matcher.group(2).trim();
//			reactionsExtraInfo2.remove(extra);
			
			proteinReactionAssociation = matcher.group(3).trim();
		}
		
		return (proteinReactionAssociation);
	}
	
	/**
	 * This method gets the protein class from a String
	 * @param notes The String with the protein class
	 * @param reactionsExtraInfo2 
	 * @return A String with the protein class parsed
	 */
	private String getProteinClass(String notes, Map<String, Map<String, String>> reactionsExtraInfo2){
		
		Pattern pattern = Pattern.compile("(<html:p>|<p>)(PROTEIN[ _]CLASS|EC[ _]Number):(.*?)(</html:p>|</p>)");
		Matcher matcher = pattern.matcher(notes);

		String proteinClass = null;
		if(matcher.find()){
			proteinClass = matcher.group(3).trim();
			
			String extra =  matcher.group(2).trim();
			reactionsExtraInfo2.remove(extra);
		}

		return proteinClass;
	}
	
	/**
	 * This method gets the reaction subsystem from a String
	 * @param notes The String with the reaction subsystem
	 * @param reactionsExtraInfo2 
	 * @return A String with the reaction subsystem parsed
	 */
	private String getSubstystem(String notes, Map<String, Map<String, String>> reactionsExtraInfo2){
		Pattern pattern = Pattern.compile("(<html:p>|<p>)(SUBSYSTEM):(.*?)(</html:p>|</p>)");
		Matcher matcher = pattern.matcher(notes);

		String subsytem = null;
		if(matcher.find()){
			
			subsytem = matcher.group(3).trim();
			String extra =  matcher.group(2).trim();
			reactionsExtraInfo2.remove(extra);
		}
		return subsytem;
	}
	
	/**
	 * @return The biomass ID
	 */
	public String getBiomassId() {
		return biomassId;
	}

	/**
	 * @return A map with the environmental conditions
	 */
	@Override
	public HashMap<String, ReactionConstraintCI> getDefaultEC() {
		return defaultEC;
	}

	/**
	 * @return A map with the compartments
	 */
	@Override
	public HashMap<String, CompartmentCI> getCompartments() {
		return compartmentList;
	}

	/**
	 * @return A map with the metabolites
	 */
	@Override
	public HashMap<String, MetaboliteCI> getMetabolites() {
		return metaboliteList;
	}

	/**
	 * @return A map with the reactions
	 */
	@Override
	public HashMap<String, ReactionCI> getReactions() {
		return reactionList;
	}

	/**
	 * @return A map with the genes
	 */
	@Override
	public Map<String, GeneCI> getGenes() {
		return genes;
	}


	public HashMap<String, String> getMetaboliteIdToSpecieTermId() {
		return null;
	}

	/**
	 * @return The external compartment ID
	 */
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
	
	/**
	 * @return A map with the metabolites extra info
	 */
	@Override
	public Map<String, Map<String, String>> getMetabolitesExtraInfo() {
		return metabolitesExtraInfo;
	}

	/**
	 * @return A map with the reactions extra info
	 */
	@Override
	public Map<String, Map<String, String>> getReactionsExtraInfo() {
		return reactionsExtraInfo;
	}

	public boolean hasWarnings() {
		return !warnings.isEmpty();
	}

	public List<String> getWarnings() {
		return warnings;
	}
}
