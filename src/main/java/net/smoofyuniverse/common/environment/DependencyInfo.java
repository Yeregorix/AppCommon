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

package net.smoofyuniverse.common.environment;

import net.smoofyuniverse.common.download.FileInfo;
import net.smoofyuniverse.common.util.URLUtil;

import java.net.URL;

/**
 * Information about a dependency.
 */
public class DependencyInfo extends FileInfo {
	/**
	 * The name of this dependency.
	 */
	public final String name;

	/**
	 * Creates a new dependency.
	 *
	 * @param name            The name.
	 * @param url             The URL.
	 * @param size            The size.
	 * @param digest          The hexadecimal representation of the digest.
	 * @param digestAlgorithm The algorithm used to compute the digest.
	 */
	public DependencyInfo(String name, String url, long size, String digest, String digestAlgorithm) {
		this(name, URLUtil.newURL(url), size, digest, digestAlgorithm);
	}

	/**
	 * Creates a new dependency.
	 *
	 * @param name            The name.
	 * @param url             The URL.
	 * @param size            The size.
	 * @param digest          The hexadecimal representation of the digest.
	 * @param digestAlgorithm The algorithm used to compute the digest.
	 */
	public DependencyInfo(String name, URL url, long size, String digest, String digestAlgorithm) {
		super(url, size, digest, digestAlgorithm);
		if (name == null || name.isEmpty())
			throw new IllegalArgumentException("name");
		this.name = name;
	}
}
