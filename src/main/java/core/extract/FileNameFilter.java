/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.extract;

import java.io.File;

public class FileNameFilter extends PatternFilter {

	public FileNameFilter(String pattern) {
		super(pattern);
	}

	public FileNameFilter() {
	}

	public boolean accept(FileTreeNode ftn) {
		return ftn.getLabel().matches(pattern);
	}

	public boolean accept(File f) {
		return f.getName().matches(pattern);
	}

	@Override
	public String toString() {
		return "name='" + pattern + "'";
	}
}
