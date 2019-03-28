/*
 * Copyright (c) 2017 Hugo Dupanloup (Yeregorix)
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
import net.smoofyuniverse.common.task.IncrementalListenerProvider;
import net.smoofyuniverse.common.util.IOUtil;
import net.smoofyuniverse.common.util.StringUtil;
import net.smoofyuniverse.logger.core.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class FileDownloadTask {
	private static final Logger logger = App.getLogger("FileDownloadTask");
	
	public final ConnectionConfiguration config;
	public final boolean isDirectory;
	public final URL url;
	
	private String expectedDigest, digestInstance;
	private long expectedSize;
	
	private Path path;
	
	public FileDownloadTask(URL url, Path path, long expectedSize, String expectedDigest, String digestInstance) {
		this(url, path, App.get().getConnectionConfig(), expectedSize, expectedDigest, digestInstance);
	}
	
	public FileDownloadTask(URL url, Path path, ConnectionConfiguration config, long expectedSize, String expectedDigest, String digestInstance) {
		if (expectedSize < 0 && expectedSize != -1)
			throw new IllegalArgumentException("Size must be positive or indefinite");
		this.url = url;
		this.path = path;
		this.config = config;
		this.isDirectory = url.getFile().endsWith("/");
		if (!this.isDirectory) {
			this.expectedSize = expectedSize;
			this.expectedDigest = expectedDigest;
			this.digestInstance = digestInstance;
		}
	}
	
	public Optional<String> expectedDigest() {
		return Optional.ofNullable(this.expectedDigest);
	}
	
	public Optional<String> digestInstance() {
		return Optional.ofNullable(this.digestInstance);
	}
	
	public long expectedSize() {
		return this.expectedSize;
	}
	
	public Path getPath() {
		return this.path;
	}
	
	public void setPath(Path p) {
		if (p == null)
			throw new IllegalArgumentException("path");
		this.path = p;
	}
	
	public Optional<String> localDigest() {
		if (this.isDirectory || this.digestInstance == null || !exists())
			return Optional.empty();
		try {
			return Optional.of(StringUtil.toHexString(IOUtil.digest(this.path, this.digestInstance)));
		} catch (Exception e) {
			logger.warn("Can't get " + this.digestInstance + " digest of file: " + this.path, e);
			return Optional.empty();
		}
	}
	
	public boolean shouldSync() {
		return !this.isDirectory && (this.expectedSize == -1 || this.expectedDigest == null || this.digestInstance == null);
	}
	
	public boolean exists() {
		return Files.exists(this.path);
	}
	
	public boolean shouldUpdate(boolean unknown) {
		if (!exists())
			return true;
		
		if (this.isDirectory)
			return !Files.isDirectory(this.path);
		
		if (this.expectedSize == -1)
			return unknown;

		try {
			if (this.expectedSize != Files.size(this.path))
				return true;
		} catch (IOException e) {
			logger.warn("Can't get size of file: " + this.path, e);
			return unknown;
		}
		
		if (this.expectedDigest == null)
			return unknown;

		String localDigest = localDigest().orElse(null);
		if (localDigest == null)
			return unknown;

		return !this.expectedDigest.equals(localDigest);
	}

	public void syncExpectedInfo() {
		if (this.isDirectory)
			return;
		URL url = null;
		try {
			url = IOUtil.editSuffix(this.url, (s) -> {
				int i = s.lastIndexOf('/') +1;
				return s.substring(0, i) + "?info=" + s.substring(i);
			});
			
			try (BufferedReader in = this.config.openBufferedReader(url)) {
				this.expectedSize = Long.parseLong(in.readLine());
				this.expectedDigest = in.readLine();
				this.digestInstance = in.readLine();
			}
		} catch (Exception e) {
			logger.warn("Can't sync with url '" + url + "'", e);
		}
	}

	public boolean update(IncrementalListenerProvider p) {
		if (this.isDirectory) {
			try {
				Files.deleteIfExists(this.path);
				Files.createDirectory(this.path);
				return true;
			} catch (IOException e) {
				logger.warn("Can't create directory: " + this.path, e);
				return false;
			}
		}
		return IOUtil.download(this.url, this.path, this.config, p.expect(this.expectedSize));
	}
}
