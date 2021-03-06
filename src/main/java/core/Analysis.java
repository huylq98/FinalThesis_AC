/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import core.extract.FileTreeNode;
import core.test.Test;
import utils.XMLSerializable;

public class Analysis implements XMLSerializable {

	private static final Logger log = LogManager.getLogger(Analysis.class);

	private static final String VERSION_STRING = "2.0";

	/** Set of sources that are being analyzed. */
	private SourceSet sourceSet;

	/** The submissions being compared. */
	private Submission[] subs;

	/**
	 * Submissions by ID.
	 */
	private final HashMap<String, Submission> idsToSubs = new HashMap<String, Submission>();

	/**
	 * Currently applied tests. Results are available in each individual submission
	 */
	private final HashSet<Test> appliedTests;

	public interface TokenizerFactory {
		Tokenizer getTokenizerFor(Submission[] subs);
	}

	private static TokenizerFactory tokenizerFactory;

	public static void setTokenizerFactory(TokenizerFactory tokenizerFactory) {
		Analysis.tokenizerFactory = tokenizerFactory;
	}

	/**
	 * Minimal initialization
	 */
	public Analysis() {
		sourceSet = null;
		appliedTests = new HashSet<>();
		subs = new Submission[0];
	}

	/**
	 * Initialize subjects from a FileTreeNode.
	 *
	 * Allows complex filters and 'virtual' files (using the SourceFileCache). After
	 * filtering, the resulting filteredTree is expected to contain, on the first
	 * level, one folder per submission (folder name to be used as ID); and under
	 * each submission-folder, the files that will be analyzed.
	 * 
	 * @param sources to load
	 * @throws java.io.IOException on error
	 */
	public void loadSources(SourceSet sources) throws IOException {
		this.sourceSet = sources;
		FileTreeNode root = sources.getFilteredTree();

		if (root == null) {
			throw new IllegalArgumentException("nothing to analyze");
		}

		HashMap<String, Submission> unique = new HashMap<>();
		idsToSubs.clear();
		int i = 0;
		for (FileTreeNode dn : root.getChildren()) {
			Submission s = new Submission(dn.getLabel(), dn.getPath(), 0);
			log.info("   created sub " + s.getId());
			for (FileTreeNode fn : dn.getLeafChildren()) {
				log.debug("    - " + fn.getFile().getName());
				s.addSource(fn.getFile());
			}

			String uniqueId = s.getId().substring(s.getId().indexOf('_') + 1, s.getId().lastIndexOf('_'));
			if (!unique.containsKey(uniqueId)) {
				unique.put(uniqueId, s);
				s.setInternalId(i++);
			} else {
				String prevSub = unique.get(uniqueId).getId();
				Integer prevSubId = Integer.parseInt(prevSub.substring(prevSub.lastIndexOf('_') + 1));
				if(Integer.parseInt(s.getId().substring(s.getId().lastIndexOf('_') + 1)) > prevSubId){
					unique.put(uniqueId, s);
				}
				log.warn("Detected EXACT duplicate.");
			}
		}

		subs = new Submission[unique.size()];
		i = 0;
		for (Submission s : unique.values()) {
			subs[i++] = s;
			idsToSubs.put(s.getId(), s);
		}
		if (i < 2) {
			log.warn("There are less than 2 unique submissions. No analysis can be carried out.");
		} else {
			log.info("{} unique submissions loaded for analysis", i);
		}
	}

	/**
	 * Choose the right tokenizer for a given file
	 */
	public Tokenizer chooseTokenizer() {
		return tokenizerFactory.getTokenizerFor(subs);
	}

	/**
	 * @return true if there are available results for this testKey
	 */
	public boolean hasResultsForKey(String testKey) {
		if (subs.length == 0) {
			return false;
		}
		Submission first = subs[0];
		return (first.getData(testKey) != null);
	}

	/**
	 * Preprocess files
	 */
	public void prepareTest(Test t) {
		t.setProgress(0f);
		t.init(subs);

		for (int i = 0; i < subs.length; i++) {
			try {
				ThreadContext.push("Pre-" + subs[i].getId());
				t.preprocess(subs[i]);
				ThreadContext.pop();
			} catch (Throwable re) {
				throw new RuntimeException("Error during pre-processing " + subs[i].getId(), re);
			}
			t.setProgress(i / (float) subs.length);
		}
		t.setProgress(1f);
	}

	private void applyParallelizedTest(Test t) {
		int nProc = Runtime.getRuntime().availableProcessors();
		int slices[] = calculateSliceSizes(nProc, subs.length);
		t.setProgress(0f);

		float[][] F = new float[subs.length][subs.length];

		// launch all jobs, first one with "monitor" set to "true"
		Thread threads[] = new Thread[nProc];
		SimilarityJob lastJob = null;
		for (int i = 0; i < nProc; i++) {
			lastJob = new SimilarityJob(F, t, slices[i], slices[i + 1], i == 0);
			threads[i] = new Thread(lastJob);
			threads[i].start();
		}

		// join them afterwards
		for (int i = 0; i < nProc; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException ie) {
				log.warn("I hate being interrupted. Test aborted");
			}
		}

		lastJob.end();
	}

	/**
	 * Apply a single test
	 */
	public void applyTest(Test t) {
		if (t.isIndependentSimilarity()) {
			applyParallelizedTest(t);
			return;
		}
		t.setProgress(0f);
		float[][] F = new float[subs.length][subs.length];
		SimilarityJob job = new SimilarityJob(F, t, 0, subs.length, true);
		job.run();
		job.end();
	}

	/**
	 * Return the limits of each slice in which to divide a large matrix-based job.
	 * Slice limits correspond to indices, where L[i+1]*L[i+1] - L[i]*L[i] =
	 * mSize*mSize/nJobs;
	 * 
	 * @param mSize rows in the square matrix that is to be split up
	 * @return an array of nJobs+1 limits
	 */
	private static int[] calculateSliceSizes(int nJobs, int mSize) {
		int x = mSize * mSize / nJobs;
		int slices[] = new int[nJobs + 1];
		slices[0] = 0;
		slices[nJobs] = mSize;
		for (int i = 0; i < nJobs - 1; i++) {
			slices[i + 1] = (int) Math.sqrt(x + slices[i] * slices[i]);
		}
		return slices;
	}

	private class SimilarityJob implements Runnable {

		private final float[][] F;
		private final int startIdx, endIdx;
		private final Test t;
		private final boolean monitor;

		public SimilarityJob(float[][] F, Test t, int startIdx, int endIdx, boolean monitor) {
			this.F = F;
			this.t = t;
			this.startIdx = startIdx;
			this.endIdx = endIdx;
			this.monitor = monitor;
		}

		public void run() {
			log.debug("I am a job from " + startIdx + " to " + endIdx);
			int total = (endIdx * endIdx - startIdx * startIdx) / 2;
			for (int i = startIdx, k = 0; i < endIdx; i++) {
				for (int j = 0; j < i; j++, k++) {
					try {
						F[i][j] = F[j][i] = t.similarity(subs[i], subs[j]);
					} catch (Throwable re) {
						throw new RuntimeException("Error comparando " + subs[i].getId() + " con " + subs[j].getId(),
								re);
					}
					if (monitor) {
						t.setProgress(k / (float) total);
					}
				}
			}
		}

		/**
		 * ends the job for all threads; should only be called on one of them
		 */
		public void end() {
			for (int i = 0; i < subs.length; i++) {
				subs[i].putData(t.getTestKey(), F[i]);
			}
			t.setProgress(1f);
			appliedTests.add(t);
		}
	}

	/**
	 * Retrieve an already-run test by key
	 * 
	 * @param key for the test
	 * @return the test that was run, or null if not found
	 */
	public Test getTestByKey(String key) {
		for (Test t : appliedTests) {
			if (t.getTestKey().equals(key)) {
				return t;
			}
		}
		return null;
	}

	public Submission[] getSubmissions() {
		return subs;
	}

	public HashSet<Test> getAppliedTests() {
		return appliedTests;
	}

	public Submission getSubmission(String id) {
		return idsToSubs.get(id);
	}

	/**
	 * Sort results by decreasing similarity
	 */
	public Result[] sortTestResults(String testKey) {
		Result[] P = new Result[subs.length * (subs.length - 1) / 2];
		for (int i = 0, k = 0; i < subs.length; i++) {
			float[] F = (float[]) subs[i].getData(testKey);
			for (int j = 0; j < i; j++, k++) {
				P[k] = new Result(subs[i], subs[j], F[j]);
			}
		}
		Arrays.sort(P);
		return P;
	}

	/**
	 * Inner class, representing a Result. Good for sorting
	 */
	public static class Result implements Comparable<Object>, Serializable {
		public Submission a, b;
		public float dist;

		public Result(Submission a, Submission b, float d) {
			this.a = a;
			this.b = b;
			this.dist = d;
		}

		@Override
		public int compareTo(Object o) {
			float f = dist - ((Result) o).dist;
			return f > 0 ? 1 : (f < 0 ? -1 : 0);
		}

		@Override
		public String toString() {
			return "" + dist + " " + a.getId() + " " + b.getId();
		}

		public Submission getA() {
			return a;
		}

		public Submission getB() {
			return b;
		}

		public float getDist() {
			return dist;
		}
	}

	/**
	 * Saves the analysis
	 * 
	 * @return an element that contains the whole document
	 * @throws IOException
	 */
	public Element saveToXML() throws IOException {
		Element root = new Element("analysis");
		root.setAttribute("version", VERSION_STRING);
		root.setAttribute("created", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));

		root.addContent(sourceSet.saveToXML());

		Element annotations = new Element("annotations");
		for (Submission s : subs) {
			if (!s.getAnnotations().isEmpty()) {
				annotations.addContent(s.saveToXML());
			}
		}
		root.addContent(annotations);

		Element tests = new Element("tests");
		for (Test t : appliedTests) {
			tests.addContent(t.saveToXML());
		}
		root.addContent(tests);

		return root;
	}

	/**
	 * Loads the analysis
	 * 
	 * @param root
	 * @throws IOException
	 */
	public void loadFromXML(Element root) throws IOException {
		String version = root.getAttributeValue("version");
		if (!version.equals(VERSION_STRING)) {
			log.warn("Loading from different version (" + version + "); " + " but this program uses save-version "
					+ VERSION_STRING);
		}

		log.info("Loading sources...");
		sourceSet.loadFromXML(root.getChild("sources"));
		loadSources(sourceSet);

		log.info("Loading annotations...");
		for (Element se : root.getChild("annotations").getChildren()) {
			Submission sub = getSubmission(se.getAttributeValue("id"));
			sub.loadFromXML(se);
		}

		log.info("Loading tests...");
		ArrayList<Test> pendingTests = new ArrayList<Test>();
		for (Element te : root.getChild("tests").getChildren()) {
			String tcn = te.getAttributeValue("class");
			try {
				Test t = (Test) getClass().getClassLoader().loadClass(tcn).newInstance();
				t.loadFromXML(te);
				pendingTests.add(t);
			} catch (Exception ex) {
				throw new IOException("Could not load test " + tcn);
			}
		}

		// now, run tests in an order that satisfies dependencies
		while (!pendingTests.isEmpty()) {
			boolean progress = false;
			for (Test candidate : pendingTests) {
				boolean dependenciesMet = false;
				for (String k : candidate.getRequires()) {
					if (!hasResultsForKey(k)) {
						log.info("Cannot execute " + candidate.getClass() + " without " + k + ": postponing");
						dependenciesMet = false;
						break;
					}
				}
				if (dependenciesMet) {
					log.info("Dependencies for " + candidate.getClass() + " satisfied, processing");
					progress = true;
					applyTest(candidate);
					pendingTests.remove(candidate);
				}
			}
			if (!progress) {
				throw new IOException("Impossible to meet dependencies for tests");
			}
		}
	}

	/**
	 * Reads an analysis from a file
	 *
	 * @param f the file to read from
	 * @throws IOException on any error (may wrap invalid internal XML errors)
	 */
	public void loadFromFile(File f) throws IOException {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(f);
			loadFromXML(doc.getRootElement());
		} catch (Exception e) {
			throw new IOException("Error loading from '" + f.getAbsolutePath() + "' xml save file", e);
		}
	}
}