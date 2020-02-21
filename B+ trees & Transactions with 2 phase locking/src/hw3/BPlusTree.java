package hw3;

import java.util.ArrayList;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;

public class BPlusTree {
	private Node root=null;
	private int innerDegree;
	private int leafDegree;
    
    public BPlusTree(int pInner, int pLeaf) {
    	//your code here
    	this.innerDegree = pInner;
    	this.leafDegree = pLeaf;
    }
    
    public LeafNode search(Field f) {
    	LeafNode leafNode = search(f, root);
    	for(int i =0; i < leafNode.getEntriesSize(); i++) {
    		if (f.compare(RelationalOperator.EQ, leafNode.getField(i))){
    			return leafNode;
    		}
    	}
    	return null;
    }

    public LeafNode search(Field f, Node node) {
    	if (node != null) {
	    	if (node.isLeafNode()) return (LeafNode) node;
	    	InnerNode innerNode = (InnerNode) node;
	    	if (f.compare(RelationalOperator.LTE, innerNode.getFirstKey())) {
	    		return search(f, innerNode.getFirstChild());
	    	} else if (f.compare(RelationalOperator.GT, innerNode.getLastKey())) {
	    		return search(f, innerNode.getLastChild());
	    	} else {
	    		int left = 0, right = innerNode.getKeysSize()-1;
	    		// find value ki such that ki < f <= ki+1
	    		while (left <= right) {
	    			int mid = left + (right-left)/2;
	    			if (f.compare(RelationalOperator.LTE, innerNode.getKey(mid))) {
	    				right = mid-1;
	    			} else {
	    				left = mid+1;
	    			}
	    		}
	    		return search(f, innerNode.getChild(left));
	    	}
    	}
    	return null;
    }
    
    public LeafNode searchForLeafNode(Field f) {
    	return search(f, root);
    }
    
    
    public void insert(Entry e) {
    	Field insertField = e.getField();
	    if(searchForLeafNode(insertField)!= null) {
	    	LeafNode tarNode = searchForLeafNode(insertField);
	    	ArrayList<Entry> tarEntries = tarNode.getEntries();
	    	
	    	if(tarNode.isFull() != true) tarNode.insert(e);
	    	else {
	    		ArrayList<Entry> newEntries = tarEntries;
	    		newEntries = this.addToEntries(e,newEntries);
	            /*
	             * If there are an even number of values, then after the split the two nodes should have an equal number of values 
	             * (half in the left, half in the right).
	             * If there are an odd number of values, the left child should contain one more value than the right child.
	             * */    		
	    		ArrayList<Entry> firstHalfEntries = new ArrayList<Entry>();
	    		for(int index = 0;index <= (newEntries.size() - 1)/2;index++) {
	    			firstHalfEntries.add(newEntries.get(index));
	    		}
	    		ArrayList<Entry> SecondHalfEntries = new ArrayList<Entry>();
	    		for(int index = (newEntries.size() - 1)/2 + 1;index < newEntries.size();index++) {
	    			SecondHalfEntries.add(newEntries.get(index));
	    		}
	    		
	    		tarNode.resetEntries(SecondHalfEntries);
	    		LeafNode addonNode = new LeafNode(this.leafDegree);
	    		addonNode.resetEntries(firstHalfEntries);
	    		Field insertToParent = addonNode.getLastField();
	    		// insert into the parent
	    		if (tarNode == this.root) {
	    			InnerNode addRoot = new InnerNode(innerDegree);
	    			this.setRoot(addRoot);
	    			addRoot.insertKey(insertToParent);
	    			addRoot.addChildren(addonNode);
	    			addRoot.addChildren(tarNode);
	    			addonNode.resetParent(addRoot);
	    			tarNode.resetParent(addRoot);
	    		}
	    		else {
	    			InnerNode currentParent = tarNode.getParent();
	    			boolean parentState = currentParent.isFull();
	    			currentParent.insertKey(insertToParent);
	    			currentParent.addChildren(addonNode);
	    			addonNode.resetParent(currentParent);
		    		if(parentState == true) parentSplit(currentParent);}
	    	}
	    }
	    else {
	    LeafNode firstNode = new LeafNode(this.leafDegree);
	    firstNode.insert(e);
	    this.setRoot(firstNode);
	    }
    }
    
    public void parentSplit(InnerNode currentParent) {
    	// split the parent and add the middle key to its parent
		// repeat until a split is not needed
    	ArrayList<Field> FirstHalfKeys = new ArrayList<Field>();
		ArrayList<Field> SecondHalfKeys = new ArrayList<Field>();
		
		for(int index = 0;index < (currentParent.getKeysSize() - 1)/2;index++) {
			FirstHalfKeys.add(currentParent.getKey(index));
		}
		for(int index = (currentParent.getKeysSize() - 1)/2+1;index < currentParent.getKeysSize();index++) {
			SecondHalfKeys.add(currentParent.getKey(index));
		}
		
		ArrayList<Node> FirstHalfChildren = new ArrayList<Node>();
		ArrayList<Node> SecondHalfChildren = new ArrayList<Node>();
		// get the middle key
		Field splitInsertToParent = currentParent.getKey((currentParent.getKeysSize() - 1)/2);
		for(int i = 0;i <= FirstHalfKeys.size();i++) {
			FirstHalfChildren.add(currentParent.getChild(i));
		}
		for(int i = FirstHalfKeys.size()+1;i <= FirstHalfKeys.size() + SecondHalfKeys.size() + 1;i++) {
			SecondHalfChildren.add(currentParent.getChild(i));
		}
		currentParent.resetKeys(FirstHalfKeys);
		currentParent.resetChildren(FirstHalfChildren);
		
		InnerNode currentParentSecond = new InnerNode(innerDegree);
		currentParentSecond.resetKeys(SecondHalfKeys);
		currentParentSecond.resetChildren(SecondHalfChildren);
		
		if(currentParent.getFirstChild().isLeafNode()) {
			for(int i =0;i<SecondHalfChildren.size();i++) {
				((LeafNode) SecondHalfChildren.get(i)).resetParent(currentParentSecond);
			}
		}
		else {
			for(int i =0;i<SecondHalfChildren.size();i++) {
				((InnerNode) SecondHalfChildren.get(i)).resetParent(currentParentSecond);
			}
		}

		//split the current parent and add a new root
		if(currentParent == this.root) {
			InnerNode rootNew = new InnerNode(innerDegree);
			rootNew.insertKey(splitInsertToParent);
			rootNew.addChildren(currentParent);
			rootNew.addChildren(currentParentSecond);
			currentParent.resetParent(rootNew);
			currentParentSecond.resetParent(rootNew);
			this.root = rootNew;
			return;
		}
		else {
			InnerNode upperParent = currentParent.getParent();
			boolean upperParentState = upperParent.isFull();
			upperParent.insertKey(splitInsertToParent);
			if (upperParentState == true) parentSplit(upperParent);
			return;
		}
    }
    
    
    public ArrayList<Entry> addToEntries(Entry e, ArrayList<Entry> tarEntries) {
    	int size = tarEntries.size();
    	for(int index=0;index<size;index++) {
			if(tarEntries.get(index).getField().compare(RelationalOperator.GT,e.getField())) {
				tarEntries.add(index, e);
				break;
			}
			if(index == size-1 ) tarEntries.add(e);
		}
    	return tarEntries;
    }
    
    public void delete(Entry e) {
    	//your code here
    	Field deleteField = e.getField();
	    if(search(deleteField) == null) return;
	    LeafNode tarNode = searchForLeafNode(deleteField);
	    
	    if(tarNode == this.root && tarNode.getEntriesSize() == 1)
	    {
	    	this.setRoot(null);
	    	return;
	    }
	    // if more than half full,remove entry and done
	    if(tarNode.isMoreThanHalfFull()) {
	    	tarNode.delete(e);
	    	return;
	    }
	    else {
	    	InnerNode tarParent = tarNode.getParent();
	    	LeafNode sibling = this.findSibling(tarNode);
	    	int tarIndex = tarParent.getChildIndex(tarNode);
	    	if(sibling.isMoreThanHalfFull()) {
	    		//updateParent
	    		if(tarIndex == 0) {
	    			// borrow from right sibling
	    			Entry borrow = sibling.getFirstEntry();
		    		//take an entry from sibling
		    		tarNode.insert(borrow);
		    		tarNode.delete(e);
		    		sibling.delete(borrow);
	    			Field updateKeyField = tarParent.getFirstKey();
	    			if(tarNode.getLastField().compare(RelationalOperator.LT, updateKeyField)){
		    			tarParent.setKeyFromIndex(0, tarNode.getLastField());
		    		}
		    		return;
	    		}
	    		else {
	    			// borrow from left sibling
	    			Entry borrow = sibling.getLastEntry();
		    		//take an entry from sibling
		    		tarNode.insert(borrow);
		    		tarNode.delete(e);
		    		sibling.delete(borrow);
		    		//the i-th child corresponds to the (i-1)-th key
		    		Field updateKeyField = tarParent.getKey(tarIndex-1);
		    		if(sibling.getLastField().compare(RelationalOperator.LT, updateKeyField)){
		    			tarParent.setKeyFromIndex(tarIndex-1, sibling.getLastField());
		    		}
		    		return;
	    		}
	    	}
	    	//merge with sibling
	    	ArrayList<Entry> tarNodeEntries = tarNode.getEntries();
	    	for(int i = 0;i < tarNodeEntries.size();i++) {
	    		  sibling.insert(tarNodeEntries.get(i));
	    	}
	    	sibling.delete(e);
	    	//delete entry from parent of removed node
	    	tarParent.deleteChild(tarNode);
	    	tarNode.resetParent(null);

	    	if(tarParent == this.root) {
	    		if(tarParent.getKeysSize() == 1) {
	    			this.root = sibling;
	    			return;
	    		}
	    		else {
	    			if(tarIndex == 0) {
	    				tarParent.deleteKey(0);
	    			}
	    			else {
	    			tarParent.deleteKey(tarIndex-1);
	    			}
	    			return;
	    		}
	    	}
	    	if(tarIndex == 0) {
	    		tarParent.setKeyFromIndex(0, sibling.getLastField());
	    	}
	    	else {
	    		tarParent.setKeyFromIndex(tarIndex-1, sibling.getLastEntry().getField());
	    	}
	    	// if parent node is no less than half, return. Else push through
	    	if(tarParent.getKeysSize()>=(tarParent.getDegree()+1)/2) {
	    		return;
	    	}
	    	
	    	InnerNode innerNode = tarParent;
	    	/*
	    	 * push through
	    	 */
	    	InnerNode innerParent = innerNode.getParent();
	    	
	    	InnerNode innerSibling = this.findSibling(innerNode);
	    	ArrayList<Node> siblingChildren = innerSibling.getChildren();
	    	ArrayList<Node> InnerChildren = innerNode.getChildren();
	    	//combine two arrays of nodes
			ArrayList<Node> combineChildren = new ArrayList<Node>();
			for (int i =0;i<siblingChildren.size();i++) {
				combineChildren.add((Node) siblingChildren.get(i));
			}
			for (int i =0;i<InnerChildren.size();i++) {
				combineChildren.add((Node) InnerChildren.get(i));
			}
			
			ArrayList<Node> innerNewChildren = new ArrayList<Node>();
			ArrayList<Node> sibilngNewChildren = new ArrayList<Node>();
			int innerIndex = innerParent.getChildIndex(innerNode);
			
	    	if(innerSibling.isMoreThanHalfFull()) {
	    		//grab value from sibling
	    		if(innerIndex == 0) {
	    			//sibling on the right
	    			Field keyInsertNode = innerParent.getFirstKey();
	        		innerNode.insertKey(keyInsertNode);
	        		innerParent.deleteKey(0);
	        		Field keyInsertParent = innerSibling.getFirstKey();
	        		innerParent.insertKey(keyInsertParent);
	        		innerSibling.deleteKey(0);
	        		int siblingKeysSize = innerSibling.getKeysSize();
	    			int InnerKeysSize = innerNode.getKeysSize();
	        		//update pointers
	        		for (int i = 0; i<=InnerKeysSize; i++) {
	        			innerNewChildren.add(combineChildren.get(i));
	        		}
	        		for (int i = InnerKeysSize+1; i<=InnerKeysSize+siblingKeysSize+1; i++) {
	        			sibilngNewChildren.add(combineChildren.get(i));
	        		}
	    		}
	    		else {
	    			Field keyInsertNode = innerParent.getKey(innerIndex-1);
	        		
	    			innerNode.deleteKey(innerIndex-1);
	    			innerNode.insertKey(keyInsertNode);
	    			
	        		innerParent.deleteKey(innerIndex-1);
	        		Field keyInsertParent = innerSibling.getLastKey();
	        		innerParent.insertKey(keyInsertParent);
	        		innerSibling.deleteKey(innerSibling.getKeysSize()-1);
	        		int siblingKeysSize = innerSibling.getKeysSize();
	    			int InnerKeysSize = innerNode.getKeysSize();
	        		//update pointers
	        		for (int i = 0; i<=siblingKeysSize; i++) {
	        			sibilngNewChildren.add(combineChildren.get(i));
	        		}
	        		
	        		for (int i = siblingKeysSize+1; i<=InnerKeysSize+siblingKeysSize+1; i++) {
	        			innerNewChildren.add(combineChildren.get(i));
	        		}
	        		
	    		}
	    		innerSibling.resetChildren(sibilngNewChildren);
	    		innerNode.resetChildren(innerNewChildren);
	    		
	    		if(innerNode.getFirstChild().isLeafNode()) {
	    			for(int i =0;i<innerNewChildren.size();i++) {
	    				((LeafNode) innerNewChildren.get(i)).resetParent(innerNode);
	    			}
	    		}
	    		else {
	    			for(int i =0;i<innerNewChildren.size();i++) {
	    				((InnerNode) innerNewChildren.get(i)).resetParent(innerNode);
	    			}
	    		}
	    		return;
	    	}
	    	else {
	    		//parent merge
	    		if(innerIndex == 0) {
	    			Field keyInsertNode = innerParent.getKey(0);
	    			innerNode.insertKey(keyInsertNode);
	        		innerParent.deleteKey(0);
	    		}
	    		else {
	    			Field keyInsertNode = innerParent.getKey(innerIndex-1);
	    			innerNode.deleteKey(innerIndex-1);
	        		innerNode.insertKey(keyInsertNode);
	        		innerParent.deleteKey(innerIndex-1);
	        		
	    		}
	    		int siblingKeysSize = innerSibling.getKeysSize();
	    		int nodeKeysSize = innerNode.getKeysSize();
	    		//merge with sibling
		    	ArrayList<Field> innerNodeKeys = innerNode.getKeys();
		    	for(int i = 0;i < innerNodeKeys.size();i++) {
		    		 innerSibling.insertKey(innerNodeKeys.get(i));
		    	}
				
		    	innerSibling.resetChildren(combineChildren);
		    	
	    		if(innerSibling.getFirstChild().isLeafNode()) {
	    			for(int i =siblingKeysSize+1;i<siblingKeysSize+nodeKeysSize+1;i++) {
	    				((LeafNode) combineChildren.get(i)).resetParent(innerSibling);
	    			}
	    		}
	    		else {
	    			for(int i =0;i<innerNewChildren.size();i++) {
	    				((InnerNode) combineChildren.get(i)).resetParent(innerSibling);
	    			}
	    		}
		    	innerParent.deleteChild(innerNode);
		    	innerNode.resetParent(null);
		    	if (innerParent.isNull()) {
	    			this.root = innerSibling;
	    		}
	    	}
	    }
    }
    
    public LeafNode findSibling(LeafNode tarNode) {
    	if (tarNode.getParent().getChildrenSize()== 1) return null;
	    int findIndex = 0; 
	    for(int i = 0;i< tarNode.getParent().getChildrenSize();i++) {
	    	LeafNode curLeaf =(LeafNode) tarNode.getParent().getChild(i);
	    	if(tarNode.getFirstEntry().getField().compare(RelationalOperator.EQ, curLeaf.getFirstEntry().getField())) {
	    		findIndex = i;
	    		break;
	    	}
	    }
	    if (findIndex == 0) {
			return (LeafNode) tarNode.getParent().getChild(1);
		}
		else {
			return (LeafNode) tarNode.getParent().getChild(findIndex - 1);	
		}
	}
    
    public Node getRoot() {
    	//your code here
    	return this.root;
    }
    
    public void setRoot(Node node) {
		this.root = node;
	}
    
    public InnerNode findSibling(InnerNode tarNode) {
    	if (tarNode.getParent().getChildrenSize()== 1) return null;
	    int findIndex = 0; 
	    for(int i = 0;i< tarNode.getParent().getChildrenSize();i++) {
	    	InnerNode curLeaf =(InnerNode) tarNode.getParent().getChild(i);
	    	if(tarNode.getFirstKey().compare(RelationalOperator.EQ, curLeaf.getFirstKey())) {
	    		findIndex = i;
	    		break;
	    	}
	    }
	    if (findIndex == 0) {
			return (InnerNode) tarNode.getParent().getChild(1);
		}
		else {
			return (InnerNode) tarNode.getParent().getChild(findIndex - 1);	
		}
	}
}
