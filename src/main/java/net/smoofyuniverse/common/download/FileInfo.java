/*
 * Copyright (c) 2017-2019 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.common.download;

import net.smoofyuniverse.common.app.App;
import net.smoofyuniverse.common.util.IOUtil;
import net.smoofyuniverse.common.util.StringUtil;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo {
	public final URL url;
	public final long size;
	public final String digest, digestInst;

	public Path file;

	public FileInfo(URL url, long size, String digest, String digestInst) {
		if (url == null)
			throw new IllegalArgumentException("url");
		if (size < 0 && size != -1)
			throw new IllegalArgumentException("Size must be positive or indefinite");

		this.url = url;
		this.size = size;
		this.digest = digest;
		this.digestInst = digestInst;
	}

	public boolean matches() {
		return matches(this.file);
	}

	public boolean matches(Path file) {
		try {
			if (!Files.isRegularFile(file))
				return false;

			if (this.size != -1 && this.size != Files.size(file))
				return false;

			if (this.digest != null && this.digestInst != null && !this.digest.equals(StringUtil.toHexString(IOUtil.digest(file, this.digestInst))))
				return false;
		} catch (Exception e) {
			App.getLogger("FileInfo").warn("Failed to check file " + file, e);
			return false;
		}
		return true;
	}
}
