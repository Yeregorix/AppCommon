package net.smoofyuniverse.common.app;

import java.nio.file.Path;
import java.nio.file.Paths;

public enum OperatingSystem {
	WINDOWS {
		@Override
		public Path getWorkingDirectory() {
			String appdata = System.getenv("APPDATA");
			return Paths.get(appdata == null ? USER_HOME : appdata);
		}
	},
	MACOS {
		@Override
		public Path getWorkingDirectory() {
			return Paths.get(USER_HOME, "Library", "Application Support");
		}
	},
	LINUX,
	UNKNOWN;

	public Path getWorkingDirectory() {
		return Paths.get(USER_HOME);
	}

	public static final OperatingSystem CURRENT = getPlatform();
	public static final String USER_HOME = System.getProperty("user.home", ".");

	private static OperatingSystem getPlatform() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win"))
			return WINDOWS;
		if (osName.contains("mac"))
			return MACOS;
		if (osName.contains("linux") || osName.contains("unix"))
			return LINUX;
		return UNKNOWN;
	}
}
