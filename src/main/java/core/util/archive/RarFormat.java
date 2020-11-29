/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.util.archive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import com.github.junrar.exception.RarException;
import com.github.junrar.impl.FileVolumeManager;
import com.github.junrar.rarfile.FileHeader;
import com.github.junrar.Archive;
import core.util.FileUtils;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Unrar driver
 *
 * @author mfreire
 */
public class RarFormat implements ArchiveFormat {

	private static byte[] rarMagic = new byte[] { 0x52, 0x61, 0x72 };

	public String getArchiveExtensions() {
		return ".*\\.rar";
	}

	private void assertIsRar(File source) throws IOException {
		try {
			if (!FileUtils.startMatches(new FileInputStream(source), rarMagic, 0)) {
				throw new IOException("File is not a rar archive");
			}
		} catch (IOException ioe) {
			throw new IOException("File is not a rar archive", ioe);
		}
	}

	public void expand(File source, File destDir) throws IOException {
		assertIsRar(source);
		Archive a = null;
		try {
			a = new Archive(new FileVolumeManager(source));
			FileHeader fh = a.nextFileHeader();
			while (fh != null) {
				String name = FileUtils.toCanonicalPath(fh.getFileNameString().trim());
				int lastSlash = name.lastIndexOf('/');
				if (lastSlash > 0) {
					// attempt to create dirs
					File d = new File(destDir, name.substring(0, lastSlash));
					if (!d.isDirectory()) {
						d.mkdirs();
					}
				}
				File out = new File(destDir, name);
				if (!out.isDirectory()) {
					FileOutputStream os = new FileOutputStream(out);
					a.extractFile(fh, os);
					os.close();
				}
				fh = a.nextFileHeader();
			}
		} catch (RarException re) {
			throw new IOException("Cannot read rar file", re);
		} finally {
			if (a != null)
				a.close();
		}
	}

	public ArrayList<String> list(File source) throws IOException {
		assertIsRar(source);

		ArrayList<String> paths = new ArrayList<String>();
		Archive a = null;
		try {
			a = new Archive(new FileVolumeManager(source));
			FileHeader fh = a.nextFileHeader();
			while (fh != null) {
				String name = FileUtils.toCanonicalPath(fh.getFileNameString().trim());
				paths.add(name);
				fh = a.nextFileHeader();
			}
		} catch (RarException re) {
			throw new IOException("Cannot read rar file", re);
		} finally {
			if (a != null)
				a.close();
		}
		return paths;
	}

	public void create(ArrayList<File> sources, File destFile, File baseDir) throws IOException {
		throw new UnsupportedOperationException("Cannot create RAR files... yet");
	}

	public int compressedSize(InputStream is) throws IOException {
		throw new UnsupportedOperationException("Fast RAR compressed size calculation not supported");
	}

	public boolean extractOne(File source, String path, File destDir) throws IOException {
		assertIsRar(source);

		boolean found = false;
		Archive a = null;
		try {
			a = new Archive(new FileVolumeManager(source));
			FileHeader fh = a.nextFileHeader();
			while (fh != null && !found) {
				String name = FileUtils.toCanonicalPath(fh.getFileNameString().trim());
				int lastSlash = name.lastIndexOf('/');
				if (lastSlash > 0 && path.startsWith(name)) {
					// attempt to create dirs
					File d = new File(destDir, name.substring(0, lastSlash));
					if (!d.isDirectory()) {
						d.mkdirs();
					}
				}
				if (path.equals(name)) {
					File out = new File(destDir, name);
					FileOutputStream os = new FileOutputStream(out);
					a.extractFile(fh, os);
					os.close();
					found = true;
				}
				fh = a.nextFileHeader();
			}
		} catch (RarException re) {
			throw new IOException("Cannot read rar file", re);
		} finally {
			if (a != null)
				a.close();
		}
		return found;
	}
}