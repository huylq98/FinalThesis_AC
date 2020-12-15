/*
 *@author ThomasLe
 *@date Dec 5, 2020
*/
package jsf.bean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.file.UploadedFile;

import core.Analysis.Result;
import core.Submission;
import jsf.lazymodel.LazyResultDataModel;
import ui.Main;
import ui.gui.CompareDialog;

@Named
@SessionScoped
public class FileUploadBean implements Serializable {

	private static final Logger log = LogManager.getLogger(FileUploadBean.class);
	private String fileToBeAnalyzed;
	public static List<Result> subResults = new ArrayList<>();
	private Result selectedAnalysis;
	private LazyDataModel<Result> filteredSubResults;
	private Float defaultDist = 0.05f;
	private LazyDataModel<Result> lazyModel;

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

	public void upload(FileUploadEvent event) {
		this.fileToBeAnalyzed = null;
		file = event.getFile();
		File dirToSaveSubmisison = null;
		try (InputStream input = file.getInputStream()) {
			dirToSaveSubmisison = Files.createTempDirectory("saved_location").toFile();
			Files.copy(input, new File(dirToSaveSubmisison, file.getFileName()).toPath());
			this.fileToBeAnalyzed = dirToSaveSubmisison.toString() + "\\" + file.getFileName();
			FacesMessage message = new FacesMessage("Upload Succesful!",
					event.getFile().getFileName() + " is uploaded to " + dirToSaveSubmisison);
			FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			dirToSaveSubmisison.deleteOnExit();
		}
	}

	public void analyze() {
		if (this.fileToBeAnalyzed != null) {
			Main.main(new String[] { this.fileToBeAnalyzed });
		}
	}

	public static Integer progress = 1;

	public Integer getProgress() {
		if (progress == null || Main.allDir.size() == 0) {
			return 0;
		}

		return Math.round((Float.parseFloat(String.valueOf(progress)) / Main.allDir.size()) * 100);
	}

	public void setProgress(Integer progressVal) {
		progress = progressVal;
	}

	public void onComplete() {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Analyze Completed!"));
	}

	public LazyDataModel<Result> getResults() {
//		return TestResultsDialog.results != null ? Arrays.asList(TestResultsDialog.results) : null;
		if (subResults != null) {
			lazyModel = new LazyResultDataModel(subResults);
		}
		return lazyModel;

	}

	public String getSubmissionSource(Submission subA, Submission subB, boolean sel) {
		if (subA == null || subB == null)
			return null;
		CompareDialog cd = new CompareDialog(subA, subB);
		cd.wrapAndHighlight(20);
		if (sel)
			return cd.getSourceA();
		else
			return cd.getSourceB();
	}
}
