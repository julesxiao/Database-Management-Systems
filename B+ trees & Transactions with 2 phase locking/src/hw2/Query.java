package hw2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.HTML;

import org.junit.Before;

import hw1.Catalog;
import hw1.Database;
import hw1.Field;
import hw1.HeapFile;
import hw1.IntField;
import hw1.RelationalOperator;
import hw1.TupleDesc;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;

public class Query {

	private String q;
	private Catalog c;

	public Query(String q) {
		this.q = q;
		this.c = new Catalog();
	}

	public void setup() {
		try {
			Files.copy(new File("testfiles/test.dat.bak").toPath(), new File("testfiles/test.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(new File("testfiles/A.dat.bak").toPath(), new File("testfiles/A.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(new File("testfiles/B.dat.bak").toPath(), new File("testfiles/B.dat").toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("unable to copy files");
			e.printStackTrace();
		}
		c = Database.getCatalog();
		c.loadSchema("testfiles/test.txt");

		c = Database.getCatalog();
		c.loadSchema("testfiles/A.txt");

		c = Database.getCatalog();
		c.loadSchema("testfiles/B.txt");
	}
	public Relation execute()  {
		Statement statement = null;
		try {
			statement = CCJSqlParserUtil.parse(q);
		} catch (JSQLParserException e) {
			System.out.println("Unable to parse query");
			e.printStackTrace();
		}

		//parsing & setup
		Select selectStatement = (Select) statement;
		PlainSelect sb = (PlainSelect)selectStatement.getSelectBody();
		setup();

		//FROM
		FromItem table = sb.getFromItem();
		int tableId = c.getTableId(table.toString());
		TupleDesc td = c.getTupleDesc(tableId);
		HeapFile hf = c.getDbFile(tableId);
		Relation relation = new Relation(hf.getAllTuples(), td);

		//JOIN
		List<Join> joins = sb.getJoins();
		if (joins != null) {
			for (Join join : joins) {
				Expression on = join.getOnExpression();
				JoinExpressionVisitor jev = new JoinExpressionVisitor();
				on.accept(jev);
				
				String leftTable = jev.getLeftTable();
				String leftColumn = jev.getLeftColumn();
				String rightTable = jev.getRightTable();
				String rightColumn = jev.getRightColumn();
				int otherTableId;
				String column;
				String otherColumn;
				if (rightTable.toUpperCase().equals(table.toString().toUpperCase())) {
					otherTableId = c.getTableId(leftTable);
					column = rightColumn;
					otherColumn = leftColumn;
				} else {
					otherTableId = c.getTableId(rightTable);
					column = leftColumn;
					otherColumn = rightColumn;
				}
				TupleDesc otherTd = c.getTupleDesc(otherTableId);
				HeapFile otherHf = c.getDbFile(otherTableId);

				relation = relation.join(new Relation(otherHf.getAllTuples(), otherTd), relation.getDesc().nameToId(column), otherTd.nameToId(otherColumn));
			}
		}
		//WHERE (selection)
		Expression where = sb.getWhere();
		if (where != null) {
			WhereExpressionVisitor wxv = new WhereExpressionVisitor();
			where.accept(wxv);
			relation = relation.select(relation.getDesc().nameToId(wxv.getLeft()), wxv.getOp(), wxv.getRight());
		}

		//SELECT (projection)
		List<SelectItem> selectItems = sb.getSelectItems();
		ArrayList<Integer> fields = new ArrayList<>();
		ArrayList<Integer> renameFields = new ArrayList<>();
		ArrayList<String> names = new ArrayList<>();
		for (SelectItem selectItem : selectItems) {
			//AS (rename)
			AsColumnVisitor asv = new AsColumnVisitor();
			selectItem.accept(asv);
			if (asv.isRename()) {
				String original = asv.getOriginal();
				String newName = asv.getNewName();
				renameFields.add(relation.getDesc().nameToId(original));
				names.add(newName);
				fields.add(relation.getDesc().nameToId(original));
				continue;
			}
			ColumnVisitor cv = new ColumnVisitor();
			selectItem.accept(cv);
			String column = cv.getColumn();
			System.out.print(column);
			if (column.equals("*")) {
				for (int i = 0; i < relation.getDesc().numFields(); i++) {
					fields.add(i);
				}
				break;
			} else {
				fields.add(relation.getDesc().nameToId(column));
			}
		}
		try {
			relation = relation.rename(renameFields, names);
		} catch (Exception e) {
			e.printStackTrace();
		}
		relation = relation.project(fields);

		//AGGREGATE or GROUP BY
		List<Expression> groupBy = sb.getGroupByColumnReferences();
		for (SelectItem selectItem : selectItems) {
			ColumnVisitor cv = new ColumnVisitor();
			selectItem.accept(cv);
			if (cv.isAggregate()) {
				relation = relation.aggregate(cv.getOp(), groupBy != null);
			}
		}

		return relation;
	}
}
