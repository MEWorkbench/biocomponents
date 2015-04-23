package pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry;

//import integration.integratedcontainers.IntegratedContainer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;
import pt.uminho.ceb.biosystems.mew.utilities.math.MathUtils;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.ContainerUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;


public class BalanceValidator implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int decimalValue = 10;
	
	
	public static final String CORRECT_TAG_REACTION = "CORRECT";
	public static final String BALANCED_TAG_REACTION = "BALANCED";
	public static final String MASS_PROBLEM_TAG_REACTION = "MASS_PROBLEM";
	public static final String CHECK_MET_FORMULAS_TAG_REACTION = "CHECK_MET_FORMULAS";
	public static final String NEED_TO_VALIDATE_H_COMPARTMENT_TAG_REACTION = "NEED_TO_VALIDATE_H_COMPARTMENT";
	public static final String DRAIN_TAG_REACTION = "DRAIN";
	public static final Set<String> ALL_TAGS = new HashSet<String>();
	static{
		ALL_TAGS.add(CORRECT_TAG_REACTION);
		ALL_TAGS.add(BALANCED_TAG_REACTION);
		ALL_TAGS.add(MASS_PROBLEM_TAG_REACTION);
		ALL_TAGS.add(CHECK_MET_FORMULAS_TAG_REACTION);
		ALL_TAGS.add(NEED_TO_VALIDATE_H_COMPARTMENT_TAG_REACTION);
		ALL_TAGS.add(NEED_TO_VALIDATE_H_COMPARTMENT_TAG_REACTION);
	}
	

	public Container cont;
//	public Map<String, String> metToERId;
	public Map<String, MetaboliteFormula> formulas;
	public Map<String, Integer> charges;
	
	
//	TODO: Remove this map
	private Map<String, String> rBalanceInfo;
	private Map<String, Map<String, Double>> rSumReactants;
	private Map<String, Map<String, Double>> rSumProducts;
	private Map<String, Map<String, Double>> rDif;
	
	private Map<String, Double> chargesP;
	private Map<String, Double> chargesR;
	private Map<String, Double> chargesDif;
	
	public Container balContainer = null;
	public Map<String, String> reactionTags = null;
	
	
	public BalanceValidator(Container cont){
		this.cont = cont;
		formulas = new HashMap<String, MetaboliteFormula>();
		charges = new HashMap<String, Integer>();
		rBalanceInfo = new HashMap<String, String>();
		rSumProducts = new HashMap<String, Map<String, Double>>();
		rSumReactants = new HashMap<String, Map<String, Double>>();
		rDif = new HashMap<String, Map<String, Double>>();
		
		chargesP = new HashMap<String, Double>();
		chargesR = new HashMap<String, Double>();
		chargesDif = new HashMap<String, Double>();
		
	}
	
	public Map<String, Integer> getCharges(){
		return charges;
	}
	
	public boolean setFormulasFromContainer(){
		
		boolean ret = false;
		for(MetaboliteCI met : cont.getMetabolites().values()){
			
			String metId = met.getId();
			String formula = met.getFormula();
			Integer charge = met.getCharge();
			if(charge==null) charge = 0;
			
			if(formula != null){
				formulas.put(metId, new MetaboliteFormula(formula));
				
			}else
				formulas.put(metId, new MetaboliteFormula(""));
			charges.put(metId, charge);
			
//			System.out.println(met.getId() + "\t" + formulas.get(metId).getComponets() + "\t" + charges.get(metId));
		}
		
		return ret;
	}
	
	
	public void setMetaboliteFormulas(Map<String,String> metIdToFormula){
		for(String metId : metIdToFormula.keySet()){
			
			String formula = metIdToFormula.get(metId);
			
//			System.out.println("teste: " +formula);
			if(formula!=null)
				formulas.put(metId, new MetaboliteFormula(formula));
		}
	}
	
	public void setMetaboliteCharges(Map<String,Integer> metaboliteCharges){
		
		charges = new HashMap<String, Integer>(metaboliteCharges);
	}
	
	
	public Container getContainer() {
		return cont;
	}

	public void validateAllReactions(){
		validateAllReactions(null);
	}
	
	public void validateAllReactions(Set<String> compToNotBalance){
		
		for(String rid : cont.getReactions().keySet()){
			validateReaction(rid, compToNotBalance);
		}
	}
	
	public Boolean validateReaction(String reactionId) {
		return validateReaction(reactionId, null);
	}

	public Boolean validateReaction(String reactionId,
			Set<String> compToNotBalance) {

		
		boolean ret = true;
		ReactionCI r = cont.getReaction(reactionId);
		StringBuilder sb = new StringBuilder(100000);
		
		
//		System.out.println();
//		System.out.println("Id:\t" + reactionId);
//		System.out.println();
//		System.out.println("ECnumber:\t" + r.getEc_number());
//		System.out.println("reaction:\t"
//				+ ContainerUtils.getReactionToString(cont, reactionId));
//		System.out.println();
//		System.out.println("Reactants");

		sb.append("#############################\n");
		sb.append("Id:      \t" + reactionId+"\n");
		sb.append("Name:    \t" + r.getName()+"\n");
		sb.append("reaction:\t"+ ContainerUtils.getReactionToString(cont, reactionId)+"\n\n");
		sb.append("Reactants\n");
		
		Map<String, Double> sumReactants = integratedBioquesmestry(r.getReactants(), sb);
		Double chargeR = sumCharges(r.getReactants());
		chargesR.put(reactionId, chargeR);
		rSumReactants.put(reactionId, sumReactants);
		
//		System.out.println("Sum: " + printBioquemValues(sumReactants));
//		System.out.println();
//		System.out.println("Products");
		
		sb.append("Sum:\t" +printBioquemValues(sumReactants)+"\n\n");
		sb.append("Products\n");
		
		
		Map<String, Double> sumProducts = integratedBioquesmestry(r
				.getProducts(), sb);
		rSumProducts.put(reactionId, sumProducts);
		Double chargeP = sumCharges(r.getProducts());
		chargesP.put(reactionId, chargeP);
		
		
//		System.out.println("Sum: " + printBioquemValues(sumProducts));
		sb.append("Sum: " + printBioquemValues(sumProducts) +"\n");

		Map<String, Double> res = difBio(sumReactants, sumProducts);
		rDif.put(reactionId, res);

		chargesDif.put(reactionId, chargeP-chargeR);
		
		Set<String> comps = new HashSet<String>(res.keySet());
		for (String comp : comps) {

			Double val = res.get(comp);
			if (MathUtils.round(val, decimalValue) != 0.0) {
				if (compToNotBalance != null
						&& !compToNotBalance.contains(comp))
					ret = false;
			} else
				res.remove(comp);
		}

		sb.append("\nResult:\t" + printBioquemValues(res)+ "\n");
		sb.append("#############################\n");
		
//		System.out.println("\nResult:\t" + printBioquemValues(res));
		rBalanceInfo.put(reactionId, sb.toString());
		
		if(sumProducts.size()==0 || sumReactants.size()==0)
			ret = false;
		
		
		return ret;
	}
	
//	static public boolean isValidated(Container cont, String b){
//		printBioquemValues(sumReactants)
//	}

	private Double sumCharges(Map<String, StoichiometryValueCI> reactants) {
		
		Double ret = 0d;
		
		if(charges!=null)
			for(String metId : reactants.keySet()){
				Integer metCharge = charges.get(metId);
				metCharge=(metCharge==null)?0:metCharge;
				ret+=metCharge*reactants.get(metId).getStoichiometryValue();
			}
		
		return ret;
	}


	public void setMetaboliteFormulasFromFile(String file, String sepRegExp,
			int metIdIndx, int metformulaIdx) throws IOException {
		FileReader fileR = new FileReader(file);
		BufferedReader reader = new BufferedReader(fileR);

		String line = reader.readLine();
		while (line != null) {
			String[] data = line.split(sepRegExp);
			String metId = data[metIdIndx].trim();
			String formula = data[metformulaIdx].trim();

			addMetaboliteFormula(metId, formula);
			line = reader.readLine();
		}

		reader.close();
		fileR.close();
	}

	@SuppressWarnings("unchecked")
	public Set<String> getMetabolitesWithoutFormulas() {

		Set<String> collection1 = cont.getMetabolites().keySet();
		Set<String> collection2 = formulas.keySet();
		return (Set<String>) CollectionUtils.getSetDiferenceValues(collection1,
				collection2);
	}

	public void addMetaboliteFormula(String metaboliteId, String formula) {

		if (cont.getMetabolites().containsKey(metaboliteId)) {
			MetaboliteFormula f = new MetaboliteFormula(formula);
			System.out.println(metaboliteId + "\t" + f.toString());
			formulas.put(metaboliteId, f);
		}
	}
	
	public String getBalanceInfo(String id){
		return rBalanceInfo.get(id);
	}
	
	public String getSumOfProductsToString(String rId){
		
		String ret = "------";
		Map<String, Double> sum = rSumProducts.get(rId);
		if(sum.size() >0)
			ret = printBioquemValues(sum);
		
		return ret;
	}
	
	public String getSumOfReactantsToString(String rId){
		
		String ret = "------";
		Map<String, Double> sum = rSumReactants.get(rId);
		if(sum.size() >0)
			ret = printBioquemValues(sum);
		
		return ret;
	}
	
	public String getDifResultToString(String rId){
		String ret = "balanced";
		
		Map<String, Double> dif = rDif.get(rId);
		Map<String, Double> sumR = rSumReactants.get(rId);
		Map<String, Double> sumP = rSumProducts.get(rId);
		if(dif.size() == 0 && sumP.size() == 0& sumR.size() == 0){
			ret = "???";
		}else if(dif.size()>0){
			ret = printBioquemValues(dif);
		}
		
		return ret;
	}
	
	
	
//	public void setFormulaFromChebi(Map<String, String> metIdToChebi) throws Exception {
//
//		for (String metId : metIdToChebi.keySet()) {
//			String chebiId = metIdToChebi.get(metId);
//
//			if (chebiId != null && !chebiId.endsWith("CHEBI:")) {
//				System.out.println(metId + "\t" + metToERId.get(metId) + "\t"
//						+ chebiId + "\t" + formulas.get(metId));
//				if (!formulas.containsKey(metId)) {
//					ChebiER chebiER = ChebiAPIInterface
//							.getExternalReference(chebiId);
//					String formula = chebiER.getFormula();
//					System.out.println(formula);
//					if (formula != null && !formula.equals("")) {
//
//						addMetaboliteFormula(metId, formula);
//						metToERId.put(metId, chebiId);
//					}
//				}
//			}
//		}
//	}

	public Set<String> getGenericMetabolites() {

		Set<String> ret = new HashSet<String>();
		for (String metId : formulas.keySet()) {
			MetaboliteFormula formula = formulas.get(metId);

			if (formula.isGeneric())
				ret.add(metId);

		}

		return ret;
	}

	public boolean hasGenericMetabolites(String reactionId) {

		boolean ret = false;
		Set<String> metabolites = new HashSet<String>();
		metabolites.addAll(cont.getReaction(reactionId).getProducts().keySet());
		metabolites
				.addAll(cont.getReaction(reactionId).getReactants().keySet());

		for (String metId : metabolites) {
			MetaboliteFormula metFormula = formulas.get(metId);
			if (metFormula == null) {
				ret = true;
				break;
			}
			if (metFormula.isGeneric()) {
				ret = true;
				break;
			}
		}

		return ret;
	}

	public MetaboliteFormula getformula(String metid) {
		return formulas.get(metid);
	}

//	public String getKeggId(String metid) {
//		return metToERId.get(metid);
//	}

	private Map<String, Double> difBio(Map<String, Double> react,
			Map<String, Double> prod) {

		Map<String, Double> ret = new HashMap<String, Double>();
		Set<String> allcomp = new HashSet<String>();
		allcomp.addAll(prod.keySet());
		allcomp.addAll(react.keySet());

		for (String c : allcomp) {

			Double valReact = react.get(c);
			Double valProd = prod.get(c);

			valProd = (valProd == null) ? 0.0 : valProd;
			valReact = (valReact == null) ? 0.0 : valReact;

			Double result = valProd - valReact;

			ret.put(c, result);
		}

		return ret;
	}

	public Set<String> getAllBalancedReactions(Set<String> compToNotBalance) {

		Set<String> ret = new HashSet<String>();

		for (String rid : cont.getReactions().keySet()) {
			if (this.validateReaction(rid, compToNotBalance))
				ret.add(rid);
		}

		return ret;

	}

	// flag = 0 => all the reactions which the metabolite is present are
	// balanced
	// flag = -1 => some reactions which the metabolite is preset are balance
	// flag = -1 => none reaction which the metabolite is preset are balance
	@SuppressWarnings("unchecked")
	public Map<String, Integer> calculateFlagMetabolites(
			Set<String> compToNotBalance) {

		Map<String, Integer> ret = new HashMap<String, Integer>();

		Set<String> br = getAllBalancedReactions(compToNotBalance);
		Set<String> t = cont.identifyTransportReactions();
		for (String mid : cont.getMetabolites().keySet()) {

			Set<String> reactions = cont.getMetabolite(mid).getReactionsId();
			reactions.removeAll(t);

			if (br.containsAll(reactions))
				ret.put(mid, 0);
			else {
				Set<String> diff = (Set<String>) CollectionUtils
						.getSetDiferenceValues(reactions, br);

				if (diff.size() == reactions.size())
					ret.put(mid, -2);
				else
					ret.put(mid, -1);
			}
		}

		return ret;
	}

	private Map<String, Double> integratedBioquesmestry(
			Map<String, StoichiometryValueCI> stoiq, StringBuilder sb) {
		Map<String, Double> ret = new HashMap<String, Double>();

		for (String metId : stoiq.keySet()) {

			MetaboliteFormula f = formulas.get(metId);
			Double stoiqValue = stoiq.get(metId).getStoichiometryValue();

			if (f != null) {
				Set<String> comps = f.getComponets();
				for (String comp : comps) {
					Double value = ret.get(comp);
					value = (value != null) ? value : 0;
					value += stoiqValue * f.getValue(comp);
					ret.put(comp, value);
				}

//				System.out.println(metId + "\t"
//						+ cont.getMetabolite(metId).getName() + "\t"
//						+ f.toString());
				
				sb.append(metId + "\t"
						+ cont.getMetabolite(metId).getName() + "\t"
						+ f.toString() + "\n");
				
			} else {
				
				sb.append(metId + "\t"
						+ cont.getMetabolite(metId).getName() + "\tnull"+"\n");
//				System.out.println(metId + "\t"
//						+ cont.getMetabolite(metId).getName() + "\tnull");
			}
		}

		return ret;
	}

	private String printBioquemValues(Map<String, Double> values) {
		String ret = "";

		for (String comp : values.keySet()) {
			ret += comp + " " + MathUtils.round(values.get(comp), decimalValue)
					+ " | ";
		}

		return ret;
	}
	
	
	public Map<String, Double> getMassR(String reaction){
		return rSumReactants.get(reaction);
	}
	
	public Map<String, Double> getMassP(String reaction){
		return rSumProducts.get(reaction);
	}
	
	public Map<String, Double> getMassDiff(String reaction){
		return rDif.get(reaction);
	}
	
	public Double getChargeP(String reactionId){
		return chargesP.get(reactionId);
	}
	
	public Double getChargeR(String reactionId){
		return chargesR.get(reactionId);
	}
	
	public Double getChargeDiff(String reactionId){
		return chargesDif.get(reactionId);
	}

	
	public Map<String, String> balanceH(String hId) {
		
		reactionTags = new HashMap<String, String>(); 
		balContainer = getContainer().clone();
		
		
		for(String id : balContainer.getReactions().keySet()){
			
			if(!balContainer.getDrains().contains(id)){
				
				ReactionCI r = balContainer.getReaction(id);
				Map<String, Double> massBalance = getMassDiff(id);
				Double chargeBalance = getChargeDiff(id);
				
				if(massBalance.size() == 0 && chargeBalance == 0.0){
					reactionTags.put(id, CORRECT_TAG_REACTION);
				}else if(massBalance.size() == 0 && chargeBalance != 0.0){
					reactionTags.put(id, CHECK_MET_FORMULAS_TAG_REACTION);
				}else if(massBalance.size() > 1){
					reactionTags.put(id, MASS_PROBLEM_TAG_REACTION);
				}else if(massBalance.size() == 1){
					Double hMassBalance = massBalance.get("H");
					if(hMassBalance==null){
//						System.out.println("Problem 1" + massBalance + "\t" + chargeBalance);
						reactionTags.put(id, MASS_PROBLEM_TAG_REACTION);
					}else{
						Set<String> comp = r.identifyCompartments();
						String c = null;
						if(comp.size()>1){
							reactionTags.put(id, NEED_TO_VALIDATE_H_COMPARTMENT_TAG_REACTION);
						}else{
							c = comp.iterator().next(); 
							if(hMassBalance.equals(chargeBalance)){
								
								if(hMassBalance<0){
									Double sEquilibator = Math.abs(hMassBalance);
									balanceStoiq(r.getReactants(), r.getProducts(), sEquilibator, hId, c);
								}else{
									balanceStoiq(r.getProducts(),r.getReactants(), hMassBalance, hId, c);
								}
								reactionTags.put(id, BALANCED_TAG_REACTION);
							}else{
								//System.out.println("Problem 1: " + hMassBalance + "\t" + chargeBalance + " reaction:"+r.getId()+"\r" + (hMassBalance.equals(chargeBalance)));
								reactionTags.put(id, CHECK_MET_FORMULAS_TAG_REACTION);
							}
								
						}
					}
					
				}
				
			}else{
				reactionTags.put(id, DRAIN_TAG_REACTION);
			}
			
		}
		
		return reactionTags;
	}
	
	private static void balanceStoiq(Map<String, StoichiometryValueCI> toRemove,
			Map<String, StoichiometryValueCI> toAdd, Double sEquilibator, String hId, String compartment) {
		
		
		StoichiometryValueCI v = toRemove.get(hId);
		if(v!=null){
			sEquilibator-= v.getStoichiometryValue();
			if(sEquilibator<0){
				v.setStoichiometryValue(-sEquilibator);
			}else{
				toRemove.remove(hId);
			}
		}
		
		if(sEquilibator > 0){
			v = toAdd.get(hId);
			if(v==null) v= new StoichiometryValueCI(hId, 0.0, compartment);
			v.setStoichiometryValue(v.getStoichiometryValue() + sEquilibator);
			toAdd.put(hId,v);
		}
	}
	
	public Map<String, String> getReactionTags(){
		
		return reactionTags;
	}
	
	public Container getBalancedContainer() throws Exception{
		if (balContainer == null) {
			throw new Exception("Container was not balanced. Call method balanceH");
		}
		return balContainer;
	}
	
//	private void getErros() {
//
//		Map<String, List<String>> metKeggIdToMet = new HashMap<String, List<String>>();
//
//		for (String id : metToERId.keySet()) {
//			String keggId = metToERId.get(id);
//			List<String> values = metKeggIdToMet.get(id);
//			values = (values != null) ? values : new ArrayList<String>();
//			values.add(id);
//			metKeggIdToMet.put(keggId, values);
//		}
//
//		for (String id : metKeggIdToMet.keySet()) {
//
//			List<String> teste = metKeggIdToMet.get(id);
//			if (teste.size() > 1)
//				System.out.println("Erro keggid: " + id
//						+ " is several metabolites: " + teste);
//		}
//	}

//	private Map<String, MetaboliteFormula> constructFormulaFromKegg(
//			Map<String, String> keggIds) throws
//			NullPointerException{
//
//		Map<String, MetaboliteFormula> ret = new HashMap<String, MetaboliteFormula>();
//		for (String metId : keggIds.keySet()) {
//
//			String keggId = keggIds.get(metId);
//			KeggCompoundER met = null;
//
//			String formula = null;
//
//			if (keggId != null) {
//				try {
//					met = KEGGAPI.get_compound_by_keggId(keggId);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//				if (met != null) {
//					formula = met.getFormula();
//
//					if (formula != null) {
//						;
//						MetaboliteFormula f = new MetaboliteFormula(formula);
//						ret.put(metId, f);
//
//						System.out.println(metId + "\t" + keggId + "\t"
//								+ f.toString());
//					} else
//						System.err.println("" + metId + "\t" + keggId + "\t"
//								+ formula);
//
//				} else
//					System.err.println("" + metId + "\t" + keggId + "\t"
//							+ formula);
//			}
//		}
//
//		return ret;
//	}

//	public void printMetaboliteInfomationInFile(String path) throws IOException {
//
//		FileWriter filew = new FileWriter(path);
//		BufferedWriter writer = new BufferedWriter(filew);
//
//		for (String mid : cont.getMetabolites().keySet()) {
//
//			String formulaSource = metToERId.get(mid);
//			MetaboliteFormula f = formulas.get(mid);
//			String formula = (f == null) ? null : f.getOriginalFormula();
//			writer.write(mid + "\t" + cont.getMetabolite(mid).getName() + "\t"
//					+ formulaSource + "\t" + formula + "\n");
//		}
//
//		writer.close();
//		filew.close();
//	}

}
