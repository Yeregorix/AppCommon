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

package net.smoofyuniverse.common.task.io;

import net.smoofyuniverse.common.task.IncrementalListener;

import java.io.IOException;
import java.io.InputStream;

/**
 * A wrapper for an {@link InputStream}.
 * Each byte read increments a {@link IncrementalListener}.
 */
public class ListenedInputStream extends InputStream {
	/**
	 * The wrapped input stream.
	 */
	public final InputStream delegate;

	/**
	 * The listener incremented when reading.
	 */
	public final IncrementalListener listener;

	/**
	 * Wraps the {@link InputStream}.
	 *
	 * @param delegate The wrapped input stream.
	 * @param listener The listener incremented when reading.
	 */
	public ListenedInputStream(InputStream delegate, IncrementalListener listener) {
		this.delegate = delegate;
		this.listener = listener;
	}

	@Override
	public int read() throws IOException {
		if (this.listener.isCancelled())
			return -1;

		int r = this.delegate.read();
		if (r != -1)
			this.listener.increment(1);
		return r;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (this.listener.isCancelled())
			return -1;

		int c = this.delegate.read(b, off, len);
		if (c != -1)
			this.listener.increment(c);
		return c;
	}

	@Override
	public long skip(long n) throws IOException {
		if (this.listener.isCancelled())
			return 0;

		long c = this.delegate.skip(n);
		this.listener.increment(c);
		return c;
	}

	@Override
	public int available() throws IOException {
		return this.delegate.available();
	}

	@Override
	public void close() throws IOException {
		this.delegate.close();
	}
}
