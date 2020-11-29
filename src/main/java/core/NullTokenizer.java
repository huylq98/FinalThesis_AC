/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core;

import java.io.IOException;
import java.io.PrintWriter;
import org.jdom2.Element;

public class NullTokenizer implements Tokenizer {

	/**
	 * Tokenize a java file into a buffer
	 */
	public void tokenize(String source, String sourceFile, PrintWriter out) {
		source = source.replaceAll("\\p{Space}+", " ");
		out.print(source);
	}

	/**
	 * Tokenize a java file into a buffer
	 */
	public void retrieveComments(String source, String sourceFile, PrintWriter out) {
		// do not do anything
	}

	public int tokenId(String t) {
		return Character.getNumericValue(t.charAt(0));
	}

	public Element saveToXML() throws IOException {
		Element e = new Element("tokenizer");
		e.setAttribute("class", getClass().getSimpleName());
		return e;
	}

	public void loadFromXML(Element element) throws IOException {
	}
}
