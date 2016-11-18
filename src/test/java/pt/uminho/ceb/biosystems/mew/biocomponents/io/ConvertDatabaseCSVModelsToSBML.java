package pt.uminho.ceb.biosystems.mew.biocomponents.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.NewDatabaseCSVFilesReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.writers.JSBMLWriter;
import pt.uminho.ceb.biosystems.mew.utilities.io.Delimiter;
import pt.uminho.ceb.biosystems.mew.utilities.io.FileUtils;

public class ConvertDatabaseCSVModelsToSBML {
	
	String modelsFolder = "/home/hgiesteira/Desktop/Models/ToConvert/";
	String convertedFolder = "/home/hgiesteira/Desktop/Models/ToConvert/SBML/";
	String originalFolder = modelsFolder + "Originals/";
	
//	Models present in sourceforge
//	iYS432: https://sourceforge.net/p/optflux/support-requests/37/
//	iKK446: https://sourceforge.net/p/optflux/support-requests/36/
//	iAN818m: https://sourceforge.net/p/optflux/support-requests/35/
//	iJM658: https://sourceforge.net/p/optflux/support-requests/34/
	
	@Test
	public void convert_iJM658() throws Exception{
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(originalFolder + "iJM658_Reactions"), 
				new File(originalFolder + "iJM658_Metabolites"),
				new HashMap<String, Integer>(){{
					put(NewDatabaseCSVFilesReader.METID,0); 
					put(NewDatabaseCSVFilesReader.METNAME,1);}}, 
				new HashMap<String, Integer>(){{
					put(NewDatabaseCSVFilesReader.REACID,0); 
					put(NewDatabaseCSVFilesReader.REACNAME,1);
					put(NewDatabaseCSVFilesReader.REACEQUATION,2);
					put(NewDatabaseCSVFilesReader.REACGENERULE,3);
					put(NewDatabaseCSVFilesReader.REACSUBSYSTEM,6);
					put(NewDatabaseCSVFilesReader.REACLB,8);
					put(NewDatabaseCSVFilesReader.REACUB,9);}},  
				"", 
				"", 
				tab.toString(), 
				tab.toString(), 
				new HashMap<String, Integer>(), 
				new HashMap<String, Integer>(){{
					put(NewDatabaseCSVFilesReader.REACGENERULE,3);
					put(NewDatabaseCSVFilesReader.REACSUBSYSTEM,6);
					put(NewDatabaseCSVFilesReader.REACECNUMBER,12);}}, 
				true, 
				true, 
				null, 
				null);
		
		Container container = new Container(reader);
		
		JSBMLWriter sbml = new JSBMLWriter(convertedFolder + "iJM658_2.xml", container);
		sbml.writeToFile();
	}
	
	@Test
	public void convert_iAN818m() throws Exception{
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(originalFolder + "iAN818m_Reactions"), 
				new File(originalFolder + "iAN818m_Metabolites"),
				new HashMap<String, Integer>(){{
					put(NewDatabaseCSVFilesReader.METID,0); 
					put(NewDatabaseCSVFilesReader.METNAME,1);}}, 
				new HashMap<String, Integer>(){{
					put(NewDatabaseCSVFilesReader.REACID,0); 
					put(NewDatabaseCSVFilesReader.REACECNUMBER,2);
//					put(NewDatabaseCSVFilesReader.REACEQUATION,2);
//					put(NewDatabaseCSVFilesReader.REACGENERULE,3);
//					put(NewDatabaseCSVFilesReader.REACSUBSYSTEM,6);
//					put(NewDatabaseCSVFilesReader.REACLB,8);
					put(NewDatabaseCSVFilesReader.REACEQUATION,1);}},  
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
		
		
		
		JSBMLWriter sbml = new JSBMLWriter(convertedFolder + "iAN818m_2.xml", container);
		sbml.writeToFile();
		
	}
	
	@Test
	public void convert_iKK446() throws Exception{
		Delimiter tab = Delimiter.TAB;
		
		NewDatabaseCSVFilesReader reader = new NewDatabaseCSVFilesReader(new File(originalFolder + "iKK446_Reactions"), 
				new File(originalFolder + "iKK446_Metabolites"),
				new HashMap<String, Integer>(){{
					put(NewDatabaseCSVFilesReader.METID,0); 
					put(NewDatabaseCSVFilesReader.METNAME,1);}}, 
				new HashMap<String, Integer>(){{
					put(NewDatabaseCSVFilesReader.REACID,0); 
					put(NewDatabaseCSVFilesReader.REACECNUMBER,3);
//					put(NewDatabaseCSVFilesReader.REACEQUATION,2);
//					put(NewDatabaseCSVFilesReader.REACGENERULE,3);
//					put(NewDatabaseCSVFilesReader.REACSUBSYSTEM,6);
//					put(NewDatabaseCSVFilesReader.REACLB,8);
					put(NewDatabaseCSVFilesReader.REACEQUATION,1);}},  
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
		
		
		
		JSBMLWriter sbml = new JSBMLWriter(convertedFolder + "iKK446.xml", container);
		sbml.writeToFile();
		
	}
	
	@Test
	public void readStoich() throws IOException{
		String file = "/home/hgiesteira/Desktop/Models/ToConvert/T";
		String reactions = "/home/hgiesteira/Desktop/Models/ToConvert/Originals/iAN818m_Reactions";
		
		List<String> stoic = FileUtils.readLines(file);
		List<String> reactionsList = FileUtils.readLines(reactions);
		reactionsList.remove(0);
		
		System.out.println(stoic.size() + "\t" + reactionsList.size());
		
		List<String> equations = new ArrayList<String>();
		for (String line : stoic) {
			String[] splitted = line.split("\t");
			equations.add(splitted[0]);
		}
		
		Map<String, String> reactionEquation = new HashMap<String, String>();
		
		for (String line : reactionsList) {
			String[] splitted = line.split("\t");
			reactionEquation.put(splitted[1], splitted[0]);
		}
		
		for (String eq : equations) {
			if(reactionEquation.containsKey(eq)){
				System.out.println(reactionEquation.get(eq) + " = " + eq);
			}
		}
	}
	
}
