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
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.app.OperatingSystem;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;

public class ResourceUtil {
	public static final String USER_HOME = Paths.get(OperatingSystem.USER_HOME).toAbsolutePath().toString();
	private static final Map<String, FileSystem> fileSystems = new HashMap<>();
	
	public static Stream<String> lines(InputStream in) {
		return lines(new BufferedReader(new InputStreamReader(in)));
	}
	
	public static Stream<String> lines(BufferedReader in) {
		return in.lines().onClose(() -> close(in));
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
		//noinspection ConstantConditions
		return new Image(Application.class.getClassLoader().getResource(localPath).toString());
	}
	
	public static Set<Class<?>> getClasses(String packageName, boolean recursive) throws IOException {
		return getClasses(Thread.currentThread().getContextClassLoader(), packageName, recursive);
	}

	public static Set<Class<?>> getClasses(ClassLoader cl, String packageName, boolean recursive) throws IOException {
		Enumeration<URL> en = cl.getResources(packageName.replace('.', '/'));
		Set<Class<?>> classes = new HashSet<>();
		while (en.hasMoreElements()) {
			try {
				findClasses(getResource(en.nextElement()), packageName, recursive, classes);
			} catch (URISyntaxException e) {
				throw new IOException("Can't get path of package " + packageName, e);
			}
		}
		return classes;
	}

	private static void findClasses(Path dir, String packageName, boolean recursive, Set<Class<?>> classes) throws IOException {
		try (Stream<Path> st = Files.list(dir)) {
			Iterator<Path> it = st.iterator();
			while (it.hasNext()) {
				Path p = it.next();
				String fn = p.getFileName().toString();
				if (Files.isDirectory(p)) {
					if (recursive)
						findClasses(p, packageName + "." + fn, true, classes);
				} else if (fn.endsWith(".class")) {
					String className = packageName + "." + fn.substring(0, fn.length() - 6);
					try {
						classes.add(Class.forName(className));
					} catch (Exception e) {
						throw new IOException("Can't get class for name " + className);
					}
				}
			}
		} catch (Exception e) {
			throw new IOException("Can't list classes in package " + packageName);
		}
	}
	
	public static Path getResource(String localPath) throws IOException, URISyntaxException {
		return getResource(Application.class.getClassLoader().getResource(localPath));
	}
	
	public static Path getResource(URL url) throws IOException, URISyntaxException {
		return getResource(url.toURI());
	}
	
	public static Path getResource(URI uri) throws IOException, URISyntaxException {
		try {
			return Paths.get(uri);
		} catch (Throwable t) {
			String[] a = uri.toString().split("!");
			FileSystem fs = fileSystems.get(a[0]);
			if (fs == null) {
				fs = FileSystems.newFileSystem(new URI(a[0]), new HashMap<>());
				fileSystems.put(a[0], fs);
			}
			return fs.getPath(a[1]);
		}
	}
	
	public static void closeResources() {
		for (FileSystem fs : fileSystems.values())
			close(fs);
		fileSystems.clear();
	}
	
	public static void close(Closeable c) {
		try {
			c.close();
		} catch (Exception ignored) {
		}
	}

	public static void close(Stream<?> st) {
		try {
			st.close();
		} catch (Exception ignored) {
		}
	}
}
