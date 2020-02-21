package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.Before;
import org.junit.Test;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapFile;
import hw1.HeapPage;
import hw1.IntField;
import hw1.StringField;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;

public class YourUnitTests {
	
	private HeapFile hf;
	private TupleDesc td;
	private Catalog c;
	private HeapPage hp;

	@Before
	public void setup() {
		
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");
		
		int tableId = c.getTableId("test");
		td = c.getTupleDesc(tableId);
		hf = c.getDbFile(tableId);
		hp = hf.readPage(0);
	}
	
	//two tests added for TupleDescTest (testNumFields and testGetFieldName)
		
	//Below are tests for HeapPage:
	@Test
	public void testAddTupleToFullPage() {
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;
		t.setField(1, new StringField(s));
		for (int i = 0; i < 29; i++) {
			try {
				hp.addTuple(t);
			} catch (Exception e) {
				fail("error when adding valid tuple");
				e.printStackTrace();
			}
		}
		try {
			hp.addTuple(t);
			fail("cannot add tuple when page is full");
		} catch (Exception e) {
			
		}
	}
	
	@Test
	public void testAddTupleWithWrongDesc() {
		TupleDesc td = new TupleDesc(new Type[]{Type.STRING, Type.STRING}, new String[]{"", ""});
		Tuple t = new Tuple(td);
		try {
			hp.addTuple(t);
			fail("cannot add tuples that do not have the same structure as the tuples within the page");
		} catch (Exception e) {
			
		}
	}
	
	@Test
	public void testDeleteFromEmptySlot() {
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, (byte)2, (byte)18}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 0x68;
		s[2] = 0x69;
		t.setField(1, new StringField(s));
		try {
			hp.deleteTuple(t);
		} catch (Exception e) {
			fail("error when deleting valid tuple");
			e.printStackTrace();
		}
		try {
			hp.deleteTuple(t);
			fail("cannot delete tuple when tuple slot is already empty");
		} catch (Exception e) {
			
		}
		
	}
	
	@Test
	public void testDeleteTupleFromAnotherPage() {
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, (byte)2, (byte)18}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 0x68;
		s[2] = 0x69;
		t.setField(1, new StringField(s));
		t.setPid(1);
		try {
			hp.deleteTuple(t);
			fail("cannot delete tuple that belongs to another page");
		} catch (Exception e) {
			
		}
		
	}

	//Below are test for HeapFile:
	@Test
	public void testWriteToFullPage() {
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, 0, (byte)131}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 98;
		s[2] = 121;
		t.setField(1, new StringField(s));
		for (int i = 0; i < 29; i++) {
			try {
				hf.addTuple(t);
			} catch (Exception e) {
				fail("error when adding valid tuple");
				e.printStackTrace();
			}
		}
		try {
			hf.addTuple(t);
			assertTrue(hf.getNumPages() == 2);
		} catch (Exception e) {
			fail("error when adding valid tuple");
			e.printStackTrace();

		}
	}
	@Test
	public void testRemoveFromEmptyFile() {
		Tuple t = new Tuple(td);
		t.setField(0, new IntField(new byte[] {0, 0, (byte)2, (byte)18}));
		byte[] s = new byte[129];
		s[0] = 2;
		s[1] = 0x68;
		s[2] = 0x69;
		t.setField(1, new StringField(s));
		
		try {
			hf.deleteTuple(t);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unable to delete tuple");
		}
		
		assertTrue(hf.getAllTuples().size() == 0);
		
		try {
			hf.deleteTuple(t);
			fail("cannot delete tuple when page is already empty");
		} catch (Exception e) {
		}
	}

	


}
