package hw5;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DBCursor implements Iterator<JsonObject>{
	
	private Iterator dbIterator;
	private ArrayList<JsonObject> matchedDocument = new ArrayList<JsonObject>();

	public DBCursor(DBCollection collection, JsonObject query, JsonObject fields) {
		if(query!=null) {
			for(String eachQueryKey:query.keySet()) {
				// Situation 1: Embedded documents, nesting will only be two levels deep at most
				if(eachQueryKey.contains(".")) {
					String firstLevel = eachQueryKey.split("\\.")[0]; 
					String secondLevel = eachQueryKey.split("\\.")[1];
					
					for(JsonObject eachDocument:collection.getDbDocuments()) {
						if (eachDocument.getAsJsonObject(firstLevel)==null){
							continue;
						}

						JsonElement eachDoc = eachDocument.getAsJsonObject(firstLevel).get(secondLevel);
						JsonElement eachQueryVal1 = query.get(eachQueryKey);
						//case 1.1: simple case
						if(eachQueryVal1.isJsonPrimitive()){
							if (query.get(eachQueryKey).equals(eachDoc)) matchedDocument.add(eachDocument);
						} 
						//Case 1.2: comparator
						else if(eachQueryVal1.isJsonObject()){
							String compareString = query.get(eachQueryKey).toString();
							if(compareString.contains(":") && compareString.contains("$")) {
								this.matchedDocument = JsonComparator(this.matchedDocument,eachDocument, query, eachQueryKey);
							}
						}
						// Case 1.3:json Array
						else if(eachQueryVal1.isJsonArray()){
							this.matchedDocument = compareJsonArray(this.matchedDocument,eachDocument,collection, query, eachQueryKey);
						}
					}
				}
				
				else{
					for(JsonObject eachDocument:collection.getDbDocuments()) {
						JsonElement eachQueryVal2 = query.get(eachQueryKey);
						JsonElement eachDocSub = eachDocument.get(eachQueryKey);
						//case 2.1: simple case
						if(eachQueryVal2.isJsonPrimitive()) {
							if (eachQueryVal2.equals(eachDocSub)) matchedDocument.add(eachDocument);
							}
						//case 2.2:comparator
						else if(eachQueryVal2.isJsonArray()){
							this.matchedDocument = compareJsonArray(this.matchedDocument,eachDocument,collection, query, eachQueryKey);
						}
						//case 1.3:json array
						else if(eachQueryVal2.isJsonObject()){
							String compareString = eachQueryVal2.toString();
							if(compareString.contains(":") && compareString.contains("$")) {
								this.matchedDocument = JsonComparator(this.matchedDocument,eachDocument, query, eachQueryKey);
							}
						}
					}
				}				
			}
		}
		else {
			matchedDocument = collection.getDbDocuments();
		}
		
		//A projection can explicitly include several fields by setting the <field> to 1 in the projection 
		if(fields!=null) 
		{
			ArrayList<JsonObject> updatedDocument = new ArrayList<JsonObject>();
			for(JsonObject eachDocument:matchedDocument) {
				JsonObject e = new JsonObject(); 
				for(String eachDocKey:eachDocument.keySet()) {
					for(String eachFieldKey:fields.keySet()) {
						if(eachDocKey.compareTo(eachFieldKey)==0 ) {
							if(fields.get(eachFieldKey).toString().equals("1")){
								e.add(eachFieldKey, eachDocument.get(eachFieldKey));
								
							}
						}	
						else {
							e.add(eachDocKey, eachDocument.get(eachDocKey));
						}
						
					}	
				}
				updatedDocument.add(e);
			}
			this.matchedDocument = updatedDocument;
		}
		
		this.dbIterator = this.matchedDocument.iterator();
	}
	
	/**
	 * Returns true if there are more documents to be seen
	 */
	public boolean hasNext() {
		return this.dbIterator.hasNext();
	}

	/**
	 * Returns the next document
	 */
	public JsonObject next() {
		return (JsonObject)this.dbIterator.next();
	}
	
	/**
	 * Returns the total number of documents
	 */
	public long count() {
		return (long)this.matchedDocument.size();
	}
	
	public ArrayList<JsonObject> compareJsonArray(ArrayList<JsonObject> documents,JsonObject eachDoc,DBCollection collection, JsonObject query, String eachQueryKey) {
		if(eachQueryKey.contains(".")) {
			String first = eachQueryKey.split("\\.")[0]; 
			String second = eachQueryKey.split("\\.")[1];
			if(query.get(eachQueryKey).equals(eachDoc.getAsJsonObject(first).get(second))) documents.add(eachDoc);
		}
		else {
			if(query.get(eachQueryKey).equals(eachDoc.get(eachQueryKey))) documents.add(eachDoc);
		}
		return documents;
	}
	
	public ArrayList<JsonObject> JsonComparator(ArrayList<JsonObject> documents,JsonObject eachDoc, JsonObject query, String eachQueryKey){
		// Comparator
		/**
		 * Name	Description
			$eq	Matches values that are equal to a specified value.
			$gt	Matches values that are greater than a specified value.
			$gte	Matches values that are greater than or equal to a specified value.
			$in	Matches any of the values specified in an array.
			$lt	Matches values that are less than a specified value.
			$lte	Matches values that are less than or equal to a specified value.
			$ne	Matches all values that are not equal to a specified value.
			$nin	Matches none of the values specified in an array.
		 *
		 **/
		String operator = "";
		Iterator keySet = query.getAsJsonObject(eachQueryKey).keySet().iterator();
		while(keySet.hasNext()) {
			operator = (String) keySet.next();
		}

		JsonElement operatorVal = query.getAsJsonObject(eachQueryKey).get(operator);
		
		JsonElement docVal = null;
		if(eachQueryKey.contains(".")) {
			String first = eachQueryKey.split("\\.")[0]; 
			String second = eachQueryKey.split("\\.")[1];
			docVal = eachDoc.getAsJsonObject(first).get(second);
		}
		else {
			docVal = eachDoc.get(eachQueryKey);}
		
		//build 2 lists to compare
		List<String> listDoc = new ArrayList<String>();
		if(docVal.isJsonArray()) {
			for(int i = 0; i < docVal.getAsJsonArray().size(); i++){
				listDoc.add(docVal.getAsJsonArray().get(i).toString());
			}
		}
		if(docVal.isJsonPrimitive()) {
			listDoc.add(docVal.toString());
		}
		
		List<String> listOperator = new ArrayList<String>();
		if(operatorVal.isJsonArray()) {
			for(int i = 0; i < operatorVal.getAsJsonArray().size(); i++){
				listOperator.add(operatorVal.getAsJsonArray().get(i).toString());
			}
		}
		if(operatorVal.isJsonPrimitive()) {
			listOperator.add(operatorVal.toString());
		}
		/**
		 * Start compare
		 * 
		 * */
		if(operator.compareTo("$eq") == 0) {
			if(listOperator.size() == 1 && listDoc.size() == 1 && listOperator.get(0).compareTo(listDoc.get(0))==0) {
				documents.add(eachDoc);
			}
			return documents;
		}
		if(operator.compareTo("$ne") == 0) {
			if(listOperator.size() == 1 && listDoc.size() == 1 && listOperator.get(0).compareTo(listDoc.get(0))!=0) {
				documents.add(eachDoc);
			}
			return documents;
		}
		if(operator.compareTo("$gt") == 0) {
			if(listOperator.size() == 1 && listDoc.size() == 1 && listOperator.get(0).compareTo(listDoc.get(0))<0) {
				documents.add(eachDoc);
			}
			return documents;
		}
		if(operator.compareTo("$gte") == 0) {
			if(listOperator.size() == 1 && listDoc.size() == 1 && listOperator.get(0).compareTo(listDoc.get(0))<=0) {
				documents.add(eachDoc);
			}
			return documents;
		}
		if(operator.compareTo("$lt") == 0) {
			if(listOperator.size() == 1 && listDoc.size() == 1 && listOperator.get(0).compareTo(listDoc.get(0))>0) {
				documents.add(eachDoc);
			}
			return documents;
		}
		if(operator.compareTo("$lte") == 0) {
			if(listOperator.size() == 1 && listDoc.size() == 1 && listOperator.get(0).compareTo(listDoc.get(0))>=0) {
				documents.add(eachDoc);
			}
			return documents;
			
		}
		if(operator.compareTo("$in") == 0) {		
			for(int i =0;i<listDoc.size();i++) {
				if(listOperator.contains(listDoc.get(i))) {
					documents.add(eachDoc);
					
				}
			}
			return documents;
		}
		if(operator.compareTo("$nin") == 0) {
			int flag = 0;
			for(int i =0;i<listDoc.size();i++) {
				if(!(listOperator.contains(listDoc.get(i)))) {
					flag += 1;
					
				}
			}
			if(flag == listDoc.size()) documents.add(eachDoc);
			return documents;
		}
		return documents;
	}	
}
