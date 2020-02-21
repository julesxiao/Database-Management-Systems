package hw5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

public class DBCollection {

	/**
	 * Constructs a collection for the given database
	 * with the given name. If that collection doesn't exist
	 * it will be created.
	 * @throws IOException 
	 */
	private DBCursor dbCursor;
	private DB db;
	private String collectionName;
	private ArrayList<JsonObject> DbDocuments = new ArrayList<JsonObject>();
	private ArrayList<JsonObject> DbDocumentsForStore = new ArrayList<JsonObject>();
	private File filePath = null;
	
	public DBCollection(DB database, String name) throws IOException {
		this.db = database;
		String dbName = database.getDbName();
		String dir = "testfiles/"+ dbName + "/" + name + ".json";
		File tmpDir = new File(dir);
		this.collectionName = name;
		this.filePath = tmpDir;

		if(!tmpDir.exists()) {
			tmpDir.createNewFile(); 
		}
		
		//read the .json file and get the documents in this collection
		//https://stackoverflow.com/questions/25346512/read-multiple-objects-json-with-java
		BufferedReader br = new BufferedReader(new FileReader(dir));     
		if (br.readLine() != null) {
			JsonStreamParser JsonObjs = new JsonStreamParser(new InputStreamReader(new FileInputStream(dir)));
			while (JsonObjs.hasNext()) {
				JsonObject currentDoc = JsonObjs.next().getAsJsonObject();
				JsonObject tempDoc = new JsonObject();
				for(String eachkey:currentDoc.keySet()) {
					if(eachkey.compareTo("Id")!=0) {
						tempDoc.add(eachkey, currentDoc.get(eachkey)); 
					}
				}
				this.DbDocuments.add(tempDoc);
			}
		}
		
		for(JsonObject eachDoc:DbDocuments) {
			this.DbDocumentsForStore.add(eachDoc);
		}
	    this.dbCursor = new DBCursor(this,null,null);
	}
	
	/**
	 * Returns a cursor for all of the documents in
	 * this collection.
	 */
	public DBCursor find() {
		return this.dbCursor;
	}
	
	/**
	 * Finds documents that match the given query parameters.
	 * 
	 * @param query relational select
	 * @return
	 */
	public DBCursor find(JsonObject query) {
		return new DBCursor(this,query,null);
	}
	
	/**
	 * Finds documents that match the given query parameters.
	 * 
	 * @param query relational select
	 * @param projection relational project
	 * @return
	 */
	public DBCursor find(JsonObject query, JsonObject projection) {
		return new DBCursor(this,query,projection);
	}
	
	/**
	 * Inserts documents into the collection
	 * Must create and set a proper id before insertion
	 * When this method is completed, the documents
	 * should be permanently stored on disk.
	 * @param documents
	 */
	public void insert(JsonObject... documents) {
		for(JsonObject eachDocument:documents) {
			this.DbDocuments.add(eachDocument);
			// create and set a proper id implemented in updateFileToStore
			updateFileToStore(eachDocument,true);
		}
		updateJsonFile();
	}
	
	/**
	 * Locates one or more documents and replaces them
	 * with the update document.
	 * @param query relational select for documents to be updated
	 * @param update the document to be used for the update
	 * @param multi true if all matching documents should be updated
	 * 				false if only the first matching document should be updated
	 */
	public void update(JsonObject query, JsonObject update, boolean multi) {
		DBCursor dbCursor = this.find(query);
		if(dbCursor!=null) {
			if(multi) {
				while(dbCursor.hasNext()) {
					this.DbDocuments.remove(dbCursor.next());
					this.DbDocuments.add(update);
					
					updateFileToStore(update,true);
				}
			}
			else {
				while(dbCursor.hasNext()) {
					this.DbDocuments.remove(dbCursor.next());
					break;
				}
				this.DbDocuments.add(update);
				updateFileToStore(update,true);
			}
			updateJsonFile();
		}
	}
	
	/**
	 * Removes one or more documents that match the given
	 * query parameters
	 * @param query relational select for documents to be removed
	 * @param multi true if all matching documents should be updated
	 * 				false if only the first matching document should be updated
	 */
	public void remove(JsonObject query, boolean multi) {
		DBCursor dbCursor = this.find(query);
		if(dbCursor!=null) {
			if(multi) {
				while(dbCursor.hasNext()) {
					JsonObject objRemove = dbCursor.next();
					this.DbDocuments.remove(objRemove);
					updateFileToStore(objRemove, false);
				}
			}
			else {
				while(dbCursor.hasNext()) {
					JsonObject objRemove = dbCursor.next();
					this.DbDocuments.remove(objRemove);
					updateFileToStore(objRemove, false);
					break;
				}
			}
			updateJsonFile();
		}
	}
	
	/**
	 * Returns the number of documents in this collection
	 */
	public long count() {
		return (long)this.DbDocuments.size();
	}
	
	public String getName() {
		return this.collectionName;
	}
	
	/**
	 * Returns the ith document in the collection.
	 * Documents are separated by a line that contains only a single tab (\t)
	 * Use the parse function from the document class to create the document object
	 */
	public JsonObject getDocument(int i) {
		return this.DbDocuments.get(i);
	}
	
	/**
	 * Drops this collection, removing all of the documents it contains from the DB
	 */
	public void drop() {
		if(this!=null) {
			this.DbDocuments = new ArrayList<JsonObject>();
			this.DbDocumentsForStore = new ArrayList<JsonObject>();
			File tmpDir = new File("testfiles/"+ this.db.getDbName() + "/" + this.collectionName + ".json");
			this.updateJsonFile();
			DB.deleteDirectory(tmpDir);
		    this.dbCursor = new DBCursor(this,null,null);
		}
	}
	
	public void updateJsonFile() {
		String fileToStore = "";
		
		for(JsonObject eachDocument:this.DbDocumentsForStore) {
			fileToStore = fileToStore + Document.toJsonString(eachDocument) + "\r\n";
		}
		try {
            Files.write(Paths.get(this.filePath.toString()), fileToStore.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public ArrayList<JsonObject> getDbDocuments(){
		return this.DbDocuments;
	}
	
	public void updateFileToStore(JsonObject update, boolean addOrNot) {
		String eachObj = Document.toJsonString(update);
		JsonObject eachJsonObj = Document.parse(eachObj);
		if(addOrNot) {
//			//timestamp as ID
//			Date date = new Date();
//			SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyhmmss");
//			String formattedDate = sdf.format(date);
			
			Integer[] arr = new Integer[1000];
		    for (int i = 0; i < arr.length; i++) {
		        arr[i] = i;
		    }
		    Collections.shuffle(Arrays.asList(arr));
			
			eachJsonObj.addProperty("Id", Integer.toString(arr[0]));
			this.DbDocumentsForStore.add(eachJsonObj);
			}
		else {
			this.DbDocumentsForStore.remove(eachJsonObj);
		}
	}
}
