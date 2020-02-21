package hw3;

import java.util.ArrayList;
import java.util.Comparator;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;
import hw1.Type;

public class InnerNode implements Node {
	
	private int degree;
	private ArrayList<Field> keys = new ArrayList<Field>();
	private ArrayList<Node> children = new ArrayList<Node>();
	private InnerNode parent=null;

	public InnerNode(int degree) {
		//your code here
		this.degree = degree;
	}
	
	public ArrayList<Field> getKeys() {
		//your code here
		return this.keys;
	}
	public int getKeysSize() {
		return this.keys.size();
	}
	public int getChildrenSize() {
		return this.children.size();
	}
	public ArrayList<Node> getChildren() {
		//your code here
		return this.children;
	}
	public Field getKey(int index) {
		return this.keys.get(index);
	}
	public Node getChild(int index) {
		return this.children.get(index);
	}
	public Field getFirstKey() {
		if (this.getKeysSize()==0) return null;
		return this.getKeys().get(0);
	}
	public Node getFirstChild() {
		if (this.getChildrenSize()==0) return null;
		return this.getChildren().get(0);
	}
	public Node getLastChild() {
		if (this.getChildrenSize()==0) return null;
		return this.getChildren().get(getChildrenSize()-1);
	}
	public Field getLastKey() {
		if (this.getKeysSize()==0) throw null;
		return this.getKeys().get(getKeysSize()-1);
	}

	public int getDegree() {
		//your code here
		return this.degree;
	}
	
	public boolean isLeafNode() {
		return false;
	}
    
	public boolean isFull() {
		if(this.children.size() == this.degree) return true;
		return false;
	}
	
	public void insertKey(Field f) {
		boolean state = this.keys.isEmpty();
		this.keys.add(f);
		if(state!=true)	this.keepSorted();
	}
	public void keepSorted() {
		for(int i=1;i < this.getKeysSize();i++) {
			Field tempKey = this.getKey(i);
			int j = i - 1;
			while(j >= 0 && this.getKey(j).compare(RelationalOperator.GT, tempKey)) {
				this.keys.set(j+1, this.getKey(j));
				j = j -1;
			}
			this.keys.set(j+1, tempKey);
		}
	}
	public void addChildren(Node child) {
		if (this.getChildrenSize() == 0) {
			children.add(child);
			return;
		}
		this.addNodetoChildren(child);
		return;
	}
	
	public void deleteChild(Node child) {
		if(this.getChildrenSize() == 0)  return;
		children.remove(child);
	}
	
	public void resetKeys(ArrayList<Field> newKeys) {
		this.keys = newKeys;
	}
	public void resetParent(InnerNode newParent) {
		this.parent = newParent;
	}
	
	public void resetChildren(ArrayList<Node> newChildren) {
		this.children = newChildren;
	}

	public void resetChildrenInnerNode(ArrayList<InnerNode> newChildren) {
		if(this.getFirstChild().isLeafNode()!= true) {
			this.children = null;
			for(int i =0;i<newChildren.size();i++) {
				this.children.add(i, newChildren.get(i));}
		}
	}
	
	public void resetChildrenLeafNode(ArrayList<LeafNode> newChildren) {
		if(this.getFirstChild().isLeafNode()) {
			this.children = null;
			for(int i =0;i<newChildren.size();i++) {
				this.children.add(i, newChildren.get(i));}
		}
	}

	public boolean isNull() {
		if(this.keys.size() == 0) return true;
		return false;
	}
	public InnerNode getParent() {
		return this.parent;
	}
	public void addNodetoChildren(Node newNode) {
		if (this.children.isEmpty()!=true) {
			if (this.children.get(0).isLeafNode()!= true){
				InnerNode addOnChild = (InnerNode) newNode;
				int oriChildrensize = this.getChildrenSize();
				for(int i = 0; i < oriChildrensize; i++) {
					InnerNode tempChild = (InnerNode) this.getChild(i);
					if (tempChild.getLastKey().compare(RelationalOperator.GT, addOnChild.getLastKey())){
						this.children.add(i, addOnChild);
						break;
					}
					if (i == oriChildrensize-1) this.children.add(addOnChild);
				}
				return;
			}
			if (this.children.get(0).isLeafNode()) {
				LeafNode addOnChild = (LeafNode) newNode;
				int oriChildrensize = this.getChildrenSize();
				for(int i = 0; i < oriChildrensize; i++) {
					LeafNode tempChild = (LeafNode) this.getChild(i);
					if (tempChild.getLastField().compare(RelationalOperator.GT, addOnChild.getLastField())){
						this.children.add(i, addOnChild);
						break;
					}
					if (i == oriChildrensize-1) this.children.add(addOnChild);
				}
				return;
			}
		}
	}
	public int getChildIndex(Node child) {
		int index=-1;
		if(child.isLeafNode()){
			LeafNode newChild = (LeafNode) child;
			for(int i = 0;i<this.getChildrenSize();i++) {
				LeafNode currentChild = (LeafNode) this.getChild(i);
				if(currentChild.getLastField().compare(RelationalOperator.EQ, newChild.getLastField())){
					index = i;
					break;
				}
			}
			return index;
		}
		else {
			InnerNode newChild = (InnerNode) child;
			for(int i = 0;i<this.getChildrenSize();i++) {
				InnerNode currentChild = (InnerNode) this.getChild(i);
				if(currentChild.getFirstKey().compare(RelationalOperator.EQ, newChild.getFirstKey())){
					index = i;
					break;
				}
			}
			return index;
		}
	}
	
	public boolean setKeyFromIndex(int index, Field key) {
		if(index >= 0 && index < this.getKeysSize()) {
			this.keys.set(index, key);
			return true;
		}
		return false;
	}
	
	public void deleteKey(int index) {
		if (this.getKeysSize() != 0) this.keys.remove(index);
		return;
	}
	
	public boolean isMoreThanHalfFull() {
		if(this.getKeysSize() > this.degree/2) return true;
		return false;
	}
}