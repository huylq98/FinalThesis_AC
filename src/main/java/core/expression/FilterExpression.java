/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.expression;

import core.extract.FileTreeFilter;

public interface FilterExpression extends Expression {

	FileTreeFilter getFilter();
}
