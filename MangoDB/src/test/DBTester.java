package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import hw5.DB;
import hw5.DBCollection;
import hw5.Document;

class DBTester {
	
	/**
	 * Things to consider testing:
	 * 
	 * Properly creates directory for new DB (done)
	 * Properly accesses existing directory for existing DB
	 * Properly accesses collection
	 * Properly drops a database
	 * Special character handling?
	 * @throws IOException 
	 */
	
	@Test
	public void testCreateDB() throws IOException {
		DB hw5 = new DB("hw5"); //call method
		assertTrue(new File("testfiles/hw5").exists()); //verify results
	}
	@Test
	void testDrop() throws IOException {
		DB hw5 = new DB("hw5");
		assertTrue(new File("testfiles/hw5").exists());
		hw5.dropDatabase();
		assertTrue(!new File("testfiles/hw5").exists());
	}
	
}
