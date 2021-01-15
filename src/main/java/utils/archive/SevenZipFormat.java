/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package utils.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import utils.FileUtils;

/**
 * Manages the 7-zip format.
 *
 * @author mfreire
 */
public class SevenZipFormat implements ArchiveFormat {

	private static final Logger log = LogManager.getLogger(SevenZipFormat.class);

	// 37 7A BC AF 27 1C, according to
	// https://en.wikipedia.org/wiki/List_of_file_signatures
	// casting to byte needed because Java does not recognize the existence of byte
	// literals
	private static byte[] sevenZipMagic = new byte[] { 0x37, 0x7A, (byte) 0xBC, (byte) 0xAF, 0x27 };

	public String getArchiveExtensions() {
		return "(.*\\.7z)";
	}

	private void assertIs7Zip(File source) throws IOException {
		try {
			if (!FileUtils.startMatches(new FileInputStream(source), sevenZipMagic, 0)) {
				throw new IOException("File is not a zip archive");
			}
		} catch (IOException ioe) {
			throw new IOException("File is not a zip archive", ioe);
		}
	}

	public ArrayList<String> list(File source) throws IOException {
		assertIs7Zip(source);

		SevenZFile zf = new SevenZFile(source);
		ArrayList<String> paths = new ArrayList<String>();

		for (SevenZArchiveEntry e : zf.getEntries()) {
			String name = FileUtils.toCanonicalPath(e.getName());
			if (e.isDirectory()) {
				continue;
			}

			paths.add(name);
		}
		return paths;
	}

	public void expand(File source, File destDir) throws IOException {
		assertIs7Zip(source);

		try (SevenZFile zf = new SevenZFile(source)) {
			log.info("Extracting 7zip: " + source.getName());

			while (true) {
				SevenZArchiveEntry e = zf.getNextEntry();
				if (e == null)
					return;

				String name = FileUtils.toCanonicalPath(e.getName());
				log.info(" - processing 7zip entry: " + name + " - " + e.getSize());

				if (e.isDirectory()) {
					// log.debug("\tExtracting directory "+e.getName());
					File dir = new File(destDir, name);
					dir.mkdirs();
					continue;
				}

				// log.debug("\tExtracting file "+name);
				File outFile = new File(destDir, name);
				if (!outFile.getParentFile().exists()) {
					// log.warn("weird 7z: had to create parent: "+outFile.getParentFile());
					outFile.getParentFile().mkdirs();
				}

				try (FileOutputStream fos = new FileOutputStream(outFile)) {
					byte[] b = new byte[(int) e.getSize()];
					zf.read(b);
					fos.write(b);
				}
			}
		}
	}

	public boolean extractOne(File source, String path, File dest) throws IOException {
		assertIs7Zip(source);

		try (SevenZFile zf = new SevenZFile(source)) {
			for (SevenZArchiveEntry e : zf.getEntries()) {
				String name = FileUtils.toCanonicalPath(e.getName());
				// System.err.println(" "+name+" =? "+path);
				if (!name.equals(path) || e.isDirectory()) {
					continue;
				}

				if (!dest.getParentFile().exists()) {
					// log.warn("weird 7z: had to create parent: "+outFile.getParentFile());
					dest.getParentFile().mkdirs();
				}

				try (FileOutputStream fos = new FileOutputStream(dest)) {
					byte[] b = new byte[(int) e.getSize()];
					zf.read(b);
					return true;
				}
			}
		}
		return false;
	}

	public void create(ArrayList<File> sources, File destFile, File baseDir) throws IOException {
		throw new IOException("Gzip does not support creation");
	}

	/**
	 * Simulates creation of a zip file, but returns only the size of the zip that
	 * results from the given input stream
	 */
	public int compressedSize(InputStream is) throws IOException {
		throw new IOException("Gzip does not support compression size");
	}
}
