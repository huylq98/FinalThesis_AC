/*
 *@author ThomasLe
 *@date Dec 6, 2020
*/
package utils;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Finder extends SimpleFileVisitor<Path> {

	private static final Logger log = LogManager.getLogger(Finder.class);
	private final Path savedPath;
	private final PathMatcher matcher;
	private final List<Path> allDir;

	public Finder(String path, String pattern, List<Path> allDir) {
		this.savedPath = Paths.get(path);
		this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
		this.allDir = allDir;
	}

	void find(Path subFile) throws IOException {
		if (subFile != null && matcher.matches(subFile.getFileName())) {
			StringBuilder savedLocation = new StringBuilder(savedPath.toString());
			savedLocation.append("\\").append(
					subFile.getFileName().toString().substring(0, subFile.getFileName().toString().indexOf("_")));
			Path separateDirForID = Paths.get(savedLocation.toString());
			if (!allDir.contains(separateDirForID)) {
				Files.createDirectories(separateDirForID);
				allDir.add(separateDirForID);
			}

			StringBuilder copyInfo = new StringBuilder("Copy ");
			log.info(copyInfo.append(subFile).append(" to ").append(separateDirForID));
			Files.copy(subFile, Paths.get(separateDirForID.toString() + "\\" + subFile.getFileName()), REPLACE_EXISTING);
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
