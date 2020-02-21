package hw5;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class DB {

	/**
	 * Creates a database object with the given name.
	 * The name of the database will be used to locate
	 * where the collections for that database are stored.
	 * For example if my database is called "library",
	 * I would expect all collections for that database to
	 * be in a directory called "library".
	 * 
	 * If the given database does not exist, it should be
	 * created.
	 */
	private String dbName;
	private ArrayList<DBCollection> collections = new ArrayList<DBCollection>();
	
	public DB(String name) throws IOException {
		File directory = new File("testfiles/"+name).getAbsoluteFile();
		this.dbName = name;
	    if (directory.exists()!=true){
	        Files.createDirectories(directory.toPath());    
	    }
	    
	    //https://stackoverflow.com/questions/1844688/how-to-read-all-files-in-a-folder-from-java
	    if(directory.listFiles().length!=0) {
	    	for(int i=0;i<directory.listFiles().length;i++) {
	    		File each = directory.listFiles()[i];
		        if (each.isFile()) {
		        	String fileName = each.getName();
		        	int pos = fileName.lastIndexOf(".");
		        	if (pos > 0) {
		        		fileName = fileName.substring(0, pos);
		        	}
		            DBCollection eachCollection = new DBCollection (this,fileName);
		            collections.add(eachCollection);
		        }
		    }
	    }
	}
	
	/**
	 * Retrieves the collection with the given name
	 * from this database. The collection should be in
	 * a single file in the directory for this database.
	 * 
	 * Note that it is not necessary to read any data from
	 * disk at this time. Those methods are in DBCollection.
	 * @throws IOException 
	 */
	public DBCollection getCollection(String name){
		if(this.collections!=null) {
			for(DBCollection collection:this.collections) {
				if(collection.getName() == name) {
					return collection;
				}
			}
		}
		try {
			return new DBCollection(this,name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Drops this database and all collections that it contains
	 */
	public void dropDatabase() {
		this.collections = null;
		File directory = new File("testfiles/"+this.dbName);
		deleteDirectory(directory);
	    this.dbName = null;
	}
	
	public String getDbName() {
		return this.dbName;
	}
	
	//https://stackoverflow.com/questions/3987921/not-able-to-delete-the-directory-through-java
	public static boolean deleteDirectory(File path) {
	    if (path.exists()) {
	        File[] files = path.listFiles();
	        if(files!=null) {
		        for (int i = 0; i < files.length; i++) {
		            if (files[i].isDirectory()) {
		                deleteDirectory(files[i]);
		            } else {
		                files[i].delete();
		            }
		        }
	        }
	    }
	    return (path.delete());
	}
}