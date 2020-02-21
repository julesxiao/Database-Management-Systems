package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import hw5.DB;
import hw5.DBCollection;
import hw5.DBCursor;
import hw5.Document;

class CollectionTester {

	/**
	 * Things to consider testing
	 * 
	 * Queries:
	 * 	Find all
	 * 	Find with relational select
	 * 		Conditional operators
	 * 		Embedded documents
	 * 		Arrays
	 * 	Find with relational project
	 * 
	 * Inserts
	 * Updates
	 * Deletes
	 * 
	 * getDocument (done?)
	 * drop
	 * @throws IOException 
	 */
	public void afterTest() throws IOException {
		String json = "{\"key\":\"value\"}";
		String json2 = "{\"embedded\":{\"key2\":\"value2\"}}";
		String json3 = "{\"array\":[\"one\",\"two\",\"three\"]}";
		DB db = new DB("testcollection");
		DBCollection test = db.getCollection("test");
		test.drop();
		test.insert(Document.parse(json));
		test.insert(Document.parse(json2));
		test.insert(Document.parse(json3));

	}
	@Test
	public void testGetDocument() throws IOException {
		DB db = new DB("data");
		DBCollection test = db.getCollection("test");
		JsonObject primitive = test.getDocument(0);
		assertTrue(primitive.getAsJsonPrimitive("key").getAsString().equals("value"));
	}
	@Test
	public void testGetDocumentAllTypes() throws IOException {	
		DB db = new DB("data");
		DBCollection test = db.getCollection("test");
		JsonObject primitive = test.getDocument(0);
		assertTrue(primitive.getAsJsonPrimitive("key").getAsString().equals("value"));

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("key2", "value2");
		assertTrue(test.getDocument(1).getAsJsonObject("embedded").getAsJsonObject().equals(jsonObject));

		JsonArray jsonArray = new JsonArray();
		jsonArray.add("one");
		jsonArray.add("two");
		jsonArray.add("three");
		assertTrue(test.getDocument(2).getAsJsonArray("array").equals(jsonArray));
	}

	@Test
	public void testRemove() throws IOException {
		DB db = new DB("testcollection");
		DBCollection test = db.getCollection("test");
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("key", "value");
		DBCursor result = test.find(jsonObject);
		assertTrue(result.count()==1);
		test.remove(jsonObject, false);
		result = test.find(jsonObject);
		assertTrue(result.count()==0);
		afterTest();
	}
	@Test
	public void testRemoveMultiFalse() throws IOException {
		DB db = new DB("testcollection");
		DBCollection test = db.getCollection("test");
		JsonObject object = new JsonObject();
		object.addProperty("key", "value");
		test.insert(object);
		test.remove(object, false);
		DBCursor result = test.find(object);
		assertTrue(result.count()==1);
		assertTrue(result.hasNext());
		afterTest();

	}
	@Test
	public void testRemoveMultiTrue() throws IOException {
		DB db = new DB("testcollection");
		DBCollection test = db.getCollection("test");
		JsonObject object = new JsonObject();
		object.addProperty("key", "value");
		test.insert(object);
		test.remove(object, true);
		DBCursor result = test.find(object);
		assertTrue(result.count()==0);
		assertTrue(!result.hasNext());
		afterTest();
	}
	@Test
	public void testDrop() throws IOException {
		DB db = new DB("testcollection");
		DBCollection test = db.getCollection("test");
		test.drop();
		assertTrue(!new File("testfiles/testcollection/test.json").exists());
		afterTest();
	}
	@Test
	public void testInsert() throws IOException {
		DB db = new DB("testcollection");
		DBCollection test = db.getCollection("test");
		JsonObject object = new JsonObject();
		object.addProperty("key", "value");
		test.insert(object);
		DBCursor result = test.find();
		assertTrue(result.count() == 4);
		afterTest();
	}
	@Test
	public void testInsertMany() throws IOException {
		DB db = new DB("testcollection");
		DBCollection test = db.getCollection("test");
		JsonObject object = new JsonObject();
		object.addProperty("key", "value");
		JsonObject[] objects = {object, object, object};
		test.insert(objects);
		DBCursor result = test.find();
		assertTrue(result.count() == 6);
		result = test.find(object);
		assertTrue(result.count() == 4);
		afterTest();
	}
	@Test
	public void testNewCollection() throws IOException {
		DB db = new DB("testcollection");
		DBCollection collection = new DBCollection(db, "test1");
		assertTrue(new File("testfiles/testcollection/test1.json").exists());
		assertTrue(db.getCollection("test1").getName().equals("test1"));
	}
	@Test
	public void testUpdateMultiFalse() throws IOException {
		DB db = new DB("testcollection");
		DBCollection test = db.getCollection("test");
		JsonObject object = new JsonObject();
		object.addProperty("key", "value");
		JsonObject object2 = new JsonObject();
		object2.addProperty("key2", "value2");
		test.update(object, object2, false);
		DBCursor result = test.find(object);
		assertTrue(result.count()==0);
		result = test.find(object2);
		assertTrue(result.count()==1);
		afterTest();
	}
	@Test
	public void testUpdateMultiTrue() throws IOException {
		DB db = new DB("testcollection");
		DBCollection test = db.getCollection("test");
		JsonObject object = new JsonObject();
		object.addProperty("key", "value");
		test.insert(object);
		JsonObject object2 = new JsonObject();
		object2.addProperty("key2", "value2");
		test.update(object, object2, true);
		DBCursor result = test.find(object);
		assertTrue(result.count()==0);
		result = test.find(object2);
		assertTrue(result.count()==2);
		afterTest();
	}
	@Test
	public void testFindProjection() throws IOException {
		DB db = new DB("data");
		DBCollection test = db.getCollection("test2");
		DBCursor result = test.find(Document.parse("{key : {$gte : 2}}"),Document.parse("{\"value\" : 1}"));
		assertTrue(result.count()==2);
		assertTrue(result.next().toString().equals("{\"key\":2,\"value\":\"value2\"}"));
		assertTrue(result.next().toString().equals("{\"key\":3,\"value\":\"value3\"}"));
		result = test.find(Document.parse("{key : {$gte : 2}}"),Document.parse("{\"value\" : 0}"));
		assertTrue(result.count()==2);
		assertTrue(result.next().toString().equals("{\"key\":2}"));
		assertTrue(result.next().toString().equals("{\"key\":3}"));
	}
}
