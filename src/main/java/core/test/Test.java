/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.test;

import core.Submission;
import core.util.XMLSerializable;
import java.io.IOException;
import org.jdom2.Element;

public abstract class Test implements XMLSerializable {

	/** the test's key; may not contain spaces */
	protected String testKey;

	/** required keys */
	protected String[] requires = new String[0];

	/** provided keys */
	protected String[] provides = new String[0];

	/** true if test preprocessing is parallelizable */
	protected boolean independentPreprocessing = false;
	/** true if test similarity is parallelizable */
	protected boolean independentSimilarity = false;

	/**
	 * Configures this test
	 * 
	 * @param e the jdom element to read settings from
	 * @throws IOException on error
	 */
	public void loadFromXML(Element e) throws IOException {
		testKey = e.getAttributeValue("key");
		requires = attributeToStringArray(e.getAttributeValue("requires"));
		provides = attributeToStringArray(e.getAttributeValue("provides"));
	}

	/**
	 * Saves the test's configuration as a JDom element.
	 * 
	 * @return saved configuration, or 'null' if nothing to save.
	 * @throws IOException on error
	 */
	public Element saveToXML() throws IOException {
		Element e = new Element("test");
		e.setAttribute("class", getClass().toString());
		e.setAttribute("key", getTestKey());
		e.setAttribute("requires", stringArrayToAttribute(requires));
		e.setAttribute("provides", stringArrayToAttribute(provides));
		saveInner(e);
		return e;
	}

	private static String stringArrayToAttribute(String[] array) {
		StringBuilder sb = new StringBuilder();
		for (String s : array) {
			sb.append(s).append(" ");
		}
		return sb.toString().trim();
	}

	private static String[] attributeToStringArray(String attribute) {
		return attribute.split("[, ]+");
	}

	/**
	 * Implemented by subclasses to save state in an element
	 * 
	 * @param e
	 * @throws IOException on error
	 */
	protected abstract void saveInner(Element e) throws IOException;

	/**
	 * Global initialization for the test
	 */
	public void init(Submission[] subjects) {
		// the default is to do nothing
	}

	/**
	 * All subjects will have been preprocessed before similarity is checked.
	 */
	public abstract void preprocess(Submission s);

	/**
	 * Determine similarity between two subjects
	 * 
	 * @return a number between 0 (most similar) and 1 (least similar)
	 */
	public abstract float similarity(Submission a, Submission b);

	/**
	 * @return true if similarity calculation can be parallelized; default is false
	 */
	public final boolean isIndependentSimilarity() {
		return independentSimilarity;
	}

	/**
	 * @return true if preprocessing calculation can be parallelized; default is
	 *         false
	 */
	public final boolean isIndependentPreprocessing() {
		return independentPreprocessing;
	}

	/**
	 * @return the testKey
	 */
	public String getTestKey() {
		return testKey;
	}

	/**
	 * @param testKey the testKey to set
	 */
	public void setTestKey(String testKey) {
		this.testKey = testKey;
	}

	public String[] getRequires() {
		return requires;
	}

	public String[] getProvides() {
		return provides;
	}

	private float progress;

	/**
	 * Returns current progress
	 */
	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}
}
