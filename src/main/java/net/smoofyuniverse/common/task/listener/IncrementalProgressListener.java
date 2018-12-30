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

package net.smoofyuniverse.common.task.listener;

public class IncrementalProgressListener implements IncrementalListener {
	public final ProgressListener delegate;
	public final long expectedTotal;
	private long total = 0;

	public IncrementalProgressListener(ProgressListener delegate, long expectedTotal) {
		this.delegate = delegate;
		this.expectedTotal = expectedTotal;

		this.delegate.setProgress(expectedTotal == -1 ? -1 : 0);
	}
	
	public long getTotal() {
		return this.total;
	}
	
	@Override
	public void increment(long value) {
		this.total += value;
		if (this.expectedTotal != -1)
			this.delegate.setProgress(this.total / (double) this.expectedTotal);
	}

	@Override
	public void setMessage(String value) {
		this.delegate.setMessage(value);
	}
	
	@Override
	public boolean isCancelled() {
		return this.delegate.isCancelled();
	}
}
