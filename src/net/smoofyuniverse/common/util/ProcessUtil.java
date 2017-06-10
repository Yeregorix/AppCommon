package net.smoofyuniverse.common.util;

import java.nio.file.Path;
import net.smoofyuniverse.common.app.Application;

public class ProcessUtil {
	
	public static ProcessBuilder builder() {
		return builder(Application.get().getWorkingDirectory());
	}
	
	public static ProcessBuilder builder(Path dir) {
		return new ProcessBuilder().directory(dir.toFile());
	}
}
