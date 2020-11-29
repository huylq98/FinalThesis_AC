/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.expression;

import java.util.ArrayList;

public interface Expression {

	/**
	 * Returns a list of headers (to be used in a combobox)
	 */
	ArrayList<String> getHeaders();

	/**
	 * Set the header of the expression; this should change the expression to a
	 * different type, according to the header
	 */
	void setHeader(String header);

	/**
	 * Returns the current header
	 */
	String getHeader();

	/**
	 * Set the body of the expression
	 */
	void setBody(String string);

	/**
	 * Returns the 'meat' of the expression, excluding the header. Typically,
	 * headers will be chosen from a combobox, and contents inserted in a textfield
	 */
	String getBody();

	/**
	 * Sets the parent expression (when this changes, the parent will probably need
	 * to be notified as well).
	 */
	void setParentExpression(CompositeExpression e);
}
