package hw2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import hw1.Field;
import hw1.IntField;
import hw1.StringField;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;


/**
 * A class to perform various aggregations, by accepting one tuple at a time
 * @author Doug Shook
 *
 */
public class Aggregator {

	public class Info {
		private int freq;
		private int value;
		private Set<String> set; 
		private Field field;
		private boolean isInt;
		private String stringValue;

		public Info(Field field, boolean isInt) {
			this.freq = 0;
			this.value = 0;
			this.set = new HashSet<>();
			this.field = field;
			this.isInt = isInt;
			this.stringValue = "";
		}
		public Field getResult(AggregateOperator op) {
			if (op.equals(AggregateOperator.SUM)) {
				return new IntField(value);
			} else if (op.equals(AggregateOperator.MIN) || op.equals(AggregateOperator.MAX)) {
				if (isInt) {
					return new IntField(value);
				} else {
					return new StringField(stringValue.trim());
				}
			} else if (op.equals(AggregateOperator.AVG)) {
				return new IntField(value/freq);
			} else {
				return new IntField(set.size());
			} 
		}
		public Field getField() {
			return this.field;
		}
		public void handleMAX(String v) {
			if (isInt) {
				int value = Integer.valueOf(v);
				this.value = this.freq == 0 ? value : Math.max(value, this.value);
			} else {
				this.stringValue = stringValue.equals("") ? v : stringValue.compareTo(v) <= 0 ? v : stringValue;
			}
			this.freq++;
		}
		public void handleMIN(String v) {
			if (isInt) {
				int value = Integer.valueOf(v);
				this.value = this.freq == 0 ? value : Math.min(value, this.value);
			} else {
				this.stringValue = stringValue.equals("") ? v : stringValue.compareTo(v) <= 0 ? stringValue : v;
			}
			this.freq++;
		}
		public void handleAVG(String v) {
			this.handleSUM(v);
		}
		public void handleSUM(String v) {
			int value = Integer.valueOf(v);
			this.value += value;
			this.freq++;
		}
		public void handleCOUNT(String v) {
			this.set.add(v);
		}
		
		public void applyOperator(AggregateOperator op, String v) {
			if (op.equals(AggregateOperator.MAX)) {
				this.handleMAX(v);
			} else if (op.equals(AggregateOperator.MIN)) {
				 this.handleMIN(v);
			} else if (op.equals(AggregateOperator.AVG)) {
				this.handleAVG(v);
			} else if (op.equals(AggregateOperator.SUM)) {
				this.handleSUM(v);
			} else {
				this.handleCOUNT(v);
			} 
		}
	}

	private Map<String, Info> map;
	private AggregateOperator op;
	private boolean groupBy;
	private TupleDesc td;

	public Aggregator(AggregateOperator o, boolean groupBy, TupleDesc td) {
		//your code here
		this.map = new HashMap<>();
		this.op = o;
		this.groupBy = groupBy;
		this.td = td;
	}

	/**
	 * Merges the given tuple into the current aggregation
	 * @param t the tuple to be aggregated
	 */
	public void merge(Tuple t) {
		//your code here
		if (t.getDesc().numFields() > 1 && !this.groupBy) mergeAllWithoutGroupBy(t);
		else if (this.groupBy) mergeWithGroupBy(t);
		else mergeWithoutGroupBy(t);

	}
	public void mergeAllWithoutGroupBy(Tuple t) {
		String fieldString = "";
		for (int i = 0; i < t.getDesc().numFields(); i++) {
			Field field = t.getField(i);
			fieldString += new String(field.toByteArray());
		}
		Info info = this.map.getOrDefault("", new Info(null, false));
		info.applyOperator(this.op, fieldString);
		this.map.put("", info);
	}
	public void mergeWithoutGroupBy(Tuple t) {
		Field field = t.getField(0);
		boolean isInt = field.getType().equals(Type.INT);
		String fieldString = isInt ? Integer.toString(((IntField) field).getValue()): new String(field.toByteArray());
		Info info = this.map.getOrDefault("", new Info(null, isInt));
		info.applyOperator(this.op, fieldString);
		this.map.put("", info);
	}
	
	public void mergeWithGroupBy(Tuple t) {
		Field groupField = t.getField(0);
		Type groupType = groupField.getType();
		String fieldString = groupType.equals(Type.INT) ? Integer.toString(((IntField) groupField).getValue()): new String(groupField.toByteArray());
		Field aggField = t.getField(1);
		Type aggtype = aggField.getType();
		boolean isInt = aggtype.equals(Type.INT);
		String aggString = isInt ? Integer.toString(((IntField) aggField).getValue()): new String(aggField.toByteArray());
		Info info = this.map.getOrDefault(fieldString, new Info(groupField, isInt));
		info.applyOperator(this.op, aggString);
		this.map.put(fieldString, info);
	}

	/**
	 * Returns the result of the aggregation
	 * @return a list containing the tuples after aggregation
	 */
	public ArrayList<Tuple> getResults() {
		//your code here
		ArrayList<Tuple> tuples = new ArrayList<>();
		for (Info info : this.map.values()) {
			Tuple tuple = new Tuple(this.td);
			if (this.groupBy) {
				tuple.setField(0, info.getField());
				tuple.setField(1, info.getResult(this.op));
			} else {
				tuple.setField(0, info.getResult(this.op));
			}
			tuples.add(tuple);
		}
		return tuples;
	}

}
