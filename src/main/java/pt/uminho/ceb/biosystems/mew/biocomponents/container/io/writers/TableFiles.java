package pt.uminho.ceb.biosystems.mew.biocomponents.container.io.writers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.TreeSet;

import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.ContainerUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionConstraintCI;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.collection.CollectionUtils;

public class TableFiles {
	
	protected Container cont;
	protected String sepFile;
	protected String reactionFile;
	protected String metaboliteFile;
	
	protected TreeSet<String> extraInfoMetToPrint;
	protected TreeSet<String> extraInfoReactionsToPrint;
	
	
	public TableFiles(Container cont){
		this(cont, ".");
	}
	
	public TableFiles(Container cont, String folder){
		this(cont, folder+"/" + cont.getModelName() + "_reac.tsv", folder+"/" + cont.getModelName() + "_metab.tsv", "\t", null, null);
	}
	
	public TableFiles(Container cont, String reactionFile, String metaboliteFile, String sepFile, TreeSet<String> extraInfoMetToPrint, TreeSet<String> extraInfoReactionsToPrint){
		this.cont = cont;
		this.reactionFile = reactionFile;
		this.metaboliteFile = metaboliteFile;
		
		if(!cont.hasUnicIds()){
			this.cont = cont.clone();
			this.cont.useUniqueIds();
		}
		this.extraInfoMetToPrint = extraInfoMetToPrint;
		this.extraInfoReactionsToPrint = extraInfoReactionsToPrint;
		
		if(extraInfoMetToPrint == null)
			this.extraInfoMetToPrint = new TreeSet<String>(cont.getMetabolitesExtraInfo().keySet());
		if(extraInfoReactionsToPrint == null)
			this.extraInfoReactionsToPrint = new TreeSet<String>(cont.getReactionsExtraInfo().keySet());
		
		this.sepFile = sepFile;
	}
	
	public void write() throws IOException{
		writeMetaboliteFile();
		writeReactionsFile();
	}

	private void writeReactionsFile() throws IOException {
		FileWriter fw = new FileWriter(reactionFile);
		fw.write(getReactionFileHeader() + "\n");
		
		for(String id : cont.getReactions().keySet()){
			fw.write(getInfoReaction(id) + "\n");
		}
		
		fw.close();
		
	}
	private void writeMetaboliteFile() throws IOException {
		FileWriter fw = new FileWriter(metaboliteFile);
		fw.write(getMetabolitesFileHeader()+ "\n");
		
		for(String id : cont.getMetabolites().keySet()){
			fw.write(getInfoMetabolite(id, cont,sepFile, extraInfoMetToPrint) + "\n");
		}
		
		fw.close();
	}


	private String getInfoReaction(String id){
		
		ReactionCI r = cont.getReaction(id);
		String ret = r.getId() + sepFile;
		ret += r.getName() + sepFile;
		ret += r.getEcNumber() + sepFile;
		ret += r.getSubsystem() + sepFile;
		ret += getEquation(r) + sepFile;
		
		ReactionConstraintCI rc = cont.getDefaultEC().get(id);
		if(rc == null){
			rc = new ReactionConstraintCI();
			if(!r.isReversible())
				rc.setLowerLimit(0.0);
		}
		
		ret += rc.getLowerLimit() + sepFile;
		ret += rc.getUpperLimit() + sepFile;
		
		for(String idExtraInfo : extraInfoReactionsToPrint){
			String info = null;
			try {
				info = cont.getReactionsExtraInfo().get(idExtraInfo).get(id);
			} catch (Exception e) {
				 e.printStackTrace();
			}
			
			ret += ((info==null)?"":info) + sepFile;
		}
		
		return ret;
	}
	
	public static String getInfoMetabolite(String id, Container cont, String sepFile, Collection<String> extraInfoMetToPrint) {
		MetaboliteCI m = cont.getMetabolite(id);
		String ret = m.getId() + sepFile;
		ret += m.getName() + sepFile;
		ret += m.getFormula() + sepFile;
		ret += m.getCharge() + sepFile;
		
		for(String idExtraInfo : extraInfoMetToPrint){
			String info = null;
			try {
				info = cont.getMetabolitesExtraInfo().get(idExtraInfo).get(id);
			} catch (Exception e) {
				if(true) e.printStackTrace();
			}
			
			ret += (info==null)?"":info + sepFile;
		}
		
		return ret;
	}
	
	public static void printAllMetabolitesInfo(Container cont, Writer w, String sep) throws IOException{
		
		Collection<String> info = cont.getMetabolitesExtraInfo().keySet();
		
		for(String id : cont.getMetabolites().keySet()){
			w.write(getInfoMetabolite(id, cont, sep, info));
		}
	}
	
	private String getEquation(ReactionCI r) {
		String eq = ContainerUtils.getReactionToString(r, true);
		return eq;
	}

	private String getReactionFileHeader() {
		String[] headers = {"ID", "NAME", "EC NUMBER","SUBSYSTEM", "EQUATION", "LOWER BOUND", "UPPER BOUND"};
		String h = CollectionUtils.join(headers, sepFile) +sepFile + CollectionUtils.join(extraInfoReactionsToPrint, sepFile) ;
		return h;
	}
	
	private String getMetabolitesFileHeader() {
		String[] headers = {"ID", "NAME", "FORMULA","CHARGE"};
		String h = CollectionUtils.join(headers, sepFile) +sepFile + CollectionUtils.join(extraInfoMetToPrint, sepFile) ;
		return h;
	}
}
