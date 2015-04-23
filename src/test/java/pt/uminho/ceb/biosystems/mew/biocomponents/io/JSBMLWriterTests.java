package pt.uminho.ceb.biosystems.mew.biocomponents.io;

import java.net.URL;

import org.junit.Test;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.FlatFilesReader;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.writers.JSBMLWriter;

public class JSBMLWriterTests {
	
	private String getFile(String fileName){
		URL nyData = getClass().getClassLoader().getResource(fileName);
		return nyData.getFile();
	}

//	@Test
//	public void test() {
//		try{
//		FlatFilesReader file = new FlatFilesReader(getFile("/home/hgiesteira/SaveFiles/WrongModel/Version4_ProtDnaRna.fluxes"), 
//				getFile("/home/hgiesteira/SaveFiles/WrongModel/Version4_ProtDnaRna.matrix"), 
//				getFile("/home/hgiesteira/SaveFiles/WrongModel/Version4_ProtDnaRna.metab"), "", "MyModel");
//		Container container = new Container(file);
//		JSBMLWriter writer = new JSBMLWriter("/home/hgiesteira/SaveFiles/WrongModel/testModel.xml", container);
//		writer.toSBML(writer.getPath());
//		}catch(Exception e){
//			System.out.println("Final!!!!!!");
//			System.out.println(e.getMessage());
//			e.printStackTrace();
//		}
//	}

}
