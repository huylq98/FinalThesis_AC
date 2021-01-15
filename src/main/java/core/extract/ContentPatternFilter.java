/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.extract;

import java.io.File;

import utils.SourceFileCache;

public class ContentPatternFilter extends PatternFilter {

	public ContentPatternFilter(String pattern) {
		super(pattern);
	}

	public ContentPatternFilter() {
	}

	public boolean accept(FileTreeNode ftn) {
		return accept(ftn.getFile());
	}

	public boolean accept(File f) {
		String source = SourceFileCache.getSource(f);

		if (source == null) {
			System.err.println("File '" + f + "' could not be read!!!");
			return false;
		}

		return source.replaceAll("\\p{Space}+", " ").matches(pattern);
	}

	@Override
	public String toString() {
		return "has='" + pattern + "'";
	}
}
