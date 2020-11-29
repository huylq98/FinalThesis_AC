/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.extract;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Element;

/**
 * A filter that contains nested filters.
 * 
 * @author mfreire
 */
public class CompositeFilter extends FileTreeFilter {

	public enum Operator {
		Nor, And, Or
	};

	private final ArrayList<FileTreeFilter> filters = new ArrayList<>();
	private Operator op = Operator.And;

	public void saveInner(Element e) throws IOException {
		e.setAttribute("operation", op.toString());

		// Add child filters
		for (FileTreeFilter filter : filters) {
			e.addContent(filter.saveToXML());
		}
	}

	public void loadFromXML(Element filterElement) throws IOException {
		setOp(Operator.valueOf(filterElement.getAttributeValue("operation")));
		filters.clear();

		List<Element> children = filterElement.getChildren();
		for (Element e : children) {
			try {
				Class filterClass = Class.forName(e.getAttributeValue("class"));
				FileTreeFilter f = (FileTreeFilter) filterClass.newInstance();
				f.loadFromXML(e);
				filters.add(f);
			} catch (ClassNotFoundException ex) {
				throw new IOException(ex);
			} catch (InstantiationException ex) {
				throw new IOException("Could not instantiate "
						+ e.getAttributeValue("class"), ex);
			} catch (IllegalAccessException ex) {
				throw new IOException(ex);
			}
		}
	}

	public void setOp(Operator op) {
		this.op = op;
	}

	public Operator getOp() {
		return op;
	}

	public void clear() {
		filters.clear();
		op = Operator.And;
	}

	public void removeFilter(FileTreeFilter f) {
		filters.remove(f);
	}

	public void addFilter(FileTreeFilter f) {
		filters.add(f);
	}

	public ArrayList<FileTreeFilter> getFilters() {
		return filters;
	}

	public boolean accept(FileTreeNode fn) {
		switch (op) {
		case Nor:
			for (FileTreeFilter ff : filters) {
				if (ff.accept(fn))
					return false;
			}
			return true;
		case Or:
			for (FileTreeFilter ff : filters) {
				if (ff.accept(fn))
					return true;
			}
			return false;
		case And:
			for (FileTreeFilter ff : filters) {
				if (!ff.accept(fn))
					return false;
			}
			return true;
		default:
			throw new RuntimeException(
					"Bad operation in boolean-composite-filter");
		}
	}

	public boolean accept(File f) {
		switch (op) {
		case Nor:
			return !filters.get(0).accept(f);
		case Or:
			for (FileTreeFilter ff : filters) {
				if (ff.accept(f))
					return true;
			}
			return false;
		case And:
			for (FileTreeFilter ff : filters) {
				if (!ff.accept(f))
					return false;
			}
			return true;
		default:
			throw new RuntimeException(
					"Bad operation in boolean-composite-filter");
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		for (FileTreeFilter f : filters) {
			sb.append(f).append(" ");
		}
		sb.append("]");
		return "" + op + " " + sb.toString();
	}
}

