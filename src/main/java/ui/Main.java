/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import core.SourceSet;
import core.test.NCDTest;
import core.util.I18N;
import core.util.archive.ZipFormat;
import ui.extract.ZipSelectionPanel;
import ui.gui.MainGui;
import utils.Finder;

public class Main {

	private static MainGui main;
	public static Path startingDir;
	public static List<Path> allDir = new ArrayList<>();
	public static Instant start;

	public static void analyze(String filter, File source) {
		I18N.setLang(Locale.getDefault().getLanguage());
		start = Instant.now();
		filterFile(source.getPath(), filter);
		allDir.forEach(f -> {
			main = new MainGui();
			ZipSelectionPanel zsp = new ZipSelectionPanel();
			zsp.addSourceFile(f.toFile());
			zsp.filterPanel
				.addExpression(f.getFileName().toString() + "_B");
			zsp.filterPanel.confirm();
			ZipSelectionPanel.analyze();
			main.launchTest(new NCDTest(new ZipFormat()), true);
		});
	}
	
	public static void selectionConfirmed(SourceSet ss) {
		main.loadSources(ss);
	}

	public static void filterFile(String sourcePath, String pattern) {
		startingDir = Paths.get(sourcePath);
		Finder finder = null;
		File temp = null;
		try {
			temp = Files.createTempDirectory("ac-temp").toFile();
			finder = new Finder(temp.getPath(), pattern, allDir);
			Files.walkFileTree(startingDir, finder);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			temp.delete();
		}
	}
}
