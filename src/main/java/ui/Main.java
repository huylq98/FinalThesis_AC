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
import java.time.Duration;
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
	private static List<Path> allDir;
	public static Instant start;

	public static void main(String args[]) {
		I18N.setLang(Locale.getDefault().getLanguage());
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				start = Instant.now();
				allDir = new ArrayList<>();
				try {
//					File source = new File("C:\\Users\\Admin\\Downloads\\101.zip");
					File source = new File(args[0]);
					if (FileUtils.canUncompressPath(source)) {
						try {
							File temp = Files.createTempDirectory("ac-temp").toFile();
							temp.deleteOnExit();
							FileUtils.getArchiverFor(source.getPath()).expand(source, temp);
							log.info("Files for " + source.getPath() + " now at " + temp.getPath());
							source = temp;
						} catch (IOException e) {
							log.warn("error uncompressing bundled file for " + source, e);
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
				log.info("Total time: " + Duration.between(start, Instant.now()).toMillis() + "ms");
			}
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
