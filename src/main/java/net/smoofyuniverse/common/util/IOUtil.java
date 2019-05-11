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

package net.smoofyuniverse.common.util;

import javafx.scene.image.Image;
import net.smoofyuniverse.common.app.App;
import net.smoofyuniverse.common.app.OperatingSystem;
import net.smoofyuniverse.common.download.ConnectionConfig;
import net.smoofyuniverse.common.task.IncrementalListener;
import net.smoofyuniverse.common.task.IncrementalListenerProvider;
import net.smoofyuniverse.logger.core.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class IOUtil {
	private static final Logger logger = App.getLogger("IOUtil");

	public static final Pattern ILLEGAL_PATH = Pattern.compile("[:\\\\/*?|<>\"]+");
	public static final String USER_HOME = Paths.get(OperatingSystem.USER_HOME).toAbsolutePath().toString();

	public static final FileVisitor<Path> DIRECTORY_REMOVER = new FileVisitor<Path>() {
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Files.delete(file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
			throw e;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
			Files.delete(dir);
			return FileVisitResult.CONTINUE;
		}
	};

	private static Method addURL;

	public static URL newURL(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void addToClasspath(Path file) {
		try {
			addToClasspath(file.toUri().toURL());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void addToClasspath(URL url) {
		addToClasspath((URLClassLoader) ClassLoader.getSystemClassLoader(), url);
	}

	public static void addToClasspath(URLClassLoader classLoader, URL url) {
		try {
			addURL.invoke(classLoader, url);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static void deleteRecursively(Path dir) throws IOException {
		Files.walkFileTree(dir, DIRECTORY_REMOVER);
	}

	public static void copyRecursively(Path from, Path to, CopyOption... options) throws IOException {
		Path source = from.toAbsolutePath(), target = to.toAbsolutePath();
		int i = source.toString().length();

		Files.walkFileTree(source, new FileVisitor<Path>() {

			private Path getDestination(Path sourcePath) {
				return target.resolve(sourcePath.toString().substring(i));
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Files.createDirectory(getDestination(dir));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, getDestination(file), options);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
				throw e;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException e) {
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static URL appendSuffix(URL url, String suffix) throws MalformedURLException {
		return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile() + suffix);
	}

	public static URL editSuffix(URL url, UnaryOperator<String> edit) throws MalformedURLException {
		return new URL(url.getProtocol(), url.getHost(), url.getPort(), edit.apply(url.getFile()));
	}

	public static boolean download(URL url, Path file, IncrementalListenerProvider p) {
		return download(url, file, App.get().getConnectionConfig(), p);
	}

	public static boolean download(URL url, Path file, ConnectionConfig config, IncrementalListenerProvider p) {
		HttpURLConnection co;
		try {
			co = config.openHttpConnection(url);
			co.setRequestProperty("Accept", "application/octet-stream");
		} catch (IOException e) {
			logger.warn("Download from url '" + url + "' failed.", e);
			return false;
		}

		return download(file, co, config.bufferSize, p);
	}

	public static boolean download(Path file, HttpURLConnection co, int bufferSize, IncrementalListenerProvider p) {
		try {
			co.connect();
			if (co.getResponseCode() / 100 != 2) {
				logger.info("Server at url '" + co.getURL() + "' returned a bad response code: " + co.getResponseCode());
				return false;
			}

			IncrementalListener l;
			try {
				l = p.expect(Long.parseLong(co.getHeaderField("Content-Length")));
			} catch (NumberFormatException e) {
				l = p.expect(-1);
			}

			logger.info("Downloading from url '" + co.getURL() + "' to file: " + file + " ..");
			long time = System.currentTimeMillis();

			try (InputStream in = co.getInputStream();
				 OutputStream out = Files.newOutputStream(file)) {
				byte[] buffer = new byte[bufferSize];
				int length;
				while ((length = in.read(buffer)) > 0) {
					if (l.isCancelled()) {
						logger.debug("Download cancelled (" + (System.currentTimeMillis() - time) / 1000F + "s).");
						return false;
					}
					out.write(buffer, 0, length);
					l.increment(length);
				}
			}
			logger.debug("Download ended (" + (System.currentTimeMillis() - time) / 1000F + "s).");
			return true;
		} catch (IOException e) {
			logger.warn("Download from url '" + co.getURL() + "' failed.", e);
			return false;
		} finally {
			co.disconnect();
		}
	}
	
	public static byte[] digest(Path file, String instance) throws IOException, NoSuchAlgorithmException {
		return digest(file, instance, 1024);
	}
	
	public static byte[] digest(Path file, String instance, int bufferSize) throws IOException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(instance);
		try (InputStream in = Files.newInputStream(file)) {
			 byte[] bytes = new byte[bufferSize];
			 int len;
			 while ((len = in.read(bytes)) != -1)
				 md.update(bytes, 0, len);
			 bytes = md.digest();
			 return bytes;
		}
	}
	
	public static Image loadImage(String localPath) {
		URL url = App.class.getClassLoader().getResource(localPath);
		if (url == null)
			throw new IllegalArgumentException("localPath");
		return new Image(url.toString());
	}

	static {
		try {
			addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			addURL.setAccessible(true);
		} catch (Exception e) {
			logger.warn("Failed to access method addURL", e);
			addURL = null;
		}
	}
}