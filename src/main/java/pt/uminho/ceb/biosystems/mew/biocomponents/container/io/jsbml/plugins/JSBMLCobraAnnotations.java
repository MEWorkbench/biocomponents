package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.fbc.converters.CobraToFbcV1Converter;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.InvalidBooleanRuleException;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.jsbml.JSBMLIOPlugin;
import pt.uminho.ceb.biosystems.mew.utilities.math.language.mathboolean.parser.ParseException;

public class JSBMLCobraAnnotations implements JSBMLIOPlugin<Object>{

	
	protected String CHARGE = "CHARGE";
	protected String FORMULA = "FORMULA";
	protected String INCHI = "INCHI";
	protected String SMILES = "SMILES";
	
	@Override
	public String getName() {
		return "cobra.annotations";
	}

	@Override
	public Object read(Model sbmlModel, Container container, Collection<String> warnings) {
		readMetabolites(sbmlModel, container.getMetabolites(), container.getMetabolitesExtraInfo());
		readReactions(sbmlModel, container.getReactions(), container.getReactionsExtraInfo(), warnings);
		return null;
	}

	

	@Override
	public void write(Model sbmlModel, Container container, Object pluginInfo,  SBMLDocument doc) {

	}
	
	
	private void readReactions(Model jsbmlmodel, Map<String, ReactionCI> reactions,
			Map<String, Map<String, String>> reactionExtraInfo, Collection<String> warnings) {
		ListOf<Reaction> sbmlreactions = jsbmlmodel.getListOfReactions();
		for (int i = 0; i < sbmlreactions.size(); i++) {
			Reaction sbmlreaction = sbmlreactions.get(i);
			String reactionId = sbmlreaction.getId();
			ReactionCI rci = reactions.get(reactionId);
			
			
			String notes="";
			try {
				notes = sbmlreaction.getNotesString();
				parserNotes(notes, rci, reactionExtraInfo);
			} catch (InvalidBooleanRuleException e) {
				warnings.add("Problem in reaction "+ sbmlreaction.getId() + ": " + e.getMessage());
			} catch (XMLStreamException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		
	}
	

	public void readMetabolites(Model jsbmlmodel, Map<String, MetaboliteCI> metaboliteList, Map<String, Map<String, String>> extraInfo) {
		ListOf<Species> sbmlspecies = jsbmlmodel.getListOfSpecies();
		for (int i = 0; i < sbmlspecies.size(); i++) {
			Species species = sbmlspecies.get(i);
			String idInModel = species.getId();
			MetaboliteCI ogspecies = metaboliteList.get(idInModel);
			try {
				String notes =species.getNotesString();
				parseGenericInfo(idInModel, notes, extraInfo);
			} catch (XMLStreamException e) {
				
			}
			
			try {
				String formula = extraInfo.get(FORMULA).get(idInModel);
				ogspecies.setFormula(formula);
			} catch (Exception e) {
				
			}
			
			try {
				String charge = extraInfo.get(CHARGE).get(idInModel);
				Integer chargeNum = Integer.parseInt(charge);
				ogspecies.setCharge(chargeNum);
			} catch (Exception e) {
				
			}
			
			try {
				String inchikey = extraInfo.get(INCHI).get(idInModel);
				ogspecies.setInchikey(inchikey);
			} catch (Exception e) {
				
			}
			
			try {
				String smiles = extraInfo.get(SMILES).get(idInModel);
				ogspecies.setSmiles(smiles);
			} catch (Exception e) {
				
			}
			
		}
	}
	
	private void parseGenericInfo(String id,  String notes, Map<String, Map<String, String>> info){
		parseGenericInfo(id,".*?", notes, info);
	}
	
	
	private void parseGenericInfo(String id, String paternKey,  String notes, Map<String, Map<String, String>> info){
		
		if(notes != null){
			
			Pattern pattern = Pattern.compile("(<html:p>|<p>)("+paternKey+"):(.*?)(</html:p>|</p>)");
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
	 * This method parses the notes
	 * @param notes A String with the notes
	 * @param ogreaction A ReactionCI object
	 * @throws InvalidBooleanRuleException 
	 * @throws ParseException
	 * @throws utilities.math.language.mathboolean.parser.ParseException
	 */
	private void parserNotes(String notes, ReactionCI ogreaction, Map<String, Map<String, String>> reactionsExtraInfo) throws InvalidBooleanRuleException{
		
		parseGenericInfo(ogreaction.getId(), notes, reactionsExtraInfo);
		ogreaction.setEc_number(getProteinClass(notes, reactionsExtraInfo));
		
		ogreaction.setGeneRule(getGeneRule(notes, reactionsExtraInfo));
		ogreaction.setSubsystem(getSubstystem(notes, reactionsExtraInfo));
		
		
		
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
	
	
}
