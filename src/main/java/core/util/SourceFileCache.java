/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.util;

import core.util.archive.ArchiveFormat;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SourceFileCache {

	private static final Logger log = LogManager.getLogger(SourceFileCache.class);

	private static final WeakHashMap<File, String> cache = new WeakHashMap<File, String>();

	private static final HashSet<File> important = new HashSet<File>();

	/**
	 * Marks a file as important
	 */
	public static void markImportant(File f) {
		important.add(f);
	}

	public static void clearImportant() {
		important.clear();
	}

	/**
	 * Set the source (use with caution; messing up with archive file contents can
	 * cause problems further on)
	 */
	public static void setSource(File f, String s) {
		cache.put(f, s);
	}

	/**
	 * A simplified version of getSource; never reloads
	 */
	public static String getSource(File f) {
		return getSource(f, false);
	}

	/**
	 * Retrieves a file's source, regardless of whether it had been read before or
	 * not; if the file does not exist, it should at least be a 'virtual pathname'
	 * stemming from a file that does
	 *
	 * The source of a compressed file is its table of contents, as retrieved by
	 * getArchiveListing
	 */
	public static String getSource(File f, boolean forceReload) {
		String source = null;

		if (cache.containsKey(f) && !forceReload) {
			source = cache.get(f);
		} else {
			try {
				if (f.exists()) {
					source = read(f);
				} else {
					File p = f.getParentFile();
					StringBuilder path = new StringBuilder(f.getName());
					while (p != null && !p.exists()) {
						path.insert(0, p.getName() + "/");
						p = p.getParentFile();
					}
					if (p == null) {
						log.warn("Bad path - could not locate existing file " + path.toString() + " from " + f);
						return null;
					}

					ArchiveFormat ar = FileUtils.getArchiverFor(p.getName());
					if (ar == null) {
						log.warn("No archiver found for '" + p.getName() + "'");
						return null;
					}

					File tmp = File.createTempFile("siglefile", ".tmp");
					if (ar.extractOne(p, path.toString(), tmp)) {
						source = read(tmp);
					} else {
						log.warn("Weird error extracting file '" + f + "' from file '" + p.getAbsolutePath()
								+ "' with path '" + path.toString() + "'");
					}
					tmp.delete();
				}
			} catch (IOException ioe) {
				log.error("Exception reading file '" + f + "'", ioe);
				return null;
			}
			cache.put(f, source);
		}

		return source;
	}

	private static String read(File f) throws IOException {
		if (FileUtils.canUncompressPath(f)) {
			getArchiveListing(f);
			return cache.get(f);
		} else {
			return FileUtils.readFileToString(f);
		}
	}

	/**
	 * Retrieves the listing for an archive, regardless of whether it had been read
	 * before or not
	 */
	public static ArrayList<String> getArchiveListing(File f) {
		String listing;
		ArrayList<String> al = null;

		if (cache.containsKey(f)) {
			al = new ArrayList<String>();
			listing = cache.get(f);
			for (StringTokenizer st = new StringTokenizer(listing, "\n"); st.hasMoreTokens(); /**/) {
				al.add(st.nextToken());
			}
		} else {
			try {
				// System.err.println("Processing f: "+f);
				al = FileUtils.getArchiverFor(f.getName()).list(f);
				StringBuffer sb = new StringBuffer();
				for (String s : al) {
					sb.append(s + "\n");
				}
				listing = sb.toString();
			} catch (IOException ioe) {
				log.error("Exception reading file '" + f + "'", ioe);
				return null;
			}
			cache.put(f, listing);
		}

		return al;
	}
}
