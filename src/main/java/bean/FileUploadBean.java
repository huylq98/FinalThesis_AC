/*
 *@author ThomasLe
 *@date Dec 5, 2020
*/
package bean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import core.Analysis;
import core.Submission;
import ui.Main;
import ui.gui.CompareDialog;

@Named
@SessionScoped
public class FileUploadBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LogManager.getLogger(FileUploadBean.class);
	private String fileToBeAnalyzed;
	public static List<Analysis.Result> subResults;
	private Analysis.Result selectedAnalysis;

	public String getFileToBeAnalyzed() {
		return fileToBeAnalyzed;
	}

	public Analysis.Result getSelectedAnalysis() {
		return selectedAnalysis;
	}

	public void setSelectedAnalysis(Analysis.Result selectedAnalysis) {
		this.selectedAnalysis = selectedAnalysis;
	}

	public void upload(FileUploadEvent event) {
		this.fileToBeAnalyzed = null;
		subResults = new ArrayList<>();
		progress = 0;
		
		UploadedFile file = event.getFile();
		File dirToSaveSubmisison = null;
		try (InputStream input = file.getInputstream()) {
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

	private static Integer progress;

	public Integer getProgress() {
		if (progress == null) {
			progress = 0;
		}
		return progress;
	}

	public static void setProgress(Integer p) {
		progress = p;
	}

	public void onComplete() {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Analyze Completed!"));
	}

	public List<Analysis.Result> getResults() {
//		return TestResultsDialog.results != null ? Arrays.asList(TestResultsDialog.results) : null;
		return subResults != null ? subResults : null;
	}
	
	public String getSubmissionSource(Submission subA, Submission subB, boolean sel) {
		if(subA == null || subB == null) return null;
		CompareDialog cd = new CompareDialog(subA, subB);
		cd.wrapAndHighlight(20);
		if(sel) return cd.getSourceA();
		else return cd.getSourceB();
	}
}
