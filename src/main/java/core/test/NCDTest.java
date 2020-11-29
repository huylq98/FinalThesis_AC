/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.test;

import core.Submission;
import core.util.archive.ArchiveFormat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Element;

public class NCDTest extends TokenizingTest {

	private static final Logger log = LogManager.getLogger(NCDTest.class);

	static final public String sizeKeySuffix = "_ncd_size";
	static final public String similarityKeySuffix = "_ncd_sim";

	private String sizeKey;
	private ArchiveFormat compressor;

	/**
	 * Creates a new instance of NCDTest
	 * 
	 * @param compressor to use
	 */
	public NCDTest(ArchiveFormat compressor) {
		this(compressor, compressor.getClass().getSimpleName().replaceAll("Format", ""));
	}

	/**
	 * Creates a new NCDTest with the given compressor and key.
	 */
	public NCDTest(ArchiveFormat compressor, String keyPrefix) {
		this.independentPreprocessing = true;
		this.independentSimilarity = true;
		this.compressor = compressor;
		sizeKey = keyPrefix + sizeKeySuffix;
		testKey = keyPrefix + similarityKeySuffix;
	}

	/**
	 * Configures this test
	 * 
	 * @param e
	 */
	@Override
	public void loadFromXML(Element e) throws IOException {
		super.loadFromXML(e);
		sizeKey = e.getAttributeValue("sizeKey");
	}

	/**
	 * Saves state to an element
	 * 
	 * @param e
	 */
	protected void saveInner(Element e) throws IOException {
		super.saveInner(e);
		e.setAttribute("compressor", compressor.getClass().getName());
		e.setAttribute("sizeKey", sizeKey);
	}

	public int getCompSize(Submission s) {
		return ((Integer) s.getData(sizeKey)).intValue();
	}

	/**
	 * All subjects will have been preprocessed before similarity is checked.
	 */
	public void preprocess(Submission s) {
		super.preprocess(s);

		String tokens = getTokens(s);

		int size = -1;
		try {
			size = compressor.compressedSize(new ByteArrayInputStream(tokens.getBytes()));
		} catch (IOException e) {
			log.warn("Exception during preprocess", e);
		}
		s.putData(sizeKey, new Integer(size));
	}

	/**
	 * @return a number between 0 (most similar) and 1 (least similar)
	 */
	public float similarity(Submission sa, Submission sb) {
		try {
			String tokens = getTokens(sa) + getTokens(sb);
			InputStream is = new ByteArrayInputStream(tokens.getBytes());
			int a = getCompSize(sa);
			int b = getCompSize(sb);
			int c = compressor.compressedSize(is);
			int m = Math.min(a, b);
			int M = a + b - m;
			return (float) (c - m) / (float) M;
		} catch (IOException e) {
			log.warn("Exception during similarity comparison", e);
		}
		return -1f;
	}
}
