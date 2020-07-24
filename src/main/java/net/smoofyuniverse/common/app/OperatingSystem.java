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
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

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

	public static final OperatingSystem CURRENT = getPlatform();
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

	public Path getWorkingDirectory() {
		return Paths.get(USER_HOME);
	}

	public void openLink(URI uri) {
		try {
			Desktop.getDesktop().browse(uri);
		} catch (Exception e) {
			if (this == MACOS) {
				try {
					ProcessUtil.builder().command("/usr/bin/open", uri.toASCIIString()).start();
				} catch (IOException e2) {
					App.getLogger("OperatingSystem").warn("Failed to open link: " + uri, e2);
				}
			} else
				App.getLogger("OperatingSystem").warn("Failed to open link: " + uri, e);
		}
	}
}
