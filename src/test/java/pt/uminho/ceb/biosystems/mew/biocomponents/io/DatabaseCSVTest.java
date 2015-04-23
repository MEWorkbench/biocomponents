package pt.uminho.ceb.biosystems.mew.biocomponents.io;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.DatabaseCSVFilesReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.NewDatabaseCSVFilesReader;

public class DatabaseCSVTest {
		
	private String getFile(String fileName){
		URL nyData = getClass().getClassLoader().getResource(fileName);
		return nyData.getFile();
	}

//	//@Test
//	public void validateGeneral() throws Exception {
//		
//		Delimiter tab = Delimiter.TAB;
//		
//		DatabaseCSVFilesReader reader = new DatabaseCSVFilesReader(new File(getFile("CSVFiles/iTT548_csvModel_ReactionsSimple")), 
//				new File(getFile("CSVFiles/iTT548_csvModel_MetabolitesSimple"), 
//				new HashMap<String, Integer>(){{put("Name",1); put("Formula",2); put("ID",0);}}, 
//				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("EC Number",6); put("ID",0); put("Gene Rule",3);}}, 
//				"", 
//				"", 
//				tab.toString(), 
//				tab.toString(), 
//				new HashMap<String, Integer>(), 
//				new HashMap<String, Integer>(), 
//				true, 
//				true, 
//				null, 
//				null);
//		
//		Container container = new Container(reader);
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}
//	}
//	
//	//@Test
//	public void validateCompartment() throws Exception {
//		
//		Delimiter tab = Delimiter.TAB;
//		
//		DatabaseCSVFilesReader reader = new DatabaseCSVFilesReader(new File(getFile("CSVFiles/iTT548_csvModel_ReactionsSimple_NO_COMPARTMENT"), 
//				new File(getFile("CSVFiles/iTT548_csvModel_MetabolitesSimple_NO_COMPARTMENT"), 
//				new HashMap<String, Integer>(){{put("Name",1); put("Formula",2); put("ID",0);}}, 
//				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("EC Number",6); put("ID",0); put("Gene Rule",3);}}, 
//				"", 
//				"", 
//				tab.toString(), 
//				tab.toString(), 
//				new HashMap<String, Integer>(), 
//				new HashMap<String, Integer>(), 
//				true, 
//				true, 
//				null, 
//				null);
//		
//		Container container = new Container(reader);
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}
//	}
//	
//	//@Test
//	public void validateWrong() throws Exception {
//		
//		Delimiter tab = Delimiter.TAB;
//		
//		DatabaseCSVFilesReader reader = new DatabaseCSVFilesReader(new File(getFile("CSVFiles/iTT548_csvModel_Reactions"), 
//				new File(getFile("CSVFiles/iTT548_csvModel_Metabolites"), 
//				new HashMap<String, Integer>(){{put("Name",1); put("Formula",2); put("ID",0);}}, 
//				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("EC Number",6); put("ID",0); put("Gene Rule",3);}}, 
//				"", 
//				"", 
//				tab.toString(), 
//				tab.toString(), 
//				new HashMap<String, Integer>(), 
//				new HashMap<String, Integer>(), 
//				true, 
//				true, 
//				null, 
//				null);
//		
//		Container container = new Container(reader);
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}
//	}
//	
//	//@Test
//	public void validateNewReader() throws Exception {
//		
//		Delimiter tab = Delimiter.TAB;
//		
//		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(getFile("CSVFiles/iTT548_csvModel_ReactionsSimpleWrong"), 
//				new File(getFile("CSVFiles/iTT548_csvModel_MetabolitesSimpleWrong"), 
//				new HashMap<String, Integer>(){{put("Name",1); put("Formula",2); put("ID",0);}}, 
//				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("EC Number",6); put("ID",0); put("Gene Rule",3);}}, 
//				"", 
//				"", 
//				tab.toString(), 
//				tab.toString(), 
//				new HashMap<String, Integer>(), 
//				new HashMap<String, Integer>(), 
//				true, 
//				true, 
//				null, 
//				null);
//		
//		Container container = new Container(reader);
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}
//	}
	
	//@Test
	public void validate1471_2164_12_535_s1_Wrong() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(getFile("CSVFiles/Original/1471-2164-12-535-s1_Reactions")), 
				new File(getFile("CSVFiles/Original/1471-2164-12-535-s1_Metabolites")), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}}, 
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}

	}
	
	////@Test
	public void validate1471_2164_12_535_s1_Corrected() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		DatabaseCSVFilesReader reader = new DatabaseCSVFilesReader(new File(getFile("CSVFiles/Corrected/1471-2164-12-535-s1_Reactions")), 
				new File(getFile("CSVFiles/Corrected/1471-2164-12-535-s1_Metabolites")), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}}, 
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}
	}
	
	////@Test
	public void validate1475_Wrong() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(getFile("CSVFiles/Original/1475-2859-13-61-s1_Reactions")), 
				new File(getFile("CSVFiles/Original/1475-2859-13-61-s1_Metabolites")), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}}, 
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}
	}
	
	//@Test
	public void validate1475_Corrected() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader1 = new NewDatabaseCSVFilesReader(new File(getFile("files/CSVFiles/Corrected/1475-2859-13-61-s1_Reactions")), 
				//new File(getFile("CSVFiles/Corrected/1475-2859-13-61-s1_Metabolites"), 
				//new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}}, 
				"", 
				"", 
				tab.toString(), 
				//tab.toString(), 
				new HashMap<String, Integer>(), 
				//new HashMap<String, Integer>(), 
				true, 
				//true, 
				null, 
				null);
		
		Container container2 = new Container(reader1);
//		for (String react : container2.getReactions().keySet()) {
//			System.out.println(container2.getReactions().get(react).getName());			
//		}
		
//		System.out.println("\n\n\n\n\n\\n\n\nNEWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWw");
		
		DatabaseCSVFilesReader reader = new DatabaseCSVFilesReader(new File(getFile("CSVFiles/Corrected/1475-2859-13-61-s1_Reactions")), 
				new File(getFile("CSVFiles/Corrected/1475-2859-13-61-s1_Metabolites")), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}}, 
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
//		for (String react : container.getReactions().keySet()) {
//			System.out.println(container.getReactions().get(react).getName());			
//		}
		
		
		
		
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}
		
	}

	////@Test
	public void validateb818710j_NOT_WORKING_NEWCSV() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(getFile("CSVFiles/Original/b818710j_Reactions")), 
				new File(getFile("CSVFiles/Original/b818710j_Metabolites")), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Equation",0);}}, 
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}
	}
	
	////@Test
	public void validateb818710j_NOT_WORKING() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		DatabaseCSVFilesReader reader = new DatabaseCSVFilesReader(new File(getFile("CSVFiles/Corrected/b818710j_Reactions")), 
				new File(getFile("CSVFiles/Corrected/b818710j_Metabolites")), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);put("Equation",2);}}, 
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}
	}
	
	
	////@Test
	public void validateiBB814_Wrong() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(getFile("CSVFiles/Original/iBB814_Reactions")), 
				new File(getFile("CSVFiles/Original/iBB814_Metabolites")), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}}, 
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}
	}
	
	//@Test
	public void validateiBB814_Corrected() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		DatabaseCSVFilesReader reader = new DatabaseCSVFilesReader(new File(getFile("/files/CSVFiles/Corrected/iBB814_Reactions")), 
				new File(getFile("CSVFiles/Corrected/iBB814_Metabolites")), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}},
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}
	}
	
	
	////@Test
	public void validateiCyc792_Wrong() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(getFile("CSVFiles/Original/iCyc792_Reactions")), 
				new File(getFile("CSVFiles/Original/iCyc792_Metabolites")), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}}, 
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}
	}
	
//	//@Test
	public void validateiCyc792_Corrected() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		DatabaseCSVFilesReader reader = new DatabaseCSVFilesReader(new File(getFile("CSVFiles/Corrected/iCyc792_Reactions")), 
				new File(getFile("CSVFiles/Corrected/iCyc792_Metabolites")), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}},
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}
	}
	
	
//	//@Test
	public void validateiBif452_Wrong() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(getFile("CSVFiles/Original/iBif452.V01.00 (1)_Reactions")), 
				new File(getFile("CSVFiles/Original/iBif452.V01.00 (1)_Metabolites")), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}}, 
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}
	}
	
//	//@Test
	public void validateiBif452_Corrected() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		DatabaseCSVFilesReader reader = new DatabaseCSVFilesReader(new File(getFile("CSVFiles/Corrected/iBif452.V01.00 (1)_Reactions")), 
				new File(getFile("CSVFiles/Corrected/iBif452.V01.00 (1)_Metabolites")), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}},
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println(compartement);
//		}
	}
	
	//@Test
	public void validateMyModelWithMetab() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(getFile("CSVFiles/Original/hmmgModel")), 
				new File(getFile("CSVFiles/Original/hmmgModel_m")), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}}, 
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
		for (String compartement: container.getCompartments().keySet()) {
			System.out.println("\tCompartimento: "+compartement);
		}
		
		for (String reaction : container.getReactions().keySet()) {
			System.out.println(reaction);
			for (String metabolite : container.getReaction(reaction).getMetaboliteSetIds()) {
				System.out.println("\t: " +metabolite);
			}
		}

	}
	
	//@Test
	public void validateMyModelOnlyReact() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(getFile("CSVFiles/Original/iNJ661_Reactions")), 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}},
				"", 
				"", 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
		
		System.out.println("num reactions: " + container.getReactions().size());
		System.out.println("num metabolites: " + container.getMetabolites().size());
		
//		for (String compartement: container.getCompartments().keySet()) {
//			System.out.println("\tCompartimento: "+compartement);
//		}
//		
//		for (String reaction : container.getReactions().keySet()) {
//			System.out.println(reaction);
//			for (String metabolite : container.getReaction(reaction).getMetaboliteSetIds()) {
//				System.out.println("\t: " +metabolite);
//			}
//		}
	}
	
	//@Test
	public void validateMyBasicModelOnlyReact() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(getFile("CSVFiles/Original/BasicModel")), 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}},
				"", 
				"", 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
		for (String compartement: container.getCompartments().keySet()) {
			System.out.println("\tCompartimento: "+compartement);
		}
		
		for (String reaction : container.getReactions().keySet()) {
			System.out.println(reaction);
			for (String metabolite : container.getReaction(reaction).getMetaboliteSetIds()) {
				System.out.println("\t: " +metabolite);
			}
		}
	}
	
	//@Test
	public void validateMyBasicModel() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(getFile("CSVFiles/Original/BasicModel")), 
				new File(getFile("CSVFiles/Original/BasicModelM")), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}}, 
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
		for (String compartement: container.getCompartments().keySet()) {
			System.out.println("\tCompartimento: "+compartement);
		}
		
		for (String reaction : container.getReactions().keySet()) {
			System.out.println(reaction);
			for (String metabolite : container.getReaction(reaction).getMetaboliteSetIds()) {
				System.out.println("\t: " +metabolite);
			}
		}
	}
	
	//@Test
	public void validateMyModelBothReaders() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader newReader = new NewDatabaseCSVFilesReader(new File(getFile("CSVFiles/Corrected/1475-2859-13-61-s1_Reactions")), 
				null, 
				new HashMap<String, Integer>(){{put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Equation",2); put("ID",0);}}, 
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true,
				null, 
				null);
		
		DatabaseCSVFilesReader oldReader = new DatabaseCSVFilesReader(new File(getFile("CSVFiles/Corrected/1475-2859-13-61-s1_Reactions")), 
				new File(getFile("CSVFiles/Corrected/1475-2859-13-61-s1_Metabolites")), 
				new HashMap<String, Integer>(){{put("Name",1); put("ID",0);}}, 
				new HashMap<String, Integer>(){{put("Name",1); put("Equation",2); put("ID",0);}}, 
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(), 
				true, 
				true, 
				null, 
				null);
		
		Container containerNewReader = new Container(newReader);
		Container containerOldReader = new Container(oldReader);
		
		String problems = "";
		
		if(containerNewReader.getCompartments().size() != containerOldReader.getCompartments().size())
			problems+= "Different Compartments in Containers"+ "\n";
		
		if(containerNewReader.getReactions().size() != containerOldReader.getReactions().size())
			problems += "Different Reactions in Containers"+ "\n";
		
		
		for (String compartement: containerNewReader.getCompartments().keySet()){
			if(containerOldReader.getCompartments().containsKey(compartement))
				System.out.println("Compartment: "+compartement);
			else
				problems += "Old Container does not contain compartment " + compartement + "\n";
		}
		
		for (String reaction : containerNewReader.getReactions().keySet()) {
			if(!containerOldReader.getReactions().containsKey(reaction))
				problems += "Old Container does not contain reaction " + reaction + "\n";
			else
			{
				System.out.println(reaction);
				for (String metabolite : containerNewReader.getReaction(reaction).getMetaboliteSetIds()){
					if(!containerOldReader.getReaction(reaction).getMetaboliteSetIds().contains(metabolite))
						problems += "Old Container does not contain metabolite " + metabolite + " in reaction " + reaction + "\n";
					else
						System.out.println("\t: " +metabolite);
				}
			}
		}
		
		for (String compartement: containerOldReader.getCompartments().keySet())
			System.out.println("Compartment: "+compartement);
		
		for (String reaction : containerOldReader.getReactions().keySet()) {
			System.out.println(reaction);
			for (String metabolite : containerNewReader.getReaction(reaction).getMetaboliteSetIds())
				System.out.println("\t: " +metabolite);
		}
		
		if(!problems.equals(""))
			throw new Exception(problems);
	}
	
	
	//@Test
	public void validateMyBasicModelWrong() throws Exception {
		
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(getFile("CSVFiles/Original/BasicModelWrong")), 
				new HashMap<String, Integer>(){{put("Equation",2); put("ID",0);}},  
				"", 
				"",
				tab.toString(), 
				new HashMap<String, Integer>(), 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
		for (String compartement: container.getCompartments().keySet()) {
			System.out.println("\tCompartimento: "+compartement);
		}
		
		for (String reaction : container.getReactions().keySet()) {
			System.out.println(reaction);
			for (String metabolite : container.getReaction(reaction).getMetaboliteSetIds()) {
				System.out.println("\t: " +metabolite);
			}
		}
	}
	
}
