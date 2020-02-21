package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import hw1.Field;
import hw1.IntField;
import hw3.BPlusTree;
import hw3.Entry;
import hw1.RelationalOperator;
import hw3.InnerNode;
import hw3.LeafNode;
import hw3.Node;

public class YourHW3Tests {

	@Test
	public void testInsertionSplitRoot() {
		BPlusTree bt = new BPlusTree(4, 3);
		bt.insert(new Entry(new IntField(9), 0));
		bt.insert(new Entry(new IntField(4), 0));
		bt.insert(new Entry(new IntField(12), 0));
//		4 9 12
		bt.insert(new Entry(new IntField(7), 0));
//		    7
//		  /  \
//		4 7  9 12
        //verify root properties
		Node root = bt.getRoot();
		assertTrue(root.isLeafNode() == false);

		InnerNode in = (InnerNode)root;

		ArrayList<Field> k = in.getKeys();
		ArrayList<Node> c = in.getChildren();

		assertTrue(k.get(0).compare(RelationalOperator.EQ, new IntField(7)));

		//grab left and right children from root
		LeafNode l = (LeafNode)c.get(0);
		LeafNode r = (LeafNode)c.get(1);

		assertTrue(l.isLeafNode());
		assertTrue(r.isLeafNode());

		//check values in left 
		
		assertTrue(l.getEntries().get(0).getField().equals(new IntField(4)));
		assertTrue(l.getEntries().get(1).getField().equals(new IntField(7)));
		
		//check values in right 
		assertTrue(r.getEntries().get(0).getField().equals(new IntField(9)));
		assertTrue(r.getEntries().get(1).getField().equals(new IntField(12)));
	}
	
	@Test
	public void testDeletionRootDeletion() {
		BPlusTree bt = new BPlusTree(3, 2);
		bt.insert(new Entry(new IntField(1), 0));
		bt.insert(new Entry(new IntField(5), 0));
		bt.insert(new Entry(new IntField(7), 0));
		bt.insert(new Entry(new IntField(8), 0));
		
//				5
//	  		  /   \
//		    1  5  7 8
		
		bt.delete(new Entry(new IntField(7), 0));
		bt.delete(new Entry(new IntField(8), 0));
//		         1
//		        / \
//			   1   5	
		bt.delete(new Entry(new IntField(1), 0));
//		         5
		
		Node root = bt.getRoot();
		assertTrue(root.isLeafNode());

		LeafNode l = (LeafNode)root;
		assertTrue(l.getEntries().get(0).getField().equals(new IntField(5)));
	}
}
