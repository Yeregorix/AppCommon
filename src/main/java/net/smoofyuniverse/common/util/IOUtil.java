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

package net.smoofyuniverse.common.util;

import net.smoofyuniverse.common.download.ConnectionConfig;
import net.smoofyuniverse.common.platform.OperatingSystem;
import net.smoofyuniverse.common.task.IncrementalListenerProvider;
import net.smoofyuniverse.common.task.io.ListenedInputStream;
import net.smoofyuniverse.logger.core.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class IOUtil {
	private static final Logger logger = Logger.get("IOUtil");

	public static final Pattern ILLEGAL_PATH = Pattern.compile("[:\\\\/*?|<>\"]+");
	public static final Pattern USER_HOME = Pattern.compile(Paths.get(OperatingSystem.USER_HOME).toAbsolutePath().toString(), Pattern.LITERAL | Pattern.CASE_INSENSITIVE);

	public static boolean contentEquals(InputStream in1, InputStream in2) throws IOException {
		if (in1 == in2)
			return true;

		if (!(in1 instanceof BufferedInputStream))
			in1 = new BufferedInputStream(in1);

		if (!(in2 instanceof BufferedInputStream))
			in2 = new BufferedInputStream(in2);

		int b1 = in1.read();
		while (b1 != -1) {
			int b2 = in2.read();
			if (b1 != b2)
				return false;
			b1 = in1.read();
		}

		return in2.read() == -1;
	}

	public static Path getMavenPath(Path dir, String fname, String suffix) {
		String[] a = fname.split(":");
		if (a.length < 3)
			throw new IllegalArgumentException("fname");

		if (a.length > 3) {
			StringBuilder b = new StringBuilder();
			for (int i = 3; i < a.length; i++)
				b.append('-').append(a[i]);
			b.append(suffix);
			suffix = b.toString();
		}

		return getMavenPath(dir, a[0], a[1], a[2], suffix);
	}

	public static Path getMavenPath(Path dir, String group, String name, String version, String suffix) {
		return dir.resolve(group.replace('.', '/')).resolve(name).resolve(version).resolve(name + "-" + version + suffix);
	}

	public static boolean download(URL url, Path file, ConnectionConfig config, IncrementalListenerProvider p) {
		HttpURLConnection co;
		try {
			co = config.openHttpConnection(url);
			co.setRequestProperty("Accept", "application/octet-stream");
		} catch (IOException e) {
			logger.warn("Failed to open connection to url " + url + ".", e);
			return false;
		}

		return download(co, file, config.bufferSize, p);
	}

	public static boolean download(HttpURLConnection co, Path file, int bufferSize, IncrementalListenerProvider p) {
		try {
			co.connect();
			if (co.getResponseCode() / 100 != 2) {
				logger.info("Server at url " + co.getURL() + " returned a bad response code: " + co.getResponseCode());
				return false;
			}

			logger.info("Downloading from url " + co.getURL() + " to file " + file + " ...");
			long time = System.currentTimeMillis();

			try (ListenedInputStream in = p.getInputStream(co);
				 OutputStream out = Files.newOutputStream(file)) {
				byte[] buffer = new byte[bufferSize];
				int length;
				while ((length = in.read(buffer)) != -1)
					out.write(buffer, 0, length);

				if (in.listener.isCancelled()) {
					logger.debug("Download cancelled (" + (System.currentTimeMillis() - time) / 1000F + "s).");
					return false;
				}
			}

			logger.debug("Download ended (" + (System.currentTimeMillis() - time) / 1000F + "s).");
			return true;
		} catch (IOException e) {
			logger.warn("Download from url " + co.getURL() + " failed.", e);
			return false;
		} finally {
			co.disconnect();
		}
	}

	public static byte[] digest(Path file, String algorithm) throws IOException, NoSuchAlgorithmException {
		return digest(file, algorithm, 4096);
	}

	public static byte[] digest(Path file, String algorithm, int bufferSize) throws IOException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(algorithm);
		try (InputStream in = Files.newInputStream(file)) {
			byte[] buffer = new byte[bufferSize];
			int len;
			while ((len = in.read(buffer)) != -1)
				md.update(buffer, 0, len);
			return md.digest();
		}
	}
}
