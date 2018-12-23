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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class ResourceLoader {
	private final Map<String, FileSystem> fileSystems = new HashMap<>();

	public void getClasses(Class<?> cl, String packageName, boolean recursive, Set<Class<?>> classes) throws IOException {
		getClasses(cl.getClassLoader(), packageName, recursive, classes);
	}

	public void getClasses(ClassLoader cl, String packageName, boolean recursive, Set<Class<?>> classes) throws IOException {
		Enumeration<URL> en = cl.getResources(packageName.replace('.', '/'));
		while (en.hasMoreElements()) {
			try {
				findClasses(toPath(en.nextElement()), packageName, recursive, classes);
			} catch (URISyntaxException e) {
				throw new IOException("Can't get path of package " + packageName, e);
			}
		}
	}

	private void findClasses(Path dir, String packageName, boolean recursive, Set<Class<?>> classes) throws IOException {
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

	public Path toPath(Class<?> cl, String localResource) throws IOException, URISyntaxException {
		return toPath(cl.getClassLoader(), localResource);
	}

	public Path toPath(ClassLoader cl, String localResource) throws IOException, URISyntaxException {
		return toPath(cl.getResource(localResource));
	}

	public Path toPath(URL url) throws IOException, URISyntaxException {
		return toPath(url.toURI());
	}

	public Path toPath(URI uri) throws IOException, URISyntaxException {
		try {
			return Paths.get(uri);
		} catch (FileSystemNotFoundException e) {
			String[] a = uri.toString().split("!");
			FileSystem fs = this.fileSystems.get(a[0]);
			if (fs == null) {
				fs = FileSystems.newFileSystem(new URI(a[0]), new HashMap<>());
				this.fileSystems.put(a[0], fs);
			}
			return fs.getPath(a[1]);
		}
	}

	public void close() {
		for (FileSystem fs : this.fileSystems.values()) {
			try {
				fs.close();
			} catch (Exception ignored) {
			}
		}
		this.fileSystems.clear();
	}
}
