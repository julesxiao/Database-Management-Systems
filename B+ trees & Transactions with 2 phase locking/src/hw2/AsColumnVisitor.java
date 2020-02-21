package hw2;

import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;

public class AsColumnVisitor extends SelectItemVisitorAdapter {
	
	private String original;
	private String newName;
	private boolean isRename;
	
	@Override
	public void visit(SelectExpressionItem item) {
		if (item.toString().indexOf("AS") != -1) {
			String[] columns = item.toString().split(" AS ");
			this.original = columns[0];
			this.newName = columns[1];
			this.isRename = true;
		} else {
			this.isRename = false;
		}
	}
	public boolean isRename() {
		return this.isRename;
	}
	public String getOriginal() {
		return this.original;
	}
	public String getNewName() {
		return this.newName;
	}

}
