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

package net.smoofyuniverse.common.task;

import net.smoofyuniverse.common.task.io.ListenedInputStream;

import java.io.IOException;
import java.net.URLConnection;

/**
 * An object that can provide an {@link IncrementalListener}.
 */
public interface IncrementalListenerProvider {

	/**
	 * Provides an {@link IncrementalListener}.
	 * If the total is strictly positive, expects the counter the reach the total.
	 * Cancels the listener when the counter reaches the total.
	 *
	 * @param total The total to reach.
	 * @return An {@link IncrementalListener}.
	 */
	IncrementalListener limit(long total);

	/**
	 * Creates a {@link ListenedInputStream} wrapping connection's input stream.
	 * The associated {@link IncrementalListener} is expecting to reach connection's header value: Content-Length.
	 *
	 * @param co The URL connection.
	 * @return A {@link ListenedInputStream}.
	 * @throws IOException See {@link URLConnection#getInputStream()}.
	 */
	default ListenedInputStream getInputStream(URLConnection co) throws IOException {
		IncrementalListener l;
		try {
			l = expect(Long.parseLong(co.getHeaderField("Content-Length")));
		} catch (NumberFormatException e) {
			l = expect(-1);
		}
		return l.wrap(co.getInputStream());
	}

	/**
	 * Provides an {@link IncrementalListener}.
	 * If the total is strictly positive, expects the counter the reach the total.
	 * The counter may exceed the total.
	 *
	 * @param total The total to reach.
	 * @return An {@link IncrementalListener}.
	 */
	IncrementalListener expect(long total);
}
