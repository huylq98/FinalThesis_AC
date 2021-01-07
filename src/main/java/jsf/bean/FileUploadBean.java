/*
 *@author ThomasLe
 *@date Dec 5, 2020
*/
package jsf.bean;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.TreeNode;
import org.primefaces.model.file.UploadedFile;

import core.Analysis.Result;
import core.util.FileUtils;
import core.Submission;
import jsf.lazymodel.LazyResultDataModel;
import ui.Main;
import ui.gui.CompareDialog;

@Named
@ViewScoped
public class FileUploadBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LogManager.getLogger(FileUploadBean.class);
	private String fileToBeAnalyzed;
	public static List<Result> subResults = new ArrayList<>();
	private Result selectedAnalysis;
	private LazyDataModel<Result> filteredSubResults;
	private Float defaultDist = 0.5f;
	private LazyDataModel<Result> lazyModel;
	private TreeNode root;
	private File uncompressedFile;

	private UploadedFile file;

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public LazyDataModel<Result> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Result> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public String getFileToBeAnalyzed() {
		return fileToBeAnalyzed;
	}

	public Result getSelectedAnalysis() {
		return selectedAnalysis;
	}

	public void setSelectedAnalysis(Result selectedAnalysis) {
		this.selectedAnalysis = selectedAnalysis;
	}

	public LazyDataModel<Result> getFilteredSubResults() {
		return filteredSubResults;
	}

	public void setFilteredSubResults(LazyDataModel<Result> filteredSubResults) {
		this.filteredSubResults = filteredSubResults;
	}

	public Float getDefaultDist() {
		return defaultDist;
	}

	public void setDefaultDist(Float defaultDist) {
		this.defaultDist = defaultDist;
	}

	public TreeNode getRoot() {
		return root;
	}

	public void upload(FileUploadEvent event) {
		file = event.getFile();
		File dirToSaveSubmisison = null;
		progress = 1;
		try (InputStream input = file.getInputStream()) {
			dirToSaveSubmisison = Files.createTempDirectory("saved_location").toFile();
			Files.copy(input, new File(dirToSaveSubmisison, file.getFileName()).toPath());
			this.fileToBeAnalyzed = dirToSaveSubmisison.toString() + "\\" + file.getFileName();
			
			this.uncompressedFile = uncompress(new File(this.fileToBeAnalyzed));
			createRoot(this.uncompressedFile.getPath(), "*.cpp");
			FacesMessage message = new FacesMessage("Upload Succesful!",
					event.getFile().getFileName() + " is uploaded to " + dirToSaveSubmisison);
			FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			dirToSaveSubmisison.deleteOnExit();
		}
	}

	private void createRoot(String sourcePath, String pattern) throws IOException {
		root = new DefaultTreeNode("Root", null);
		BeanFinder finder = new BeanFinder(pattern);
		Files.walkFileTree(Paths.get(sourcePath), finder);
	}
	
	public File uncompress(File compressedFile) {
		if (FileUtils.canUncompressPath(compressedFile)) {
			File temp = null;
			try {
				temp = Files.createTempDirectory("ac-temp").toFile();
				FileUtils.getArchiverFor(compressedFile.getPath()).expand(compressedFile, temp);
				log.info("Files for " + compressedFile.getPath() + " now at " + temp.getPath());
				compressedFile = temp;
			} catch (IOException e) {
				log.warn("error uncompressing bundled file for " + compressedFile, e);
			} finally {
				temp.deleteOnExit();
			}
		}
		return compressedFile;
	}

	public void analyze() {
		if (this.fileToBeAnalyzed != null) {
			Main.analyze("*", this.uncompressedFile);
		}
	}

	public static int progress = 1;

	public Integer getProgress() {
		if (Main.allDir.size() == 0) {
			return 0;
		}

		return Math.round((Float.parseFloat(String.valueOf(progress)) / Main.allDir.size()) * 100);
	}

	public void setProgress(Integer progressVal) {
		progress = progressVal;
	}

	public void onComplete() {
		if (subResults != null) {
			lazyModel = new LazyResultDataModel(subResults);
		}
		root = null;
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Analyze Completed!"));
	}

	public LazyDataModel<Result> getResults() {
		return lazyModel;

	}

	public String getSubmissionSource(Submission subA, Submission subB, boolean sel) {
		if (subA == null || subB == null)
			return null;
		CompareDialog cd = new CompareDialog(subA, subB);
		cd.wrapAndHighlight(10);
		List<Integer[]> currentLocationsOfA = new ArrayList<>();
		List<Integer[]> currentLocationsOfB = new ArrayList<>();

		cd.getIntervalA().forEach(interval -> currentLocationsOfA.add(new Integer[] { interval.so, interval.eo }));
		cd.getIntervalB().forEach(interval -> currentLocationsOfB.add(new Integer[] { interval.so, interval.eo }));

		String submissionA = cd.getSourceA();
		String submissionB = cd.getSourceB();

		List<String> partOfSourceA = new ArrayList<>();
		List<String> partOfSourceB = new ArrayList<>();
		for (Integer[] location : currentLocationsOfA) {
			partOfSourceA.add(submissionA.substring(location[0], location[1]));
		}
		for (Integer[] location : currentLocationsOfB) {
			partOfSourceB.add(submissionB.substring(location[0], location[1]));
		}

		for (String part : partOfSourceA) {
			submissionA = submissionA.replace(part, "<span style=\"background-color: #fdffbc;\">" + part + "</span>");
		}

		for (String part : partOfSourceB) {
			submissionB = submissionB.replace(part, "<span style=\"background-color: #fdffbc;\">" + part + "</span>");
		}
		if (sel)
			return submissionA;
		else
			return submissionB;
	}

	private class BeanFinder extends SimpleFileVisitor<Path> {

		private final PathMatcher matcher;
		private List<String> examCodes;

		public BeanFinder(String pattern) {
			examCodes = new ArrayList<>();
			this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
		}

		void find(Path subFile) throws IOException {
			if (subFile != null && matcher.matches(subFile.getFileName())) {
				String examCode = subFile.getFileName().toString().substring(0,
						subFile.getFileName().toString().indexOf("_"));
				TreeNode examCodeNode = new DefaultTreeNode(examCode);
				if (!examCodes.contains(examCode)) {
					examCodes.add(examCode);
					root.getChildren().add(examCodeNode);
				}
				root.getChildren().forEach(node ->{
					if(node.getData().toString().equals(examCode)) {
						node.getChildren().add(new DefaultTreeNode(subFile.getFileName().toString()));
					}
				});
			}
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			find(file);
			return CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			find(dir);
			return CONTINUE;
		}
	}
}
