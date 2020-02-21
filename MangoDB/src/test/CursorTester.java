package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import hw5.DB;
import hw5.DBCollection;
import hw5.DBCursor;
import hw5.Document;

class CursorTester {
	
	/**
	 * Things to consider testing:
	 * 
	 * hasNext (done?)
	 * count (done?)
	 * next (done?)
	 * @throws IOException 
	 */

	@Test
	public void testFindAll() throws IOException {
		DB db = new DB("data");
		DBCollection test = db.getCollection("test");
		DBCursor results = test.find();
		
		assertTrue(results.count() == 3);
		assertTrue(results.hasNext());
		JsonObject d1 = results.next(); //pull first document
		//verify contents?
		assertTrue(results.hasNext());//still more documents
		JsonObject d2 = results.next(); //pull second document
		//verfiy contents?
		assertTrue(results.hasNext()); //still one more document
		JsonObject d3 = results.next();//pull last document
		assertFalse(results.hasNext());//no more documents
	}
	@Test
	public void testQueryAllKinds() throws IOException   {
		DB db = new DB("data");
		DBCollection test = db.getCollection("test");
		DBCursor results = test.find(Document.parse("{\"key\":\"value\"}"));
		assertTrue(results.count() == 1);
		assertTrue(results.hasNext());
		results.next();
		assertTrue(!results.hasNext());
	    results = test.find(Document.parse("{embedded.key2 : value2}"));
		assertTrue(results.count() == 1);
		assertTrue(results.hasNext());
		results.next();
		assertTrue(!results.hasNext());
	    results = test.find(Document.parse("{\"array\":[\"one\",\"two\",\"three\"]}"));
		assertTrue(results.count() == 1);
		assertTrue(results.hasNext());
		results.next();
		assertTrue(!results.hasNext());
	}
	@Test
	public void testEmbeddedArray() throws IOException {
		DB db = new DB("data");
		DBCollection test = db.getCollection("test1");
		DBCursor results = test.find(Document.parse("{embedded.key:[1,2,\"three\"]}"));
		assertTrue(results.count() == 1);
		assertTrue(results.hasNext());
		results.next();
		assertTrue(!results.hasNext());
	}
	@Test
	public void testAllOperators() throws IOException {
		DB db = new DB("data");
		DBCollection test = db.getCollection("test2");
		DBCursor results = test.find(Document.parse("{key : {$eq : 2}}"));
		assertTrue(results.count() == 1);
		assertTrue(results.hasNext());
		results.next();
		assertTrue(!results.hasNext());
		results = test.find(Document.parse("{key : {$gt : 2}}"));
		assertTrue(results.count() == 1);
		assertTrue(results.hasNext());
		results.next();
		assertTrue(!results.hasNext());
		results = test.find(Document.parse("{key : {$gte : 2}}"));
		assertTrue(results.count() == 2);
		results = test.find(Document.parse("{key : {$lt : 2}}"));
		assertTrue(results.count() == 1);
		assertTrue(results.hasNext());
		results.next();
		assertTrue(!results.hasNext());
		results = test.find(Document.parse("{key : {$lte : 1}}"));
		assertTrue(results.count() == 1);
		assertTrue(results.hasNext());
		results.next();
		assertTrue(!results.hasNext());
		results = test.find(Document.parse("{key : {$ne : 3}}"));
		assertTrue(results.count() == 2);
		results = test.find(Document.parse("{key : {$nin : [1,2]}}"));
		assertTrue(results.count() == 1);
		assertTrue(results.hasNext());
		results.next();
		assertTrue(!results.hasNext());
		results = test.find(Document.parse("{key : {$in : [1,5]}}"));
		assertTrue(results.count() == 1);
		assertTrue(results.hasNext());
		results.next();
		assertTrue(!results.hasNext());
	}
	
}
