package hw1;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a tuple that will contain a single row's worth of information
 * from a table. It also includes information about where it is stored
 * @author Sam Madden modified by Doug Shook
 *
 */
public class Tuple {
	
	private TupleDesc tupleDesc;
	private int Pid;
	private int id;
	private Map<String, Field> map;
	
	/**
	 * Creates a new tuple with the given description
	 * @param t the schema for this tuple
	 */
	public Tuple(TupleDesc t) {
		//your code here
		this.tupleDesc = t;
		map = new HashMap<>();
	}
	
	public TupleDesc getDesc() {
		//your code here
		return this.tupleDesc;
	}
	
	/**
	 * retrieves the page id where this tuple is stored
	 * @return the page id of this tuple
	 */
	public int getPid() {
		//your code here
		return this.Pid;
	}

	public void setPid(int pid) {
		//your code here
		this.Pid = pid;
	}

	/**
	 * retrieves the tuple (slot) id of this tuple
	 * @return the slot where this tuple is stored
	 */
	public int getId() {
		//your code here
		return this.id;
	}

	public void setId(int id) {
		//your code here
		this.id = id;
	}
	
	public void setDesc(TupleDesc td) {
		//your code here
		this.tupleDesc = td;
	}
	
	/**
	 * Stores the given data at the i-th field
	 * @param i the field number to store the data
	 * @param v the data
	 */
	public void setField(int i, Field v) {
		//your code here
		map.put(this.getDesc().getFieldName(i), v);
	}
	
	public Field getField(int i) {
		//your code here
		return map.get(this.getDesc().getFieldName(i));
	}
	
	public String getFieldName(int i) {
		return this.getDesc().getFieldName(i);
	}
	
	/**
	 * Creates a string representation of this tuple that displays its contents.
	 * You should convert the binary data into a readable format (i.e. display the ints in base-10 and convert
	 * the String columns to readable text).
	 */
	public String toString() {
		//your code here
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.getDesc().numFields(); i++) {
			Field field = this.getField(i);
			String value = field.getType().equals(Type.INT) ? Integer.toString(((IntField) field).getValue()): new String(field.toByteArray());
			sb.append(this.getFieldName(i)+":"+value);
			if (i <this.getDesc().numFields()-1) sb.append("; ");
		}
		return sb.toString();
	}
}
	