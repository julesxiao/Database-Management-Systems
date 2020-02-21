package hw3;

import java.util.ArrayList;
import java.util.Comparator;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;
import hw1.Type;

public class LeafNode implements Node {
	
	private int degree;
	private ArrayList<Entry> entries = new ArrayList<Entry>();
	private LeafNode next;
	private InnerNode parent=null;
	
	public LeafNode(int degree) {
		this.degree = degree;
		this.entries = new ArrayList<>();
	}
	
	public ArrayList<Entry> getEntries() {
		//your code here
		return this.entries;
	}

	public int getDegree() {
		//your code here
		return this.degree;
	}
	
	public boolean isLeafNode() {
		return true;
	}
	public void insert(Entry e) {
		this.entries.add(e);
		this.keepSorted();
	}
	public void delete(Entry e) {
		for(int i =0; i < this.getEntriesSize();i++) {
    		if(this.getField(i).compare(RelationalOperator.EQ, e.getField())){
    			this.entries.remove(i);
    		}
    	}
	}
	public void keepSorted() {
		this.entries.sort(new Comparator<Entry>() {
			@Override
			public int compare(Entry o1, Entry o2) {
				boolean isInt = o1.getField().getType().equals(Type.INT);
				if (isInt) {
					int i1 = ((IntField) o1.getField()).getValue();
					int i2 = ((IntField) o2.getField()).getValue();
					return i1-i2;
				} else {
					String s1 = new String(o1.getField().toByteArray());
					String s2 = new String(o2.getField().toByteArray());
					return s2.compareTo(s1);
				}
			}
		});
	}
	public boolean isFull() {
		if(this.entries.size() == this.degree) return true;
		return false;
	}
	public void resetEntries(ArrayList<Entry> newEntries) {
		//your code here
		this.entries = newEntries;
	}
	public InnerNode getParent() {
		return this.parent;
	}
	public void resetParent(InnerNode newParent) {
		this.parent = newParent;
	}
	public int getEntriesSize() {
		return this.entries.size();
	}
	public Field getLastField() {
		if (this.getEntriesSize()==0) throw null;
		return this.getEntries().get(getEntriesSize()-1).getField();
	}
	public Field getField(int index) {
		return this.entries.get(index).getField();
	}
	public boolean isMoreThanHalfFull() {
		if(this.getEntriesSize() > (this.degree+1)/2) return true;
		return false;
	}

	public Entry getLastEntry() {
		if (this.getEntriesSize() == 0) throw null;
		return this.getEntries().get(getEntriesSize()-1);
	}
	
	public Entry getFirstEntry() {
		if (this.getEntriesSize() == 0) throw null;
		return this.getEntries().get(0);
	}
}