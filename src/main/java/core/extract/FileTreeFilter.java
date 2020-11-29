/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.extract;

import core.util.XMLSerializable;

import java.io.FileFilter;
import java.io.IOException;
import org.jdom2.Element;

/**
 * A file-tree filter.
 * 
 * @author mfreire
 */
public abstract class FileTreeFilter implements XMLSerializable, FileFilter {

	/**
	 * Tests whether or not the specified node should be accepted
	 *
	 * @param node The node to be tested
	 * @return <code>true</code> if and only if <code>node</code> should be included
	 *         in an "accepted" list
	 */
	public abstract boolean accept(FileTreeNode node);

	public Element saveToXML() throws IOException {
		Element filterElement = new Element("filter");
		filterElement.setAttribute("class", this.getClass().getName());
		saveInner(filterElement);
		return filterElement;
	}

	/**
	 * Subclasses should save their details here
	 * 
	 * @param e
	 */
	public abstract void saveInner(Element e) throws IOException;
}
