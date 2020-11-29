/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core;

import core.extract.Hasher;
import core.util.SourceFileCache;
import core.util.XMLSerializable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.jdom2.Element;

public class Submission implements XMLSerializable {

	/**
	 * subject ID
	 */
	private final String id;
	/**
	 * subject ID
	 */
	private final String originalPath;
	/**
	 * subject internal index
	 */
	private int internalId;
	/**
	 * source code, as a list of filename+filecontents
	 */
	private final ArrayList<Source> sources = new ArrayList<Source>();

	private final ArrayList<Annotation> annotations = new ArrayList<>();

	private final static String annotationKey = "annotation";

	private String hash;

	private boolean hashUpToDate;

	/**
	 * Processed source, test run results, and so on the key 'annotations' is used
	 * to store an arraylist of annotations
	 */
	private final HashMap<String, Object> data = new HashMap<String, Object>();

	/**
	 * Basic constructor
	 * 
	 * @param id    used to identify this submission
	 * @param index used as an internal ID
	 */
	public Submission(String id, String originalPath, int index) {
		if (id.contains(".")) {
			id = id.substring(0, id.lastIndexOf("."));
		}
		this.id = id;
		this.internalId = index;
		this.originalPath = originalPath;
		data.put(annotationKey, annotations);
	}

	/**
	 * Adds a source-file
	 * 
	 * @param f source-file to add
	 */
	public void addSource(File f) {
		String source = SourceFileCache.getSource(f);
		sources.add(new Source(source, f.getName()));
	}

	public String getHash() {
		if (!hashUpToDate) {
			StringBuilder sb = new StringBuilder();
			for (Source s : getSources()) {
				sb.append(s.getCode());
			}
			hash = Hasher.hash(sb.toString());
			hashUpToDate = true;
		}
		return hash;
	}

	public String getId() {
		return id;
	}

	public void setInternalId(int internalId) {
		this.internalId = internalId;
	}

	public int getInternalId() {
		return internalId;
	}

	public String getOriginalPath() {
		return originalPath;
	}

	public boolean containsSource(String sourceName) {
		boolean sourceExists = false;

		for (Source s : getSources()) {
			if (s.getFileName().equals(sourceName)) {
				sourceExists = true;
				break;
			}
		}

		return sourceExists;
	}

	public ArrayList<Source> getSources() {
		return sources;
	}

	public String getSourceCode(int i) {
		return sources.get(i).getCode();
	}

	public String getSourceName(int i) {
		return sources.get(i).getFileName();
	}

	public Object getData(String key) {
		return data.get(key);
	}

	public void putData(String key, Object value) {
		data.put(key, value);
	}

	@Override
	public String toString() {
		return getId();
	}

	public Element saveToXML() throws IOException {
		Element se = new Element("submission");
		se.setAttribute("id", id);
		for (Annotation annotation : annotations) {
			se.addContent(annotation.saveToXML());
		}
		return se;
	}

	public void loadFromXML(Element element) throws IOException {
		annotations.clear();
		// load annotations
		for (Element ae : (List<Element>) element.getChildren("annotation")) {
			Annotation a = new Annotation();
			a.loadFromXML(ae);
			addAnnotation(a);
		}
	}

	/**
	 * Source code
	 */
	public static class Source {

		private final String code;
		private final String fileName;

		public Source(String code, String fileName) {
			this.code = code;
			this.fileName = fileName;
		}

		/**
		 * @return the code
		 */
		public String getCode() {
			return code;
		}

		/**
		 * @return the fileName
		 */
		public String getFileName() {
			return fileName;
		}
	}

	public void addAnnotation(Annotation annotation) {
		annotations.add(annotation);
	}

	public Collection<Annotation> getAnnotations() {
		return annotations;
	}
}
