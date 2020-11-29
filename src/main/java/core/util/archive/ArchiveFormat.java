/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.util.archive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public interface ArchiveFormat {

	/**
	 * Returns the pattern that describes the extensions that this archiver can
	 * process
	 */
	String getArchiveExtensions();

	/**
	 * List the contents of the archive
	 */
	ArrayList<String> list(File source) throws IOException;

	/**
	 * Expand the archive into its component files
	 */
	void expand(File source, File destDir) throws IOException;

	/**
	 * Expand the archive into its component files
	 */
	boolean extractOne(File source, String path, File dest) throws IOException;

	/**
	 * Return the *size* (in bytes) of compressing the input stream with this
	 * algorithm This allows the archiver to be used in compression-distance
	 * calculations, without the need to create intermediate files
	 */
	int compressedSize(InputStream is) throws IOException;

	/**
	 * Create an archive from the given sources; files in the archive are relative
	 * to baseDir.
	 */
	void create(ArrayList<File> sources, File destFile, File baseDir) throws IOException;
}
