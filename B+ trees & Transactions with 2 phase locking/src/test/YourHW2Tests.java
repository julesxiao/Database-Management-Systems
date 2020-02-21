package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapFile;
import hw1.IntField;
import hw1.RelationalOperator;
import hw1.TupleDesc;
import hw2.AggregateOperator;
import hw2.Query;
import hw2.Relation;

public class YourHW2Tests {

	private HeapFile testhf;
	private TupleDesc testtd;
	private HeapFile ahf;
	private TupleDesc atd;
	private Catalog c;
	private HeapFile bhf;
	private TupleDesc btd;

	@Before
	public void setup() {
		
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(new File("testfiles/A.dat.bak").toPath(), new File("testfiles/A.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");
		
		int tableId = c.getTableId("test");
		testtd = c.getTupleDesc(tableId);
		testhf = c.getDbFile(tableId);
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/A.txt");
		
		tableId = c.getTableId("A");
		atd = c.getTupleDesc(tableId);
		ahf = c.getDbFile(tableId);
		
		c = Database.getCatalog();
		c.loadSchema("testfiles/B.txt");
		
		tableId = c.getTableId("B");
		btd = c.getTupleDesc(tableId);
		bhf = c.getDbFile(tableId);
	}
	
	@Test
	public void testTuplesAfterProjection() {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		ArrayList<Integer> c = new ArrayList<Integer>();
		c.add(1);
		ar = ar.project(c);
		assertTrue("TupleDesc of resulting tuples after projection should match TupleDesc of relation", ar.getDesc().getSize() == ar.getTuples().get(0).getDesc().getSize());
		assertTrue("Column name of resulting tuples after projection should match column name of TupleDesc", ar.getTuples().get(0).getFieldName(0).equals("a2"));
		assertTrue("Value of resulting tuples after projection should remain the same", ar.getTuples().get(0).getField(0).toString().equals("1"));

	}
	@Test
	public void testEmptyResultAfterSelect() {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		ar = ar.select(0, RelationalOperator.EQ, new IntField(531));
		
		assertTrue("Should be 0 tuples after select operation", ar.getTuples().size() == 0);
		assertTrue("select operation does not change tuple description", ar.getDesc().equals(atd));
	}
	@Test
	public void testSelectOnAllOperators() {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		Relation arEQ = ar.select(0, RelationalOperator.EQ, new IntField(3));
		assertTrue("Should be 1 tuple after select operation", arEQ.getTuples().size() == 1);
		
		Relation arGT = ar.select(0, RelationalOperator.GT, new IntField(1));
		assertTrue("Should be 7 tuples after select operation", arGT.getTuples().size() == 7);
		
		Relation arGTE = ar.select(0, RelationalOperator.GTE, new IntField(1));
		assertTrue("Should be 8 tuples after select operation", arGTE.getTuples().size() == 8);
		
		Relation arLT = ar.select(0, RelationalOperator.LT, new IntField(2));
		assertTrue("Should be 1 tuple after select operation", arLT.getTuples().size() == 1);
		
		Relation arLTE = ar.select(0, RelationalOperator.LTE, new IntField(2));
		assertTrue("Should be 2 tuples after select operation", arLTE.getTuples().size() == 2);
		
		Relation arNOTEQ = ar.select(0, RelationalOperator.NOTEQ, new IntField(1));
		assertTrue("Should be 7 tuples after select operation", arNOTEQ.getTuples().size() == 7);
	}
	
	@Test
	public void testMutipleSelect() {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		ar = ar.select(0, RelationalOperator.NOTEQ, new IntField(3));
		assertTrue("Should be 7 tuples after select operation", ar.getTuples().size() == 7);
		
		ar = ar.select(0, RelationalOperator.LT, new IntField(530));
		assertTrue("Should be 2 tuples after select operation", ar.getTuples().size() == 2);
	}
	
	@Test
	public void testMultipleJoin() {
		Relation tr = new Relation(testhf.getAllTuples(), testtd);
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		tr = tr.join(ar, 0, 0);
		assertTrue("There should be 5 tuples after the join", tr.getTuples().size() == 5);
		assertTrue("The size of the tuples should reflect the additional columns from the join", tr.getDesc().getSize() == 141);
		
		Relation br = new Relation(bhf.getAllTuples(), btd);
		tr = tr.join(br, 0, 0);
		assertTrue("There should be 25 tuples after the join", tr.getTuples().size() == 25);
		assertTrue("The size of the tuples should reflect the additional columns from the join", tr.getDesc().getSize() == 149);
	}
	
	@Test
	public void testAggregateAllOperators() {
		Relation ar = new Relation(ahf.getAllTuples(), atd);
		ArrayList<Integer> c = new ArrayList<Integer>();
		c.add(1);
		ar = ar.project(c);
		
		Relation arSUM = ar.aggregate(AggregateOperator.SUM, false);
		assertTrue("The result of an aggregate should be a single tuple", arSUM.getTuples().size() == 1);
		IntField aggSUM = (IntField) arSUM.getTuples().get(0).getField(0);
		assertTrue("The sum of these values was incorrect", aggSUM.getValue() == 36);
		
		Relation arAVG = ar.aggregate(AggregateOperator.AVG, false);
		assertTrue("The result of an aggregate should be a single tuple", arAVG.getTuples().size() == 1);
		IntField aggAVG = (IntField) arAVG.getTuples().get(0).getField(0);
		assertTrue("The sum of these values was incorrect", aggAVG.getValue() == 36/8);
		
		Relation arMIN = ar.aggregate(AggregateOperator.MIN, false);
		assertTrue("The result of an aggregate should be a single tuple", arMIN.getTuples().size() == 1);
		IntField aggMIN = (IntField) arMIN.getTuples().get(0).getField(0);
		assertTrue("The sum of these values was incorrect", aggMIN.getValue() == 1);
		
		Relation arMAX = ar.aggregate(AggregateOperator.MAX, false);
		assertTrue("The result of an aggregate should be a single tuple", arMAX.getTuples().size() == 1);
		IntField aggMAX = (IntField) arMAX.getTuples().get(0).getField(0);
		assertTrue("The sum of these values was incorrect", aggMAX.getValue() == 8);
	}
	
	@Test
	public void testQueryAggregateAllOperators() {
		Query qSUM = new Query("SELECT SUM(a2) FROM A");
		Relation rSUM = qSUM.execute();
		assertTrue("Aggregations should result in one tuple",rSUM.getTuples().size() == 1);
		IntField aggSUM = (IntField) rSUM.getTuples().get(0).getField(0);
		assertTrue("Result of sum aggregation is 36", aggSUM.getValue() == 36);
		
		Query qAVG = new Query("SELECT AVG(a2) FROM A");
		Relation rAVG = qAVG.execute();
		assertTrue("The result of an aggregate should be a single tuple", rAVG.getTuples().size() == 1);
		IntField aggAVG = (IntField) rAVG.getTuples().get(0).getField(0);
		assertTrue("The sum of these values was incorrect", aggAVG.getValue() == 36/8);
		
		Query qMIN = new Query("SELECT MIN(a2) FROM A");
		Relation rMIN = qMIN.execute();
		assertTrue("The result of an aggregate should be a single tuple", rMIN.getTuples().size() == 1);
		IntField aggMIN = (IntField) rMIN.getTuples().get(0).getField(0);
		assertTrue("The sum of these values was incorrect", aggMIN.getValue() == 1);
	
		Query qMAX = new Query("SELECT MAX(a2) FROM A");
		Relation rMAX = qMAX.execute();
		assertTrue("The result of an aggregate should be a single tuple", rMAX.getTuples().size() == 1);
		IntField aggMAX = (IntField) rMAX.getTuples().get(0).getField(0);
		assertTrue("The sum of these values was incorrect", aggMAX.getValue() == 8);
		
		
	}
	
	@Test
	public void testRename() {
		Query q = new Query("SELECT a1 AS b FROM A");
		Relation r = q.execute();
		
		assertTrue(r.getDesc().getFieldName(0).equals("b"));

	}
	

}
