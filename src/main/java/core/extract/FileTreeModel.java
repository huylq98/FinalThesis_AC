/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.extract;

import org.apache.logging.log4j.Logger;

import utils.FileUtils;

import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class FileTreeModel extends DefaultTreeModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LogManager.getLogger(FileTreeModel.class);

	private FileTreeNode root;

	/**
	 * Creates a new instance of FileTreeModel
	 */
	public FileTreeModel() {
		super(new FileTreeNode(null, null));
		this.root = (FileTreeNode) getRoot();
	}

	/**
	 * Add a new file, compressed file or folder into the system.
	 * 
	 * @param f file/archive/folder to add
	 * @return the tree-path for the newly added file/archive/folder
	 */
	public void addSource(File f) {
		try {
			log.info("Adding source: " + f);
			FileTreeNode n = new FileTreeNode(f, root, FileUtils.canUncompressPath(f));
			insertNodeInto(n, root, findIndexFor(n, root));
		} catch (Exception e) {
			log.warn("Error reading '" + f.getAbsolutePath() + "': " + e);
			e.printStackTrace();
		}
	}

	/**
	 * Add a copy of a fileTreeNode from another tree into this one
	 * 
	 * @param fn
	 * @return
	 */
	public void addSource(FileTreeNode fn) {
		FileTreeNode n = new FileTreeNode(fn);
		insertNodeInto(n, root, findIndexFor(n, root));
		n.setParent(root);
	}

	/**
	 * Returns correct index for ordered insertion of child in parent
	 * 
	 * @param n      node to insert
	 * @param parent to insert into
	 * @return index to insert into, from 0 to parent.getChildCount() inclusive
	 */
	private static int findIndexFor(FileTreeNode n, FileTreeNode parent) {
		String key = n.getFile().getAbsolutePath();
		for (int i = 0; i < parent.getChildCount(); i++) {
			String other = ((FileTreeNode) parent.getChildAt(i)).getFile().getAbsolutePath();
			if (key.compareToIgnoreCase(other) < 0) {
				return i;
			}
		}
		return parent.getChildCount();
	}

	/**
	 * Returns the file for a given treepath
	 */
	public FileTreeNode getNodeFor(TreePath tp) {
		return (FileTreeNode) tp.getLastPathComponent();
	}

	/**
	 * Finds all paths that match the given filter
	 */
	public TreePath[] findWithFilter(FileTreeFilter ff, boolean onlyFiles, boolean recurseIfFound) {

		ArrayList<TreePath> al = new ArrayList<TreePath>();
		Stack<FileTreeNode> s = new Stack<FileTreeNode>();
		s.push(root);
		// System.err.println("FileFilter is "+ff);
		for (FileTreeNode n : root.getChildren()) {
			findInternal(n, ff, al, s, onlyFiles, recurseIfFound);
		}
		return al.toArray(new TreePath[al.size()]);
	}

	/**
	 * Finds all paths that match the given filter, starting from a node; If a
	 * parent matches, children will not be searched
	 */
	public void findInternal(FileTreeNode n, FileTreeFilter ff, ArrayList<TreePath> found, Stack<FileTreeNode> s,
			boolean onlyFiles, boolean recurseIfFound) {

		n.refresh();

		s.push(n);

		log.debug("testing " + n.getFile() + " path: " + n.getPath());

		if (!onlyFiles || !n.getAllowsChildren()) {
			if (ff.accept(n)) {
				found.add(new TreePath(s.toArray()));
				log.debug("found " + n.getFile() + " to match!");
				if (!recurseIfFound) {
					s.pop();
					return;
				}
			}
		}
		if (!n.isLeaf()) {
			for (FileTreeNode c : n.getChildren()) {
				findInternal(c, ff, found, s, onlyFiles, recurseIfFound);
			}
		}
		s.pop();
	}

	/**
	 * Returns a list with all terminal nodes
	 */
	public ArrayList<FileTreeNode> getAllTerminals() {
		ArrayList<FileTreeNode> al = new ArrayList<FileTreeNode>();
		getAllTerminals(root, al);
		return al;
	}

	/**
	 * Internal version of the above
	 */
	private void getAllTerminals(FileTreeNode n, ArrayList<FileTreeNode> al) {
		if (n.isLeaf()) {
			al.add(n);
		} else {
			for (FileTreeNode c : n.getChildren()) {
				getAllTerminals(c, al);
			}
		}
	}

	/**
	 * Clears the model
	 */
	public void clear() {
		root.getChildren().clear();
		reload();
	}
}
