package test;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import hw5.Document;

class DocumentTester {
	
	/*
	 * Things to consider testing:
	 * 
	 * Invalid JSON
	 * 
	 * Properly parses embedded documents
	 * Properly parses arrays
	 * Properly parses primitives (done!)
	 * 
	 * Object to embedded document
	 * Object to array
	 * Object to primitive
	 */
	
	@Test
	public void testParsePrimitive() {
		String json = "{ \"key\":\"value\" }";//setup
		JsonObject results = Document.parse(json); //call method to be tested
		assertTrue(results.getAsJsonPrimitive("key").getAsString().equals("value")); //verify results
	}
	
	@Test
	public void testInvalidJson() {
		String json = "{ 'key''value' }";//setup
		try {
			Document.parse(json);
			fail();
		} catch (Exception e) {
		}
	}
	@Test
	public void testParseArrays() {
		String json = "{\r\n" + 
				"    \"employees\": [\r\n" + 
				"        {\"firstName\": \"John\", \"lastName\": \"Doe\"}, \r\n" + 
				"        {\"firstName\": \"Anna\", \"lastName\": \"Smith\"}, \r\n" + 
				"        {\"firstName\": \"Peter\", \"lastName\": \"Jones\"}\r\n" + 
				"    ],\r\n" + 
				"    \"manager\": [\r\n" + 
				"        {\"firstName\": \"John\", \"lastName\": \"Doe\"}, \r\n" + 
				"        {\"firstName\": \"Anna\", \"lastName\": \"Smith\"}, \r\n" + 
				"        {\"firstName\": \"Peter\", \"lastName\": \"Jones\"}\r\n" + 
				"    ]\r\n" + 
				"}";//setup
		JsonObject results = Document.parse(json); //call method to be tested
		JsonArray jsonArray = results.getAsJsonArray("manager");
		JsonObject john = jsonArray.get(0).getAsJsonObject();
		assertTrue(john.getAsJsonPrimitive("firstName").getAsString().equals("John"));
	}
	
	@Test
	public void testParseEmbeddedDocuments() {
		String json = "{\"id\":1,\r\n" + 
				"        \"name\":\"abc\",\r\n" + 
				"        \"address\": {\"streetName\":\"cde\",\r\n" + 
				"                    \"streetId\":2\r\n" + 
				"                    }\r\n" + 
				"        }";//setup
		JsonObject results = Document.parse(json); //call method to be tested
		assertTrue(results.getAsJsonObject("address").getAsJsonPrimitive("streetId").getAsInt()==2);
	}
	@Test
	public void testToString() {
		String json = "{\"id\":1,\"name\":\"abc\",\"address\":{\"streetName\":\"cde\",\"streetId\":2}}";//setup
		JsonObject results = Document.parse(json); //call method to be tested
		assertTrue(Document.toJsonString(results).equals(json));
	}
}
