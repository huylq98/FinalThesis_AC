/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.expression;

import java.util.ArrayList;

public interface CompositeExpression extends Expression {

	/**
	 * Returns the list of current subexpressions; null if none
	 */
	ArrayList<Expression> getChildren();

	/**
	 * Adds a subexpression
	 */
	Expression addChild(boolean composite);

	/**
	 * Removes a subexpression
	 */
	void removeChild(Expression e);
}
