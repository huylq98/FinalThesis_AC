/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.expression;

import core.extract.CompositeFilter;
import core.extract.FileTreeFilter;
import java.util.ArrayList;
import static core.util.I18N.m;

/**
 * A composite expression that evaluates to true or false depending on its
 * contents
 *
 * @author mfreire
 */
public class CompositeBooleanExp implements CompositeExpression,
		FilterExpression {

	private CompositeFilter filter = new CompositeFilter();

	private CompositeBooleanExp parent;
	private final ArrayList<FilterExpression> children = new ArrayList<FilterExpression>();

	/**
	 * Creates a new instance of CompositeBooleanExp
	 */
	public CompositeBooleanExp(CompositeFilter filter) {
		this.filter = filter;
	}

	public ArrayList<Expression> getChildren() {
		ArrayList<Expression> al = new ArrayList<Expression>();
		al.addAll(children);
		return al;
	}

	public Expression addChild(boolean composite) {
		FilterExpression child = composite ? new CompositeBooleanExp(
				new CompositeFilter()) : new AtomicBooleanExp();

		filter.addFilter(child.getFilter());
		children.add(child);
		child.setParentExpression(this);
		return child;
	}

	public void setParentExpression(CompositeExpression parent) {
		this.parent = (CompositeBooleanExp) parent;
	}

	public void removeChild(Expression e) {
		children.remove(e);
		filter.removeFilter(((FilterExpression) e).getFilter());
	}

	public String getHeader() {
		switch (filter.getOp()) {
		case And:
			return headers.get(0);
		case Or:
			return headers.get(1);
		default:
			return null;
		}
	}

	public String getBody() {
		return null;
	}

	private static ArrayList<String> headers = new ArrayList<String>();

	public ArrayList<String> getHeaders() {
		if (headers.isEmpty()) {
			headers.add(m("Filter.AllConditions"));
			headers.add(m("Filter.AtLeastOneCondition"));
			headers.add(m("Filter.NoCondition"));
		}
		return headers;
	}

	public FileTreeFilter getFilter() {
		return filter;
	}

	/**
	 * Change the header - and therefore, the expression type
	 */
	public void setHeader(String header) {
		if (header.equals(m("Filter.AllConditions"))) {
			filter.setOp(CompositeFilter.Operator.And);
		} else if (header.equals(m("Filter.AtLeastOneCondition"))) {
			filter.setOp(CompositeFilter.Operator.Or);
		} else if (header.equals(m("Filter.NoCondition"))) {
			filter.setOp(CompositeFilter.Operator.Nor);
		} else {
			throw new IllegalArgumentException("Bad composition: " + header);
		}
	}

	public void setBody(String string) {
	}
}
