/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package ui.gui;

import core.Analysis;
import ui.Main;
import core.test.Test;
import jsf.bean.FileUploadBean;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

public class GraphicalAnalysis implements ActionListener {

	private static final Logger log = LogManager.getLogger(GraphicalAnalysis.class);

	private javax.swing.Timer t;
	private Test test;
	private Analysis ac;
	private Runnable callback;
	private long startTime;
	private boolean isTestFinished = false;

	public GraphicalAnalysis(Analysis ac, Test test, Runnable callback) {
		this.ac = ac;
		this.callback = callback;
		this.test = test;
	}

	void start() {
		TestRunner runner = new TestRunner();
		Thread testThread = new Thread(runner);
		testThread.start();
		startTime = System.currentTimeMillis();
		t = new javax.swing.Timer(1000, this); // miliseconds
		t.setRepeats(true);
		t.start();
	}

	private class TestRunner implements Runnable {
		public void run() {
			try {
				ThreadContext.push("T-" + test);
				ac.prepareTest(test);
				ac.applyTest(test);
				isTestFinished = true;
				ThreadContext.pop();
			} catch (RuntimeException e) {
				java.io.StringWriter sw = new java.io.StringWriter();
				e.printStackTrace(new PrintWriter(sw));
			}
		}
	}

	public void actionPerformed(ActionEvent evt) {
		if (isTestFinished) {
			t.stop();
			FileUploadBean.progress++;
			log.info("Total time: " + Duration.between(Main.start, Instant.now()).toMillis() + "ms");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			callback.run();
		}
	}
}