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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.SourceSet;
import core.test.NCDTest;
import core.util.FileUtils;
import core.util.I18N;
import core.util.archive.ZipFormat;
import ui.extract.ZipSelectionPanel;
import ui.gui.MainGui;
import utils.Finder;

public class Main {

	private static final Logger log = LogManager.getLogger(Main.class);

	private static MainGui main;
	public static Path startingDir;
	public static List<Path> allDir = new ArrayList<>();
	public static Instant start;

	public static void main(String args[]) {
		I18N.setLang(Locale.getDefault().getLanguage());
		start = Instant.now();
		try {
//					File source = new File("C:\\Users\\Admin\\Downloads\\101.zip");
			File source = new File(args[0]);
			if (FileUtils.canUncompressPath(source)) {
				File temp = null;
				try {
					temp = Files.createTempDirectory("ac-temp").toFile();
					FileUtils.getArchiverFor(source.getPath()).expand(source, temp);
					log.info("Files for " + source.getPath() + " now at " + temp.getPath());
					source = temp;
				} catch (IOException e) {
					log.warn("error uncompressing bundled file for " + source, e);
				} finally {
					temp.deleteOnExit();
				}
			}
			filterFile(source.getPath(), "*.cpp");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		allDir.forEach(f -> {
			main = new MainGui();
			ZipSelectionPanel zsp = new ZipSelectionPanel();
			zsp.addSourceFile(f.toFile());
			zsp.filterPanel.addExpression(f.getFileName().toString() + "_B");
			zsp.filterPanel.confirm();
			ZipSelectionPanel.analyze();
			main.launchTest(new NCDTest(new ZipFormat()), true);
		});
	}

	public static void selectionConfirmed(SourceSet ss) {
		main.loadSources(ss);
	}

	public static void filterFile(String sourcePath, String pattern) throws IOException {
		startingDir = Paths.get(sourcePath);
		Finder finder = new Finder("C:\\Users\\Admin\\OneDrive\\Desktop\\Test", pattern, allDir);
		Files.walkFileTree(startingDir, finder);
	}
}
