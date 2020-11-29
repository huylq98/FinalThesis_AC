/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package ui.expression;

import core.expression.Expression;

public class AtomicExpressionPanel extends ExpressionPanel {

	private Expression e;

	public AtomicExpressionPanel(CompositeExpressionPanel parentPanel) {
		super(parentPanel);
		initComponents();
	}

	public void setExpression(Expression e) {
		this.e = e;
		jtfPattern.setText(e.getBody());
	}

	public Expression getExpression() {
		return e;
	}

	private void initComponents() {
		jtfPattern = new javax.swing.JTextField();

		jtfPattern.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent evt) {
				jtfPatternFocusLost(evt);
			}
		});
		jtfPattern.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyTyped(java.awt.event.KeyEvent evt) {
				jtfPatternKeyTyped(evt);
			}
		});
	}

	private void jtfPatternFocusLost(java.awt.event.FocusEvent evt) {
		e.setBody(jtfPattern.getText());
	}

	private void jtfPatternKeyTyped(java.awt.event.KeyEvent evt) {
		e.setBody(jtfPattern.getText());
	}

	private javax.swing.JTextField jtfPattern;
}
