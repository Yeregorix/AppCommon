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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A static helper for URLs.
 */
public class URLUtil {

	/**
	 * Creates an URL from the string representation.
	 *
	 * @param url The string representation.
	 * @return The new URL.
	 */
	public static URL newURL(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("url", e);
		}
	}

	/**
	 * Creates a copy of the given URL and appends the suffix to the file.
	 *
	 * @param url    The URL to copy.
	 * @param suffix The file suffix.
	 * @return The new URL.
	 * @throws MalformedURLException if the new URL is invalid.
	 */
	public static URL appendSuffix(URL url, String suffix) throws MalformedURLException {
		return setFile(url, url.getFile() + suffix);
	}

	/**
	 * Creates a copy of the given URL but with the file set to a new value.
	 *
	 * @param url  The URL to copy.
	 * @param file The new file.
	 * @return The new URL.
	 * @throws MalformedURLException if the new URL is invalid.
	 */
	public static URL setFile(URL url, String file) throws MalformedURLException {
		return new URL(url.getProtocol(), url.getHost(), url.getPort(), file);
	}
}
