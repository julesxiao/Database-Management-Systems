package hw2;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;
import hw1.StringField;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;

public class JoinExpressionVisitor extends ExpressionVisitorAdapter{
	private RelationalOperator op;
	private String leftTable;
	private String leftColumn;
	private String rightTable;
	private String rightColumn;
	
	@Override
	public void visit(EqualsTo equalsTo) {
		op = RelationalOperator.EQ;
		processOps(equalsTo);
	}
	private void processOps(ComparisonOperator c) {
		leftTable = ((Column)c.getLeftExpression()).getTable().getName();
		leftColumn = ((Column)c.getLeftExpression()).getColumnName();
		rightTable = ((Column)c.getRightExpression()).getTable().getName();
		rightColumn = ((Column)c.getRightExpression()).getColumnName();
	}
	
	public String getLeftTable() {
		return this.leftTable;
	}
	public String getLeftColumn() {
		return this.leftColumn;
	}
	public String getRightTable() {
		return this.rightTable;
	}
	public String getRightColumn() {
		return this.rightColumn;
	}
	
	public RelationalOperator getOp() {
		return op;
	}
}
