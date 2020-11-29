/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.expression;

import core.extract.CompositeFilter;
import core.extract.ContentPatternFilter;
import core.extract.FileNameFilter;
import core.extract.FileTreeFilter;
import core.extract.PathFilter;
import core.extract.PatternFilter;
import java.util.ArrayList;

import static core.util.I18N.m;

public class AtomicBooleanExp implements FilterExpression {

	private String header;
	private FileTreeFilter filter;

	private CompositeBooleanExp parent;

	/**
	 * Creates a new instance of AtomicBooleanExp
	 */
	public AtomicBooleanExp(FileTreeFilter filter) {
		this.filter = filter;
	}

	public AtomicBooleanExp() {
		setHeader(getHeaders().get(0));
		setBody(".*");
	}

	public void setParentExpression(CompositeExpression parent) {
		this.parent = (CompositeBooleanExp) parent;
		setHeader(getHeader());
	}

	private static ArrayList<String> headers = new ArrayList<String>();

	public ArrayList<String> getHeaders() {
		if (headers.isEmpty()) {
			headers.add(m("Filter.NameContains"));
			headers.add(m("Filter.NameMatches"));
			headers.add(m("Filter.NameEndsWith"));
			headers.add(m("Filter.PathContains"));
			headers.add(m("Filter.PathMatches"));
			headers.add(m("Filter.PathEndsWith"));
			headers.add(m("Filter.ContentContains"));
			headers.add(m("Filter.ContentMatches"));
		}
		return headers;
	}

	public String getHeader() {
		return header;
	}

	public String getBody() {
		String p = ((PatternFilter) filter).getPattern();
		if (header.endsWith(m("Filter.Contains"))) {
			p = p.substring(".*".length(), p.length() - ".*".length());
		} else if (header.endsWith(m("Filter.EndsWith"))) {
			p = p.substring(".*".length());
		}
		return p;
	}

	public void setBody(String body) {
		if (header.endsWith(m("Filter.Contains"))) {
			body = ".*" + body + ".*";
		} else if (header.endsWith(m("Filter.EndsWith"))) {
			body = ".*" + body;
		}
		((PatternFilter) filter).setPattern(body);
	}

	private FileTreeFilter createFilter(String header, String body) {
		if (header.endsWith(m("Filter.Contains"))) {
			body = ".*" + body + ".*";
		} else if (header.endsWith(m("Filter.EndsWith"))) {
			body = ".*" + body;
		}

		FileTreeFilter ff = null;
		if (header.startsWith(m("Filter.Name"))) {
			ff = new FileNameFilter(body);
		} else if (header.startsWith(m("Filter.Path"))) {
			ff = new PathFilter(body);
		} else if (header.startsWith(m("Filter.Content"))) {
			ff = new ContentPatternFilter(body);
		}
		return ff;
	}

	public FileTreeFilter getFilter() {
		return filter;
	}

	public void setHeader(String header) {
		String body = (filter == null) ? ".*" : getBody();
		filter = createFilter(header, body);
		this.header = header;

		if (parent != null) {
			int i = parent.getChildren().indexOf(this);
			((CompositeFilter) parent.getFilter()).getFilters().set(i, filter);
		}
	}
}
