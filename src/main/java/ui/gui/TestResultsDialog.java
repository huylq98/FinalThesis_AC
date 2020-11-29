/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package ui.gui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import bean.FileUploadBean;
import core.Analysis;

public class TestResultsDialog {
	private Analysis ac;
	private String testKey;
	public static Analysis.Result[] results;

	public TestResultsDialog(Analysis ac, String testKey) {
		this.ac = ac;
		this.testKey = testKey;
		showResults();
	}

	public void showResults() {
		Analysis.Result[] R = ac.sortTestResults(testKey);
		results = R;
		Charset charset = Charset.forName("US-ASCII");
		for (int i = 0; i < R.length; i++) {
			FileUploadBean.subResults.add(R[i]);
			String s = R[i].getA() + " - " + R[i].getB() + ": " + R[i].getDist();
			Path resultFile = Paths.get("C:\\Users\\Admin\\Downloads\\Result" + "\\"
					+ R[i].getA().toString().substring(0, R[i].getA().toString().indexOf("_")) + ".txt");
			if (Files.notExists(resultFile)) {
				try {
					Files.createFile(resultFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try (BufferedWriter writer = Files.newBufferedWriter(resultFile, charset, StandardOpenOption.APPEND)) {
				writer.write(s + "\n");
			} catch (IOException e) {
			}
		}
	}
}