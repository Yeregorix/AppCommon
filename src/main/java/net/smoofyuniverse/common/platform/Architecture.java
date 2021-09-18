/*
 * Copyright (c) 2017-2021 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.common.platform;

import java.util.Locale;

/**
 * A processor architecture.
 */
public enum Architecture {
	X86,
	X64,
	ARM32,
	ARM64,
	UNKNOWN;

	/**
	 * The current architecture.
	 */
	public static final Architecture CURRENT = getCurrent();

	private static Architecture getCurrent() {
		String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
		if (arch.matches("^(x8664|amd64|ia32e|em64t|x64)$"))
			return X64;
		if (arch.matches("^(x8632|x86|i[3-6]86|ia32|x32)$"))
			return X86;
		if (arch.matches("^(aarch64|arm64)$"))
			return ARM64;
		if (arch.matches("^(arm|arm32)$"))
			return ARM32;
		return UNKNOWN;
	}
}
