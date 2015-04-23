package pt.uminho.ceb.biosystems.mew.biocomponents.container.components;

import java.io.Serializable;

public class CrossReference implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String idInDB;
	protected String dbName;
	
	
	public CrossReference(String idInDB, String dbName){
		this.idInDB = idInDB;
		this.dbName = dbName;
	}


	public String getIdInDB() {
		return idInDB;
	}


	public void setIdInDB(String idInDB) {
		this.idInDB = idInDB;
	}


	public String getDbName() {
		return dbName;
	}


	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
	
}
