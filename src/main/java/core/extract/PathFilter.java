/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.extract;

import java.io.File;

public class PathFilter extends PatternFilter {

	public PathFilter(String pattern) {
		super(pattern);
	}

	public PathFilter() {
	}

	public boolean accept(FileTreeNode ftn) {
		return ftn.getPath().matches(pattern);
	}

	public boolean accept(File f) {
		return f.getPath().matches(pattern);
	}

	@Override
	public String toString() {
		return "path='" + pattern + "'";
	}
}