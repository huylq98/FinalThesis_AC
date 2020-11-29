/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.util;

import java.io.IOException;
import org.jdom2.Element;

/**
 * Can save state to and from a JDOM Element
 *
 * @author miguelinux
 */
public interface XMLSerializable {
	/**
	 * Saves state to a org.jdom.Element instance
	 *
	 * @return the Element created
	 * @throws IOException on error
	 */
	public Element saveToXML() throws IOException;

	/**
	 * Load state from a org.jdom.Element instance
	 *
	 * @param element the element to be read
	 * @throws IOException on error
	 */
	public void loadFromXML(Element element) throws IOException;
}
