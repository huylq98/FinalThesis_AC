/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package ui.expression;

import core.expression.CompositeExpression;
import core.expression.Expression;

public class CompositeExpressionPanel extends ExpressionPanel {

	private CompositeExpression e;

	public CompositeExpressionPanel(CompositeExpressionPanel parentPanel) {
		super(parentPanel);
	}

	public void setExpression(Expression e) {
		this.e = (CompositeExpression) e;
		for (Expression c : this.e.getChildren()) {
			addChildSub(c);
		}
	}

	public CompositeExpression getExpression() {
		return e;
	}

	private void addChildSub(Expression c) {
		ExpressionPanel p = (c instanceof CompositeExpression) ? new CompositeExpressionPanel(this)
				: new AtomicExpressionPanel(this);
		p.setExpression(c);
	}

	public void deleteSubexp(ExpressionPanel subexp) {
		e.removeChild(subexp.getExpression());
		CompositeExpressionPanel p = parentPanel;
		while (p != null) {
			p = p.parentPanel;
		}
	}

	public void confirm() {
		test(false);
	}

	public void jbAddSimpleActionPerformed(java.awt.event.ActionEvent evt) {
		Expression c = e.addChild(false);
		addChildSub(c);
		CompositeExpressionPanel p = parentPanel;
		while (p != null) {
			p = p.parentPanel;
		}
	}

	public void addExpression(String expression) {
		Expression c = e.addChild(false);
		c.setBody(expression);
		addChildSub(c);
		CompositeExpressionPanel p = parentPanel;
		while (p != null) {
			p = p.parentPanel;
		}
	}
}
