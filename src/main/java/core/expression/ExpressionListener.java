/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.expression;

public interface ExpressionListener {

	/**
	 * Graphically display the effects of this expression
	 */
	void expressionChanged(Expression e, boolean test);
}
