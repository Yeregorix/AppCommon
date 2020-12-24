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

package net.smoofyuniverse.common.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * An helper to load resources as {@link Path}s.
 */
public class ResourceLoader implements AutoCloseable {
	private final Map<String, FileSystem> fileSystems = new HashMap<>();

	/**
	 * Finds the resource with the given name in the class's loader.
	 * Converts it to a {@link Path}.
	 *
	 * @param cl   The class.
	 * @param name The resource name.
	 * @return The path.
	 * @throws IOException if an I/O exception occurs.
	 */
	public Path getResource(Class<?> cl, String name) throws IOException {
		return getResource(cl.getClassLoader(), name);
	}

	/**
	 * Finds the resource with the given name in the class loader.
	 * Converts it to a {@link Path}.
	 *
	 * @param cl   The class loader.
	 * @param name The resource name.
	 * @return The path.
	 * @throws IOException if an I/O exception occurs.
	 */
	public Path getResource(ClassLoader cl, String name) throws IOException {
		URL url = cl.getResource(name);
		if (url == null)
			throw new NoSuchElementException("Resource not found");
		return toPath(url);
	}

	/**
	 * Converts the {@link URL} to a {@link Path}.
	 *
	 * @param url The URL.
	 * @return The path.
	 * @throws IOException if an I/O exception occurs.
	 */
	public Path toPath(URL url) throws IOException {
		try {
			return toPath(url.toURI());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Converts the {@link URI} to a {@link Path}.
	 *
	 * @param uri The URI.
	 * @return The path.
	 * @throws IOException if an I/O exception occurs.
	 */
	public Path toPath(URI uri) throws IOException {
		try {
			return Paths.get(uri);
		} catch (FileSystemNotFoundException e) {
			String[] a = uri.toString().split("!");
			FileSystem fs = this.fileSystems.get(a[0]);
			if (fs == null) {
				try {
					fs = FileSystems.newFileSystem(new URI(a[0]), new HashMap<>());
				} catch (URISyntaxException e2) {
					throw new IllegalArgumentException(e2);
				}
				this.fileSystems.put(a[0], fs);
			}
			return fs.getPath(a[1]);
		}
	}

	/**
	 * Close all underlying file systems.
	 */
	@Override
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
