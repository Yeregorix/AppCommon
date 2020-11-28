/*
 * Copyright (c) 2017-2020 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.smoofyuniverse.common.app;

import net.smoofyuniverse.common.util.ProcessUtil;

import java.awt.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * An operating system.
 */
public enum OperatingSystem {
	WINDOWS {
		@Override
		public Path getApplicationDirectory() {
			String appdata = System.getenv("APPDATA");
			return Paths.get(appdata == null ? USER_HOME : appdata);
		}
	},
	MACOS {
		@Override
		public Path getApplicationDirectory() {
			return Paths.get(USER_HOME, "Library", "Application Support");
		}

		@Override
		protected boolean browseFallback(URI uri) throws Exception {
			ProcessUtil.builder().command("/usr/bin/open", uri.toASCIIString()).start();
			return true;
		}
	},
	LINUX,
	UNKNOWN;

	/**
	 * The current operating system.
	 */
	public static final OperatingSystem CURRENT = getPlatform();

	/**
	 * The value of system property {@code user.home}.
	 */
	public static final String USER_HOME = System.getProperty("user.home", ".");

	private static OperatingSystem getPlatform() {
		String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
		if (osName.contains("win"))
			return WINDOWS;
		if (osName.contains("mac"))
			return MACOS;
		if (osName.contains("linux") || osName.contains("unix"))
			return LINUX;
		return UNKNOWN;
	}

	/**
	 * Gets the preferred directory where applications should be stored.
	 *
	 * @return The directory where applications should be stored.
	 */
	public Path getApplicationDirectory() {
		return Paths.get(USER_HOME);
	}

	/**
	 * Launches the default browser to display an URI.
	 *
	 * @param uri the URI to be displayed.
	 * @return Whether the operation was successful.
	 */
	public boolean browse(URI uri) {
		if (uri == null)
			throw new IllegalArgumentException("uri");

		try {
			Desktop.getDesktop().browse(uri);
			return true;
		} catch (Exception ignored) {
		}

		try {
			return browseFallback(uri);
		} catch (Exception e) {
			return false;
		}
	}

	protected boolean browseFallback(URI uri) throws Exception {
		return false;
	}
}
