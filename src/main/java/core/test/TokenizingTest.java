/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.test;

import core.Submission;
import core.Tokenizer;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Element;

public abstract class TokenizingTest extends Test {

	private static final Logger log = LogManager
			.getLogger(TokenizingTest.class);

	protected Tokenizer tokenizer;

	/**
	 * Set the tokenizer to use during testing (usually only preprocessing)
	 * @param t
	 */
	public void setTokenizer(Tokenizer t) {
		this.tokenizer = t;
	}

	/**
	 * Retrieve tokens for the given subject
	 * @param s
	 * @return 
	 */
	public String getTokens(Submission s) {
		return (String) s.getData(Tokenizer.TOKEN_KEY);
	}

	/**
	 * Configures this test
	 * @param e
	 */
	@Override
	public void loadFromXML(Element e) throws IOException {
		super.loadFromXML(e);
		Element te = e.getChild("tokenizer");
		try {
			String tokenizerClassName = te.getAttributeValue("class");
			tokenizer = (Tokenizer) getClass().getClassLoader().loadClass(
					tokenizerClassName).newInstance();
			tokenizer.loadFromXML(e);
		} catch (Exception ex) {
			throw new IOException("Error loading tokenizer", ex);
		}
	}

	/**
	 * Saves state to an element
	 * @param e 
	 */
	protected void saveInner(Element e) throws IOException {
		e.addContent(tokenizer.saveToXML());
	}

	/**
	 * Tokenizes the subject's sources (if they had not yet been tokenized)
	 * @param s
	 */
	public void preprocess(Submission s) {
		String tokens = (String) s.getData(Tokenizer.TOKEN_KEY);
		if (tokens == null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			String currentFile = null;
			//            System.out.println(s.getId() + ":");
			try {
				for (int i = 0; i < s.getSources().size(); i++) {
					currentFile = s.getId() + "/" + s.getSourceName(i);
					tokenizer.tokenize(s.getSourceCode(i), currentFile, pw);
				}
			} catch (Throwable tr) {
				log.warn("Error tokenizing " + currentFile + " from " + s, tr);
			}
			pw.flush();
			sw.flush();
			tokens = sw.toString();
			s.putData(Tokenizer.TOKEN_KEY, tokens);
		}
	}
}