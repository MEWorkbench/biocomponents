package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;

public class CSVMatcher {
	// same values in ReactionsDatabaseCSVReaderGUI
	public static String METID = "ID";
	public static String METNAME = "Name";
	public static String METFORMULA = "Formula";

	private Container container;
	private Map<String, String> metaRegExp;

	private Pattern patModel;
	private Pattern patDB;
	private boolean hasModelRegExp = false;
	// association the field to metabolite
	private HashMap<String, Set<String>> relFieldMeta;

	public CSVMatcher(Container container, Map<String, String> metaRegExp) throws Exception {
		this.container = container;
		this.metaRegExp = metaRegExp;
		this.relFieldMeta = new HashMap<String, Set<String>>();
		if (metaRegExp != null) {
			if (!metaRegExp.get("ModelField").equals("")) {
				if (!metaRegExp.get("ModelRegExp").equals("")) {
					hasModelRegExp = true;
					patModel = Pattern.compile(metaRegExp.get("ModelRegExp"));
				}
				createMetRelations();
			}

			if (!metaRegExp.get("DatabaseField").equals("") && !metaRegExp.get("DatabaseRegExp").equals("")) {
				patDB = Pattern.compile(metaRegExp.get("DatabaseRegExp"));
			}
		}
	}

	public String[] convertMetaId(String dbFieldValue, String dbCompoundId) throws Exception {
		String[] modelMetaMatch = null;
		// get the value of group 1 if database field value has regular
		// expression otherwise is the original value.
		if (!metaRegExp.get("DatabaseRegExp").equals("")) {
			Matcher m = patDB.matcher(dbFieldValue);
			if (m.matches()) {
				try {
					dbFieldValue = m.group(1);
				} catch (Exception e) {
					throw new Exception("Group 1 not found in regular expression!");
				}
			}
		}
		// System.out.println("Database group1: " + dbFieldValue);
		// the field is present in the relation between model field and
		// metabolites
		if (relFieldMeta.containsKey(dbFieldValue)) {
			modelMetaMatch = getMatch(dbFieldValue, dbCompoundId);
		}
		return modelMetaMatch;
	}

	private String[] getMatch(String field, String dbCompoundId) {
		String[] modelMetaMatch = null;
		Set<String> modelMetabolites = relFieldMeta.get(field);
		// verify if the match is with only one metabolite of model and that
		// bellow only to one compartment
		if (modelMetabolites.size() == 1) {
			String metaId = (String) modelMetabolites.toArray()[0];
			Set<String> compartments = container.getMetaboliteCompartments((String) modelMetabolites.toArray()[0]);
			if (compartments.size() == 1) {
				String compartment = (String) compartments.toArray()[0];
				modelMetaMatch = new String[2];
				modelMetaMatch[0] = dbCompoundId + "[" + compartment + "]";
				modelMetaMatch[1] = metaId;
			}
		}
		return modelMetaMatch;
	}

	private void createMetRelations() throws Exception {
		if (metaRegExp.get("ModelField") != null) {
			if (metaRegExp.get("ModelField").equals(METID)) {
				for (String meta : container.getMetabolites().keySet()) {
					processMeta(meta, container.getMetabolite(meta).getId());
				}

			} else if (metaRegExp.get("ModelField").equals(METNAME)) {
				for (String meta : container.getMetabolites().keySet()) {
					processMeta(meta, container.getMetabolite(meta).getName());
				}

			} else if (metaRegExp.get("ModelField").equals(METFORMULA)) {
				for (String meta : container.getMetabolites().keySet()) {
					processMeta(meta, container.getMetabolite(meta).getFormula());
				}

			} else { // Field are in extra info area
				Map<String, String> extInfo = container.getMetabolitesExtraInfo().get(metaRegExp.get("ModelField"));
				for (String meta : extInfo.keySet()) {
					processMeta(meta, extInfo.get(meta));
				}

			}
		}
	}

	private void processMeta(String meta, String fieldValue) throws Exception {
		String modelFieldValue = fieldValue;
		if (hasModelRegExp) {
			Matcher m = patModel.matcher(fieldValue);
			if (m.matches()) {
				try {
					modelFieldValue = m.group(1);
				} catch (Exception e) {
					throw new Exception("Group 1 not found in regular expression!");
				}
			}
		}
		if (relFieldMeta.containsKey(modelFieldValue)) {
			relFieldMeta.get(modelFieldValue).add(meta);
		} else {
			TreeSet<String> newArray = new TreeSet<String>();
			newArray.add(meta);
			relFieldMeta.put(modelFieldValue, newArray);
		}
	}

	// gets and sets
	public Map<String, String> getMetaRegExp() {
		return metaRegExp;
	}

	public void setMetaRegExp(Map<String, String> metaRegExp) {
		this.metaRegExp = metaRegExp;
	}
	// public Set<String> convertMetaId2(String dbFieldValue, String
	// compartment) throws Exception {
	// Set<String> modelMetaMatch = new TreeSet<String>();
	// String modelFieldValue = "";
	//
	// // get the value of group 1 if database field value has regular
	// // expression otherwise is the original value.
	// if (!metaRegExp.get("DatabaseRegExp").equals("")) {
	// Matcher m =
	// Pattern.compile(metaRegExp.get("DatabaseRegExp")).matcher(dbFieldValue);
	// if (m.matches()) {
	// try {
	// dbFieldValue = m.group(1);
	// } catch (Exception e) {
	// throw new Exception("Group 1 not found in regular expression!");
	// }
	// }
	// }
	// // System.out.println("Database group1: " + dbFieldValue);
	//
	// // for all fields in container apply the regular expression and see if
	// // the field are equal to the fieldValue
	// if (!metaRegExp.get("ModelRegExp").equals("")) {
	// Pattern p = Pattern.compile(metaRegExp.get("ModelRegExp"));
	// for (String field : relFieldMeta.keySet()) {
	// Matcher m = p.matcher(field);
	// if (m.matches()) {
	// try {
	// modelFieldValue = m.group(1);
	// } catch (Exception e) {
	// throw new Exception("Group 1 not found in regular expression!");
	// }
	// }
	// if (dbFieldValue.equals(modelFieldValue)) {
	// // insert the metaIds that bellow to the same compartment of
	// // the database
	// for (String meta : relFieldMeta.get(field)) {
	// if
	// (container.getCompartment(compartment).getMetabolitesInCompartmentID().contains(meta))
	// modelMetaMatch.add(meta);
	// }
	// }
	// }
	// } else if (relFieldMeta.containsKey(dbFieldValue)) {
	// for (String meta : relFieldMeta.get(dbFieldValue)) {
	// if
	// (container.getCompartment(compartment).getMetabolitesInCompartmentID().contains(meta))
	// modelMetaMatch.add(meta);
	// }
	// }
	// return modelMetaMatch;
	// }
}
