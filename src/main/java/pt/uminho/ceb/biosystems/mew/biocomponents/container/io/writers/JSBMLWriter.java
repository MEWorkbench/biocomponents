/*
 * Copyright 2010
 * IBB-CEB - Institute for Biotechnology and Bioengineering - Centre of Biological Engineering
 * CCTC - Computer Science and Technology Center
 *
 * University of Minho 
 * 
 * This is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This code is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Public License for more details. 
 * 
 * You should have received a copy of the GNU Public License 
 * along with this code. If not, see http://www.gnu.org/licenses/ 
 * 
 * Created inside the SysBioPseg Research Group (http://sysbio.di.uminho.pt)
 */
package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.writers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.Unit.Kind;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbml.jsbml.xml.XMLNode;
import org.sbml.jsbml.xml.XMLTriple;
import org.sbml.jsbml.xml.parsers.MathMLStaxParser;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.ContainerUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionTypeEnum;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.exceptions.JSBMLWriterException;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
/**
 * A writer for SBML Files 
 * 
 * @author pmaia
 */
public class JSBMLWriter{
	
	
	private static final String GENE_RULE_PREFIX = "GENE_ASSOCIATION: ";
	private static final String PROTEIN_RULE_PREFIX = "PROTEIN_ASSOCIATION: ";
	private static final String PROTEIN_CLASS_PREFIX = "PROTEIN_CLASS: ";
	private static final String SUBSYSTEM_PREFIX = "SUBSYSTEM: ";
	private static final String FOMULA_PREFIX = "FORMULA: ";
	private static final String CHARGE_PREFIX = "CHARGE: ";

	private String path;
	private Container container;
	private boolean palssonSpecific = false;
	private boolean writeMath = false;
	private boolean ignoreNotesField = false;
	private boolean ignoreCellDesignerAnnotations = false;
	private boolean writeUnits = true;
	private boolean needsToBeStandardized = false;
	private Map<String, ReactionConstraintCI> overrideConstraints;
	
	private SBMLLevelVersion levelAndVersion = SBMLLevelVersion.L2V1;
	private ArrayList<String> ignoredNamespaces = new ArrayList<String>();
	private HashMap<String, MetaboliteCI> new_metabolites_names_r = new HashMap<String, MetaboliteCI>();
	private HashMap<String, MetaboliteCI> new_metabolites_names_p = new HashMap<String, MetaboliteCI>();
	private Set<String> extraMetabolitesInfo;
	private Set<String> extraReactionsInfo;
	
	
	
	public static String CELLDESIGNER_NAMESPACE_PREFIX = "celldesigner";
	public static String CELLDESIGNER_NAMESPACE_URI = "http://www.sbml.org/2001/ns/celldesigner";

//	private XMLNamespaces spaces;

	/**
	 *  Having this constructor is very dangerous
	 *  since the global variables are not initialized
	 */
	@Deprecated
	public JSBMLWriter(){}

	public JSBMLWriter(String path,Container container) {
		this.overrideConstraints = new HashMap<String, ReactionConstraintCI>();
		this.path = path;
		this.container = container;
//		System.out.println("\n\n\n\n");
//		System.out.println(!container.hasUnicIds());
//		System.out.println(container.getMetabolites().keySet());
		if(!container.hasUnicIds()){
			this.container = container.clone();
			this.container.useUniqueIds();
		}
		
		
		
		extraMetabolitesInfo = container.getMetabolitesExtraInfo().keySet();
		extraReactionsInfo = container.getReactionsExtraInfo().keySet();
	}

	public JSBMLWriter(String path,Container container, boolean palssonSpecific, boolean writeUnits){
		this(path,container);
		this.palssonSpecific = palssonSpecific;
		this.writeUnits = writeUnits;
	}

	public JSBMLWriter(String path,Container container, boolean palssonSpecific,SBMLLevelVersion lv, boolean writeUnits){
		this(path,container,palssonSpecific, writeUnits);
		this.levelAndVersion = lv;
	}

	public JSBMLWriter(String path,Container container, boolean palssonSpecific,boolean writeMath,SBMLLevelVersion lv, boolean writeUnits){
		this(path,container,palssonSpecific, writeUnits);
		this.writeMath = writeMath;
		this.levelAndVersion = lv;
	}

	/**
	 * This method writes the container into a SBML file
	 * @throws Exception
	 */
	public void writeToFile() throws Exception {
		if(isPalssonSpecific())
			constructMetabolitesToInsert();
		try {
            System.out.println("Writing to file: " + path);
			toSBML(path);
		} catch (SBMLException e) {
			e.printStackTrace();
			throw new Exception(e);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	
	}
	
	public void setOverrideConstraints(
			Map<String, ReactionConstraintCI> overrideConstraints) {
		this.overrideConstraints = overrideConstraints;
	}

	/**
	 * This method constructs the metabolites to insert
	 */
	private void constructMetabolitesToInsert(){
		
		Set<String> drains = container.getDrains();
		
		Map<String, MetaboliteCI> metabolites = container.getMetabolites();
		
		for(String reaction : drains){
			Map<String, StoichiometryValueCI> reactants = container.getReaction(reaction).getReactants();
			Map<String, StoichiometryValueCI> products = container.getReaction(reaction).getProducts();
			
			if(reactants.size() > 0){
				generalStandard(reactants, metabolites, reaction);
			}
			else if(products.size() > 0){
				generalStandard(products, metabolites, reaction);
			}
		}
	}
	
	/**
	 * This method receives a map with some metabolites, and updates them with a new standardized name
	 * @param map The structure with the to update metabolites
	 * @param metabolites A structure with all the metabolites
	 * @param reaction The reaction ID
	 */
	private void generalStandard(Map<String, StoichiometryValueCI> map, Map<String, MetaboliteCI> metabolites, String reaction){
		MetaboliteCI new_metabolite = null;
		String new_metabolite_name;
		for (String name : map.keySet()){				//it will be just 1 reactant
			new_metabolite_name = name;
			if(underComp(name)) new_metabolite_name = name.replaceAll("_(" + CollectionUtils.join(container.getCompartments().keySet(), "|")+")_$", "");					//-2 to delete the "_e"
			if(needsToBeStandardized) new_metabolite_name = standardizeMetId(new_metabolite_name);
			new_metabolite_name += "_b";
			new_metabolite = metabolites.get(name).clone();
			new_metabolite.setId(new_metabolite_name);
			new_metabolites_names_p.put(reaction, new_metabolite);
		}
	}

	/**
	 * This method checks if a metaboliteID has a compartment and a '_' in its name
	 * @param name The metaboliteID
	 * @return
	 */
	private boolean underComp(String name) {
		String reg_exp = ".*(" + CollectionUtils.join(container.getCompartments().keySet(), "|")+")_$";
		if(name.matches(reg_exp)) return true;
		return false;
	}

	
	protected Model createModel() throws Exception{
		

		System.out.println(container.getModelName());
		Model model = new Model(container.getModelName(), levelAndVersion.getLevel(), levelAndVersion.getVersion());
		
		ListOf<UnitDefinition> list_unitDef = new ListOf<UnitDefinition>(levelAndVersion.getLevel(), levelAndVersion.getVersion());
		ListOf<Unit> list_unit = new ListOf<Unit>(levelAndVersion.getLevel(), levelAndVersion.getVersion());
		UnitDefinition unit_def = null;
		
		/**write unit definitions*/
		if(writeUnits)
			writeUnitDefinitions(list_unitDef, list_unit, unit_def, model);

		
		/** load all the compartments */
		loadCompartments(model);


		/** load all species */		
		loadSpecies(model);

		
		/**  load the drains and define new metabolites for each one of them  */
		
		if(isPalssonSpecific())
			loadDrains(model);

		/** load all the reactions */
		loadReactions(model, unit_def);
		
		return model;
	}
	
	
	protected void saveToFile(Model model, String outputFile) throws FileNotFoundException, SBMLException, XMLStreamException{
		SBMLDocument document = new SBMLDocument(levelAndVersion.getLevel(), levelAndVersion.getVersion());
		Logger.getLogger(SBMLDocument.class).setLevel(Level.OFF);
		Logger.getLogger(MathMLStaxParser.class).setLevel(Level.OFF);
//		spaces = new XMLNamespaces();
//		spaces.add("http://www.w3.org/1999/xhtml", "html");
//		spaces.add("http://www.sbml.org/sbml/level2");
//		spaces.add("http://www.w3.org/1998/Math/MathML");
////		document.addDeclaredNamespace(prefix, namespace);
		document.addNamespace("html", "xmlns", "http://www.w3.org/1999/xhtml");
		
		document.setModel(model);
		writeNotes(document, model);
		SBMLWriter writer = new SBMLWriter();
		OutputStream out = new FileOutputStream(outputFile);
		writer.write(document, out);
	}
	
	/**
	 * <p>This method converts the <code>InformationContainer</code> to the SBML</p>
	 * <p>native format and returns it as an SBML <code>String</code></p> 
	 * 
	 * @return <code>String</code> representation of the SBML model.
	 * @throws Exception 
	 */
	public void toSBML(String outputFile) throws Exception{
		Model model = createModel();
		saveToFile(model, outputFile);
		
	}
	
	/**
	 * This method writes the notes
	 * @param document The SBMLDocument object
	 * @param model The model
	 */
	public void writeNotes(SBMLDocument document, Model model){
		
//		System.out.println(container.getNotes());
//		standardizeMetId(idcontainer)
//		if(container.getNotes() != null) document.setNotes(container.getNotes());
//		document.setn
//		document.addDeclaredNamespace("html", "html");
		String id = container.getModelName(); 
		String name = container.getModelName();
		if(id==null)
			id="ID";
		if(name==null)
			name="Exported by metabolic package - OptFlux Project";
		model.setId(id);
		model.setName(name);
	}

	/**
	 * This method writes the unit definitions
	 * @param list_unitDef A list of unit definitios
	 * @param list_unit A list of units
	 * @param unit_def A UnitDefition object
	 * @param model The model
	 */
	public void writeUnitDefinitions(ListOf<UnitDefinition> list_unitDef, ListOf<Unit> list_unit, UnitDefinition unit_def, Model model){
		Unit unit_mole = new Unit(levelAndVersion.getLevel(), levelAndVersion.getVersion());
		unit_mole.setScale(-3);
		unit_mole.setKind(Kind.MOLE);
		
		Unit unit_gram = new Unit(levelAndVersion.getLevel(), levelAndVersion.getVersion());
		unit_gram.setKind(Kind.GRAM);
		unit_gram.setExponent(-1.0);
		
		Unit unit_second = new Unit(levelAndVersion.getLevel(), levelAndVersion.getVersion());
		unit_second.setLevel(levelAndVersion.getLevel());
		unit_second.setMultiplier(0.00027777);
		unit_second.setKind(Kind.SECOND);
		unit_second.setExponent(-1.0);
		
		list_unit.add(unit_mole);
		list_unit.add(unit_gram);
		list_unit.add(unit_second);
		
		unit_def = new UnitDefinition("mmol_per_gDW_per_hr", levelAndVersion.getLevel(), levelAndVersion.getVersion());
		unit_def.setListOfUnits(list_unit);
		
		list_unitDef.add(unit_def);
		
		model.setListOfUnitDefinitions(list_unitDef);
	}
	
	/**
	 * This method loads the compartments
	 * @param model The model
	 */
	public void loadCompartments(Model model){
		for(CompartmentCI comp : container.getCompartments().values()){
			Compartment sbmlCompartment = new Compartment(comp.getId(), comp.getName(), levelAndVersion.getLevel(), levelAndVersion.getVersion());
			sbmlCompartment.setId(comp.getId());
			if(comp.getName()!= null) sbmlCompartment.setName(comp.getName());
			sbmlCompartment.setOutside(comp.getOutside());
			
			try{
				model.addCompartment(sbmlCompartment);
			}catch(Exception e){
				exceptionsMap.put(comp.getId(), e);
			}
		}
		if(!exceptionsMap.isEmpty())
			throw new JSBMLWriterException(exceptionsMap);
	}
	
	/**
	 * This method loads the metabolites
	 * @param model The model
	 */
	public void loadSpecies(Model model){
		
		verifyNeedsStandardize();
		
		
		
		for(CompartmentCI compCI : container.getCompartments().values()){
			for(String s : compCI.getMetabolitesInCompartmentID()){		
				
				
				MetaboliteCI species = container.getMetabolite(s);
				
	//			System.out.println(s + "\t" + species.getId());
				Species sbmlSpecies = new Species(levelAndVersion.getLevel(), levelAndVersion.getVersion());
				
				String sbmlName;// = standardizeMetId(species.getId());
				if(needsToBeStandardized) sbmlName = standardizeMetId(species.getId());
				else sbmlName = species.getId();
				
				
				sbmlSpecies.setId(sbmlName);
				
				sbmlSpecies.setName(species.getName());
				
				sbmlSpecies.setCompartment(compCI.getId());
	//			if(true)
	//				System.out.println(sbmlSpecies.getId());
				
				try{
					addMetaboliteNoteInformation(s, sbmlSpecies);
					model.addSpecies(sbmlSpecies);
				}catch(Exception e){
					exceptionsMap.put(species.getId(), e);
				}
			}
			if(!exceptionsMap.isEmpty())
				throw new JSBMLWriterException(exceptionsMap);
		}
	}
	
	private void addMetaboliteNoteInformation(String s, Species sbmlSpecies) {
		

		XMLNode note = new XMLNode(new XMLTriple("notes"));
		
		MetaboliteCI m = container.getMetabolite(s);
		
		String formula = m.getFormula();
		Integer charge = m.getCharge();
		
		if(formula == null) formula = "";
		if(charge == null) charge = 0;
		
			
		XMLNode gener = new XMLNode(new XMLTriple("p", "", "html"));
		gener.addChild(new XMLNode(FOMULA_PREFIX + formula));
		note.addChild(gener);
		
		XMLNode proteinr = new XMLNode(new XMLTriple("p", "", "html"));
		proteinr.addChild(new XMLNode(CHARGE_PREFIX + charge));
		note.addChild(proteinr);
		
		for(String id : extraMetabolitesInfo){
			XMLNode infoNode = new XMLNode(new XMLTriple("p", "", "html"));
			String info = null;
			
			info = container.getMetabolitesExtraInfo().get(id).get(s);
			if(info == null) info = "";
			infoNode.addChild(new XMLNode(id+": " + info));
			note.addChild(infoNode);
		}
		
		
		
//		note.addChild(proteinc);
//		note.addChild(sub);
		
		sbmlSpecies.setNotes(note);
		
	}

	public void verifyNeedsStandardize(){
		for(MetaboliteCI species : container.getMetabolites().values()){
			needsToBeStandardized |= Character.isDigit(species.getId().charAt(0));
		}
	}
	
	/**
	 * This method loads the drains and define new metabolites for each one of them
	 * @param model
	 * @throws Exception 
	 */
	public void loadDrains(Model model) throws Exception{
		String extcomp = container.getExternalCompartment().getId();
		for(MetaboliteCI newmet : new_metabolites_names_p.values()){
			Species sbmlSpecies = new Species(levelAndVersion.getLevel(), levelAndVersion.getVersion());
			sbmlSpecies.setId(newmet.getId());
			sbmlSpecies.setName(newmet.getName());
			sbmlSpecies.setCompartment(extcomp);
			if(model.getSpecies(newmet.getId())==null)
				model.addSpecies(sbmlSpecies);
		}
		
		for(MetaboliteCI newmet : new_metabolites_names_r.values()){
			Species sbmlSpecies = new Species(levelAndVersion.getLevel(), levelAndVersion.getVersion());
			sbmlSpecies.setId(newmet.getId());
			sbmlSpecies.setName(newmet.getName());
			sbmlSpecies.setCompartment(extcomp);
			if(model.getSpecies(newmet.getId())==null)
				model.addSpecies(sbmlSpecies);
		}
	}
	
	/**
	 * This method loads the reactions
	 * @param model The model
	 * @param unitDef A UnitDefinition object
	 */
	public void loadReactions(Model model, UnitDefinition unitDef){
		for(String rId : container.getReactions().keySet()){
			ReactionCI ogreaction = container.getReactions().get(rId);
			
			String reactionName = standardizerReactId(ogreaction.getId(), ogreaction.getType());
			Reaction sbmlReaction = new Reaction(levelAndVersion.getLevel(), levelAndVersion.getVersion());
			sbmlReaction.setId(reactionName);
			sbmlReaction.setName(ogreaction.getName());
			sbmlReaction.setReversible(ogreaction.isReversible());
			
			
			/** write the notes */
			writeReactionsNotes(ogreaction, sbmlReaction);
			
			/** write bounds and GR notes as palsson does*/
			writeBoundsAndGR(ogreaction, sbmlReaction, unitDef);
			

			/**products*/
			writeProductsOrReactants(ogreaction, sbmlReaction, true, rId, new_metabolites_names_p);
			
			
			/**reactants*/
			writeProductsOrReactants(ogreaction, sbmlReaction, false, rId, new_metabolites_names_r);
			
			try{
				model.addReaction(sbmlReaction);
			}catch(Exception e){
				exceptionsMap.put(rId, e);
			}
		}
		if(!exceptionsMap.isEmpty())
			throw new JSBMLWriterException(exceptionsMap);
	}
	
	Map<String, Exception> exceptionsMap = new HashMap<String, Exception>();
	
	/**
	 * This is an auxiliary method that writes the reactions notes
	 * @param ogreaction A ReactionCI object
	 * @param sbmlReaction A Reaction object
	 */
	public void writeReactionsNotes(ReactionCI ogreaction, Reaction sbmlReaction){
		if(!isIgnoreNotesField()){
			String notes_gene = " ";
			String notes_protein = " ";
			String notes_class = " ";
			String notes_subsystem = " ";
			
			if(ogreaction.getGeneRule() != null && ogreaction.getGeneRule().getRootNode() != null){
					notes_gene += ogreaction.getGeneRule().toString();
			}
			
			if(ogreaction.getProteinRule() != null && ogreaction.getProteinRule().getRootNode() != null){
					notes_protein += ogreaction.getProteinRule().toString();
			}
			
			notes_class += ogreaction.getEc_number();
			
			notes_subsystem += ogreaction.getSubsystem();
			
			if(notes_gene!=null && !notes_gene.isEmpty() && notes_protein!=null && !notes_protein.isEmpty() && notes_class!=null && !notes_class.isEmpty() && notes_subsystem!=null && !notes_subsystem.isEmpty()){
				
				XMLNode note = new XMLNode(new XMLTriple("notes"));
				note.addNamespace("html");
				
				XMLNode gener = new XMLNode(new XMLTriple("p", "", "html"));
				gener.addChild(new XMLNode(GENE_RULE_PREFIX + notes_gene));
				
				XMLNode proteinr = new XMLNode(new XMLTriple("p", "", "html"));
				proteinr.addChild(new XMLNode(PROTEIN_RULE_PREFIX + notes_protein));
				
				XMLNode proteinc = new XMLNode(new XMLTriple("p", "", "html"));
				proteinc.addChild(new XMLNode(PROTEIN_CLASS_PREFIX + notes_class));
				
				XMLNode sub = new XMLNode(new XMLTriple("p", "", "html"));
				sub.addChild(new XMLNode(SUBSYSTEM_PREFIX + notes_subsystem));
				
				
				note.addChild(gener);
				note.addChild(proteinr);
				note.addChild(proteinc);
				note.addChild(sub);
				
				for(String id : extraReactionsInfo){
					XMLNode infoNode = new XMLNode(new XMLTriple("p", "", "html"));
					String info = null;
					
					info = container.getReactionsExtraInfo().get(id).get(ogreaction.getId());
					if(info == null) info = "";
					infoNode.addChild(new XMLNode(id+": " + info));
					note.addChild(infoNode);
				}
				
				sbmlReaction.setNotes(note);
				
			}
		}
	}
	
	/**
	 * This is an auxiliary method that writes the bounds and geneRules
	 * @param ogreaction A ReactionCI object
	 * @param sbmlReaction A Reaction object
	 * @param unitDef A UnitDefinition object
	 */
	public void writeBoundsAndGR(ReactionCI ogreaction, Reaction sbmlReaction, UnitDefinition unitDef){
		
			LocalParameter lowerP = new LocalParameter("LOWER_BOUND");
			LocalParameter upperP = new LocalParameter("UPPER_BOUND");
			if(overrideConstraints.get(ogreaction.getId()) != null) {
				lowerP.setValue(overrideConstraints.get(ogreaction.getId()).getLowerLimit());
				upperP.setValue(overrideConstraints.get(ogreaction.getId()).getUpperLimit());
			}
			else if(container.getDefaultEC().get(ogreaction.getId()) != null){
				lowerP.setValue(container.getDefaultEC().get(ogreaction.getId()).getLowerLimit());
				upperP.setValue(container.getDefaultEC().get(ogreaction.getId()).getUpperLimit());
			}
			else{
				
				if(ogreaction.getReversible())
					lowerP.setValue(-ContainerUtils.DEFAULT_BOUND_VALUE);
				else
					lowerP.setValue(0);
				
				upperP.setValue(ContainerUtils.DEFAULT_BOUND_VALUE);
			}
			lowerP.setName("");
			upperP.setName("");
			
			if(writeUnits){
				lowerP.setUnits(unitDef);
				upperP.setUnits(unitDef);
			}
			

//			LocalParameter objectiveCoeff = new LocalParameter("OBJECTIVE_COEFFICIENT");
//			objectiveCoeff.setName("");
//			
//			LocalParameter fluxValue = new LocalParameter("FLUX_VALUE");
//			fluxValue.setName("");
//			if(writeUnits) fluxValue.setUnits(unit_def);
//		
//			LocalParameter reducedCost = new LocalParameter("REDUCED_COST");
//			reducedCost.setName("");

			KineticLaw law = new KineticLaw();	
			law.addLocalParameter(lowerP);
			law.addLocalParameter(upperP);				
//			law.addLocalParameter(objectiveCoeff);
//			law.addLocalParameter(fluxValue);
//			law.addLocalParameter(reducedCost);
			String math = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><ci> FLUX_VALUE </ci></math>";
//			String math = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"></math>";
			ASTNode mathnode = JSBML.readMathMLFromString(math);			
			law.setMath(mathnode);				
			sbmlReaction.setKineticLaw(law);
	}
	
	/**
	 * This is an auxiliary method that writes the products or the reactants
	 * @param ogreaction A ReactionCI object
	 * @param sbmlreaction A Reaction object
	 * @param isProduct A boolean that tells if we are inserting a product (true) or a reactant (false)
	 * @param z The reactionID of the current reaction
	 * @param new_metabolites_names A structure with the names of the products or of the reactants
	 */
	public void writeProductsOrReactants(ReactionCI ogreaction, Reaction sbmlreaction, boolean isProduct, String z, HashMap<String, MetaboliteCI> new_metabolites_names){
		Map<String, StoichiometryValueCI> aux;
		if(isProduct) aux = ogreaction.getProducts();
		else aux = ogreaction.getReactants();
		
		for(String specie : aux.keySet()){
			SpeciesReference compound = new SpeciesReference(levelAndVersion.getLevel(), levelAndVersion.getVersion());
			String specieName = specie;
			if(needsToBeStandardized) specieName = standardizeMetId(specie);
			compound.setSpecies(specieName);
			if(isProduct){
				compound.setStoichiometry(ogreaction.getProducts().get(specie).getStoichiometryValue());
				sbmlreaction.addProduct(compound);
			} else{
				compound.setStoichiometry(ogreaction.getReactants().get(specie).getStoichiometryValue());
				sbmlreaction.addReactant(compound);
			}
		}
		
		if(new_metabolites_names.containsKey(z)){
			SpeciesReference compound = new SpeciesReference(levelAndVersion.getLevel(), levelAndVersion.getVersion());
			compound.setSpecies(new_metabolites_names.get(z).getId());
			compound.setStoichiometry(1.0);
			if(isProduct) sbmlreaction.addProduct(compound);
			else sbmlreaction.addReactant(compound);
		}
	}
	
	/**
	 * Method that standardizes a reactionID
	 * @param reaction The reactionID
	 * @param reactionTypeEnum The type of reaction
	 * @return The new reactionID
	 */
	private String standardizerReactId(String reaction,
			ReactionTypeEnum reactionTypeEnum) {
		
		String newName = reaction;
		
		if(!(reaction.startsWith("r_") || reaction.startsWith("R_")))
			newName = "R_" + reaction;
		
		newName = newName.replace("-", "_");
		newName = newName.replace("(", "_");
		newName = newName.replace(")", "_");
		newName = newName.replace(",", "_");
		newName = newName.replace(".", "_");
		newName = newName.replace("[", "_");
		newName = newName.replace("]", "_");
		newName = newName.replace(" ", "_");
		
		return newName;
	}

	/**
	 * Method that standardizes a metaboliteID
	 * @param id The metabolite ID
	 * @return The new metaboliteID
	 */
	private String standardizeMetId(String id) {

		if(!(id.startsWith("m_") || id.startsWith("M_")))
			id = "M_"+ id;
		id = id.replace("-", "_");
		id = id.replace("(", "_");
		id = id.replace(")", "_");
		id = id.replace(",", "_");
		id = id.replace(".", "_");
		id = id.replace("[", "_");
		id = id.replace("]", "_");
		id = id.replace(" ", "_");
		id = id.replace("=", "");
		id = id.replace(":", "_");
		id = id.replace("'", "_");
		return id;
	}

	/**
	 * This method validates a XMLNode object
	 * @param node A XMLNode object
	 * @return A boolean telling if the XMLNode is or not valid
	 */
	public boolean validateNode(XMLNode node){
		for(String prefix : ignoredNamespaces){
			String np = node.getPrefix();
			if(np.equalsIgnoreCase(prefix))
				return false;
		}
		return true;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the container
	 */
	public Container getContainer() {
		return container;
	}

	/**
	 * @param container the container to set
	 */
	public void setContainer(Container container) {
		this.container = container;
	}

	/**
	 * @return A boolean telling if is palsoon specific (true) or not (false)
	 */
	public boolean isPalssonSpecific() {
		return palssonSpecific;
	}

	/**
	 * @param palssonSpecific A boolean to set if it is palsson specific (true) or not (false)
	 */
	public void setPalssonSpecific(boolean palssonSpecific) {
		this.palssonSpecific = palssonSpecific;
	}

	/**
	 * @return A boolean telling if it's supposed to ignore the notes field (true) or not (false)
	 */
	public boolean isIgnoreNotesField() {
		return ignoreNotesField;
	}
	
	/**
	 * @param ignoreNotesField A boolean to set if it is supposed to ingore the notes field (true) or not (false)
	 */
	public void setIgnoreNotesField(boolean ignoreNotesField) {
		this.ignoreNotesField = ignoreNotesField;
	}
	
	/**
	 * @return A boolean telling if it is supposed to ignore the CellDesigner annotations
	 */
	public boolean isIgnoreCellDesignerAnnotations() {
		return ignoreCellDesignerAnnotations;
	}

	/**
	 * @param ignoreCellDesignerAnnotations A boolean to set if it is supposed to ignore the CellDesigner annotations
	 */
	public void setIgnoreCellDesignerAnnotations(boolean ignoreCellDesignerAnnotations) {
		this.ignoreCellDesignerAnnotations = ignoreCellDesignerAnnotations;
		if(ignoreCellDesignerAnnotations)
			ignoredNamespaces.add(CELLDESIGNER_NAMESPACE_PREFIX);
	}

	/**
	 * This method gets the notes of a reaction and returns a XMLNode object with it
	 * @param reaction A ReactionCI object
	 * @return A XMLNode with the reaction notes
	 */
	
	//TODO: Remove after 31 Dec 2012
//	@Deprecated
//	private XMLNode getNotes(ReactionCI reaction){
//		String notes="<notes>\n";
//		
//		notes += "<html:p>GENE_ASSOCIATION: "+ reaction.getGeneRule()+"</html:p>\n";
//		notes += "<html:p>PROTEIN_ASSOCIATION: " + reaction.getProteinRule()+"</html:p>\n";
//		notes += "<html:p>SUBSYSTEM: " + reaction.getSubsystem() +"</html:p>";
//		notes += "<html:p>PROTEIN_CLASS: " + reaction.getEc_number()+ "</html:p>";
//		notes+="</notes>";
//					
//		
//		XMLNode annotationsNode = XMLNode.convertStringToXMLNode(notes,spaces);
//		
//		return annotationsNode;
//	}
}
