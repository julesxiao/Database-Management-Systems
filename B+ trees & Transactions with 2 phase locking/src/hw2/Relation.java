package hw2;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.naming.ldap.Rdn;

import hw1.Field;
import hw1.RelationalOperator;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;

/**
 * This class provides methods to perform relational algebra operations. It will be used
 * to implement SQL queries.
 * @author Doug Shook
 *
 */
public class Relation {

	private ArrayList<Tuple> tuples;
	private TupleDesc td;
	
	public Relation(ArrayList<Tuple> l, TupleDesc td) {
		//your code here
		this.tuples = l;
		this.td = td;
	}
	
	/**
	 * This method performs a select operation on a relation
	 * @param field number (refer to TupleDesc) of the field to be compared, left side of comparison
	 * @param op the comparison operator
	 * @param operand a constant to be compared against the given column
	 * @return
	 */
	public Relation select(int field, RelationalOperator op, Field operand) {
		//your code here
		ArrayList<Tuple> tuples = new ArrayList<>();
		for (Tuple tuple : this.getTuples()) {
			Field f = tuple.getField(field);
			if (f.compare(op, operand)) tuples.add(tuple);
		}
		return new Relation(tuples, this.getDesc());
	}
	
	/**
	 * This method performs a rename operation on a relation
	 * @param fields the field numbers (refer to TupleDesc) of the fields to be renamed
	 * @param names a list of new names. The order of these names is the same as the order of field numbers in the field list
	 * @return
	 * @throws Exception 
	 */
	public Relation rename(ArrayList<Integer> fields, ArrayList<String> names) throws Exception {
		//your code here
		TupleDesc td = this.td;
		for (int i = 0; i < fields.size(); i++) {
			if (names.get(i)!="" && !duplicatedRename(names.get(i))) td.setFieldName(fields.get(i), names.get(i));
		}
		return new Relation(this.getTuples(), td);
	}
	public boolean duplicatedRename(String name) throws Exception {
		for (int i = 0; i < td.numFields(); i++) {
			if (td.getFieldName(i).equals(name)) throw new Exception();
		}
		return false;
	}
	
	/**
	 * This method performs a project operation on a relation
	 * @param fields a list of field numbers (refer to TupleDesc) that should be in the result
	 * @return
	 */
	public Relation project(ArrayList<Integer> fields) throws IllegalArgumentException {
		//your code here
		Type[] typeAr = new Type[fields.size()];
		String[] fieldAr = new String[fields.size()];
		for (int i = 0; i < fields.size(); i++) {
			try {
				typeAr[i] = td.getType(fields.get(i));
				fieldAr[i] = td.getFieldName(fields.get(i));
			} catch (Exception e) {
				throw new IllegalArgumentException();
			}
		}
		TupleDesc td = new TupleDesc(typeAr, fieldAr);
		ArrayList<Tuple> tuples = new ArrayList<>();
		if (td.numFields()==0) return new Relation(tuples, td);
		for (Tuple tuple : this.getTuples()) {
			Tuple newTuple = new Tuple(td);
			for (int i = 0; i < fields.size(); i++) {
				newTuple.setField(i, tuple.getField(fields.get(i)));
			}
			tuples.add(newTuple);
		}
		return new Relation(tuples, td);
	}
	
	/**
	 * This method performs a join between this relation and a second relation.
	 * The resulting relation will contain all of the columns from both of the given relations,
	 * joined using the equality operator (=)
	 * @param other the relation to be joined
	 * @param field1 the field number (refer to TupleDesc) from this relation to be used in the join condition
	 * @param field2 the field number (refer to TupleDesc) from other to be used in the join condition
	 * @return
	 */
	public Relation join(Relation other, int field1, int field2) {
		//your code here
		int size = this.getDesc().numFields() + other.getDesc().numFields();
		Type[] typeAr = new Type[size];
		String[] fieldAr = new String[size];
		for (int i = 0; i < this.getDesc().numFields(); i++) {
			typeAr[i] = this.getDesc().getType(i);
			fieldAr[i] = this.getDesc().getFieldName(i);
		}
		for (int i = 0; i < other.getDesc().numFields(); i++) {
			typeAr[i+this.getDesc().numFields()] = other.getDesc().getType(i);
			fieldAr[i+this.getDesc().numFields()] = other.getDesc().getFieldName(i);
		}
		TupleDesc td = new TupleDesc(typeAr, fieldAr);
		ArrayList<Tuple> tuples = new ArrayList<>();
		for (Tuple tuple : this.getTuples()) {
			for (Tuple otherTuple : other.getTuples()) {
				if (tuple.getField(field1).compare(RelationalOperator.EQ, otherTuple.getField(field2))) {
					Tuple newTuple = new Tuple(td);
					for (int i = 0; i < this.getDesc().numFields(); i++) {
						newTuple.setField(i, tuple.getField(i));
					}
					for (int i = 0; i < other.getDesc().numFields(); i++) {
						newTuple.setField(i+this.getDesc().numFields(), otherTuple.getField(i));
					}
					tuples.add(newTuple);
				}
			}
		}
		return new Relation(tuples, td);
	}
	
	/**
	 * Performs an aggregation operation on a relation. See the lab write up for details.
	 * @param op the aggregation operation to be performed
	 * @param groupBy whether or not a grouping should be performed
	 * @return
	 */
	public Relation aggregate(AggregateOperator op, boolean groupBy) {
		//your code here
		Aggregator aggregator = new Aggregator(op, groupBy, this.getDesc());
		for (Tuple tuple : this.getTuples()) {
			aggregator.merge(tuple);
		}
		return new Relation(aggregator.getResults(), this.getDesc());
	}
	
	public TupleDesc getDesc() {
		//your code here
		return this.td;
	}
	
	public ArrayList<Tuple> getTuples() {
		//your code here
		return this.tuples;
	}
	
	/**
	 * Returns a string representation of this relation. The string representation should
	 * first contain the TupleDesc, followed by each of the tuples in this relation
	 */
	public String toString() {
		//your code here
		StringBuilder sb = new StringBuilder();
		sb.append(this.getDesc().toString() + " ");
		for (Tuple tuple : this.getTuples()) {
			sb.append(tuple.toString() + " ");
		}
		return sb.toString().trim();
	}
}
