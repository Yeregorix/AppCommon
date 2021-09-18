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

package net.smoofyuniverse.common.download;

import net.smoofyuniverse.common.task.IncrementalListenerProvider;
import net.smoofyuniverse.common.util.IOUtil;
import net.smoofyuniverse.common.util.StringUtil;
import net.smoofyuniverse.logger.core.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Information about a remote file.
 * Optionally holds a path to a local file.
 */
public class FileInfo {
	private static final Logger logger = Logger.get("FileInfo");

	/**
	 * The URL of the remote file.
	 */
	public final URL url;

	/**
	 * The size of the remote file.
	 * -1 means unknown.
	 */
	public final long size;

	/**
	 * The hexadecimal representation of the remote file digest.
	 */
	public final String digest;

	/**
	 * The algorithm used to compute the remote file digest.
	 */
	public final String digestAlgorithm;

	/**
	 * The local file.
	 */
	public Path file;

	/**
	 * Creates remote file information.
	 *
	 * @param url             The URL.
	 * @param size            The size.
	 * @param digest          The hexadecimal representation of the digest.
	 * @param digestAlgorithm The algorithm used to compute the digest.
	 */
	public FileInfo(URL url, long size, String digest, String digestAlgorithm) {
		if (url == null)
			throw new IllegalArgumentException("url");
		if (size < 0 && size != -1)
			throw new IllegalArgumentException("Size must be positive or unknown");

		this.url = url;
		this.size = size;
		this.digest = digest;
		this.digestAlgorithm = digestAlgorithm;
	}

	/**
	 * Creates parent directory.
	 *
	 * @return Whether the directory was created without error.
	 */
	public boolean createParent() {
		Path dir = this.file.getParent();
		if (!Files.isDirectory(dir)) {
			try {
				Files.createDirectories(dir);
			} catch (IOException e) {
				logger.warn("Failed to create directory " + dir, e);
				return false;
			}
		}
		return true;
	}

	/**
	 * Downloads the remote file to the local file.
	 *
	 * @param config The connection configuration.
	 * @param p      A listener provider.
	 * @return Whether the download has succeeded.
	 */
	public boolean download(ConnectionConfig config, IncrementalListenerProvider p) {
		return download(this.file, config, p);
	}

	/**
	 * Downloads the remote file to the target file.
	 *
	 * @param file   The target file.
	 * @param config The connection configuration.
	 * @param p      A listener provider.
	 * @return Whether the download has succeeded.
	 */
	public boolean download(Path file, ConnectionConfig config, IncrementalListenerProvider p) {
		HttpURLConnection co;
		try {
			co = openDownloadConnection(config);
		} catch (IOException e) {
			logger.warn("Failed to open connection to url " + url + ".", e);
			return false;
		}

		return IOUtil.download(co, file, config.bufferSize, p);
	}

	/**
	 * Opens and configures the download connection.
	 *
	 * @param config The connection configuration.
	 * @return The connection.
	 * @throws IOException if an I/O exception occurs.
	 */
	public HttpURLConnection openDownloadConnection(ConnectionConfig config) throws IOException {
		HttpURLConnection co = config.openHttpConnection(this.url);
		co.setRequestProperty("Accept", "application/octet-stream");
		return co;
	}

	/**
	 * Checks whether the local file matches the remote file.
	 *
	 * @return Whether the local file matches the remote file.
	 */
	public boolean matches() {
		return matches(this.file);
	}

	/**
	 * Checks whether the local file matches the remote file.
	 *
	 * @param file The local file.
	 * @return Whether the local file matches the remote file.
	 */
	public boolean matches(Path file) {
		try {
			if (!Files.isRegularFile(file))
				return false;

			if (this.size != -1 && this.size != Files.size(file))
				return false;

			if (this.digest != null && this.digestAlgorithm != null && !this.digest.equals(StringUtil.toHexString(IOUtil.digest(file, this.digestAlgorithm))))
				return false;
		} catch (Exception e) {
			logger.warn("Failed to check file " + file, e);
			return false;
		}
		return true;
	}
}
