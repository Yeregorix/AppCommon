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

package net.smoofyuniverse.common.environment;

import com.grack.nanojson.JsonObject;
import net.smoofyuniverse.common.download.FileInfo;

import java.net.URL;
import java.time.Instant;

/**
 * Information about a release.
 */
public final class ReleaseInfo extends FileInfo {

	/**
	 * The version.
	 */
	public final String version;

	/**
	 * The publication date.
	 */
	public final Instant date;

	/**
	 * Extra data.
	 */
	public final JsonObject extraData;

	/**
	 * Creates a new dependency.
	 *
	 * @param version         The version.
	 * @param date            The publication date.
	 * @param extraData       Extra data.
	 * @param url             The URL.
	 * @param size            The size.
	 * @param digest          The hexadecimal representation of the digest.
	 * @param digestAlgorithm The algorithm used to compute the digest.
	 */
	public ReleaseInfo(String version, Instant date, JsonObject extraData, URL url, long size, String digest, String digestAlgorithm) {
		super(url, size, digest, digestAlgorithm);
		if (version == null || version.isEmpty())
			throw new IllegalArgumentException("version");
		if (date == null)
			throw new IllegalArgumentException("date");

		this.version = version;
		this.date = date;
		this.extraData = extraData;
	}
}
