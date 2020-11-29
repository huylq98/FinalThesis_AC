/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package ui.extract;

import ui.Main;
import core.SourceSet;
import core.expression.CompositeBooleanExp;
import core.expression.FilterExpression;
import core.extract.CompositeFilter;
import core.extract.FileTreeFilter;
import core.extract.FileTreeModel;
import core.extract.FileTreeNode;
import core.expression.ExpressionListener;
import core.expression.Expression;
import ui.expression.CompositeExpressionPanel;
import core.util.FileUtils;
import core.util.SourceFileCache;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import javax.swing.tree.TreePath;

public class ZipSelectionPanel {

	private static final Logger log = LogManager.getLogger(ZipSelectionPanel.class);

	/**
	 * The left-hand-side, a view of the filesystem where submission roots can be
	 * selected.
	 */
	private FileTreeModel fileTreeModel;
	public CompositeExpressionPanel filterPanel;
	private CompositeBooleanExp filterExpression;

	/**
	 * The right-hand side, for each selected submission roots shows its contents,
	 * and allows some of them to be filtered out.
	 */
	private static FileTreeModel selectedFilesModel;
	private CompositeExpressionPanel fileSelFilterPanel;
	private CompositeBooleanExp fileSelFilterExpression;

	/** Creates new form ZipSelectionPanel */
	public ZipSelectionPanel() {
		fileTreeModel = new FileTreeModel();

		filterPanel = new CompositeExpressionPanel(null);
		filterExpression = new CompositeBooleanExp(new CompositeFilter());
		filterPanel.setExpression(filterExpression);
		filterPanel.addExpressionListener(new ZipSelListener());
		fileSelFilterPanel = new CompositeExpressionPanel(null);
		fileSelFilterExpression = new CompositeBooleanExp(new CompositeFilter());
		fileSelFilterPanel.setExpression(fileSelFilterExpression);
		fileSelFilterPanel.addExpressionListener(new FileSelListener());

		selectedFilesModel = new FileTreeModel();
	}

	public class ZipSelListener implements ExpressionListener {
		public void expressionChanged(Expression e, boolean test) {
			if (!test) {
				reloadFileSelTree(true);
			}
		}
	}

	public void reloadFileSelTree(boolean fromScratch) {

		log.info("Reloading sel. files");

		if (fromScratch) {
			selectedFilesModel.clear();
			// Initializes with a fresh set of files
			FileTreeFilter zf = filterExpression.getFilter();
			TreePath[] allPaths = fileTreeModel.findWithFilter(zf, false, false);
			for (TreePath tp : allPaths) {
				addSubmissionNode(fileTreeModel.getNodeFor(tp));
			}
		}

		// Purges out those that do not match the filter
		FileTreeFilter ff = fileSelFilterExpression.getFilter();
		TreePath[] selPaths = selectedFilesModel.findWithFilter(ff, true, false);
		HashSet<FileTreeNode> valid = new HashSet<FileTreeNode>();
		for (TreePath tp : selPaths) {
			valid.add((FileTreeNode) tp.getLastPathComponent());
		}

		boolean removedSomething = true;
		while (removedSomething) {
			removedSomething = false;
			ArrayList<FileTreeNode> allNodes = selectedFilesModel.getAllTerminals();
			for (FileTreeNode n : allNodes) {
				if (!valid.contains(n)) {
					try {
						selectedFilesModel.removeNodeFromParent(n);
						removedSomething = true;
						log.info("removed " + n + " (" + n.getPath() + ") from parent");
					} catch (Exception e) {
						log.warn("could not remove " + n + " (" + n.getPath() + ") from parent", e);
					}
				}
			}
		}
	}

	public class FileSelListener implements ExpressionListener {
		public void expressionChanged(Expression e, boolean test) {
			if (!test) {
				reloadFileSelTree(false);
			} else {
				FileTreeFilter ff = ((FilterExpression) e).getFilter();
				selectedFilesModel.findWithFilter(ff, true, false);
			}
		}
	}

	public void addSourceFile(File f) {
		log.info("Adding source: " + f);
		if (f == null || !f.exists()) {
			log.warn("Ignored: null or no longer there");
			return;
		} else {
			fileTreeModel.addSource(f);
		}
	}

	public void addSubmissionNode(FileTreeNode fn) {
		selectedFilesModel.addSource(fn);
	}

	/**
	 * Adds a single file. If it happens to be a folder with folders, then each
	 * 1st-level subfolder will be considered a submission.
	 * 
	 * @param f
	 */
	public void addSubmissionFile(File f) {
		log.info("Adding submission file: " + f);

		if (f == null || !f.exists()) {
			log.warn("Ignored: null or no longer there");
			return;
		}

		if (f.isDirectory()) {
			boolean isHierarchy = false;
			File[] listing = f.listFiles();
			Arrays.sort(listing, new FileTreeNode.FileSorter());
			for (File s : listing) {
				if (s.isDirectory()) {
					isHierarchy = true;
					break;
				}
			}
			if (isHierarchy) {
				for (File s : listing) {
					if (s.isDirectory()) {
						selectedFilesModel.addSource(s);
					}
				}
				return;
			}
		}
		selectedFilesModel.addSource(f);
	}

	public boolean uncompress(File dest) {
		try {
			if (!dest.exists()) {
				dest.mkdirs();
			}
			for (FileTreeNode n : selectedFilesModel.getAllTerminals()) {
				File p = ((FileTreeNode) n.getParent()).getFile();
				File f = n.getFile();

				String pname = p.getName();
				String id = (pname.contains(".")) ? pname.substring(0, pname.lastIndexOf(".")) : pname;

				File destDir = new File(dest, id);

				if (!destDir.exists()) {
					destDir.mkdir();
				}

				String name = f.getName();
				String contents = SourceFileCache.getSource(f);

				FileUtils.writeStringToFile(new File(destDir, name), contents);
			}
		} catch (IOException ioe) {
			log.error("error uncompressing files", ioe);
			return false;
		}
		return true;
	}

	public static void analyze() {
		try {
			SourceSet ss = new SourceSet((FileTreeNode) selectedFilesModel.getRoot());
			Main.selectionConfirmed(ss);
		} catch (IOException ioe) {
			log.error("Error exporting sources ", ioe);
		}
	}
}
