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

package net.smoofyuniverse.common.listener;

import net.smoofyuniverse.common.fxui.task.ObservableTask;

public class ObservableListener implements BasicListener {
	public final ObservableTask task;
	public final long expectedTotal;
	private long total = 0;
	
	public ObservableListener(ObservableTask task, long expectedTotal) {
		this.task = task;
		this.expectedTotal = expectedTotal;
		this.task.setProgress(expectedTotal == -1 ? -1 : 0);
	}
	
	public long getTotal() {
		return this.total;
	}
	
	@Override
	public boolean isCancelled() {
		return this.task.isCancelled();
	}

	@Override
	public void increment(long v) {
		this.total += v;
		if (this.expectedTotal != -1)
			this.task.setProgress(this.total / (double) this.expectedTotal);
	}
	
	@Override
	public void setMessage(String v) {
		this.task.setMessage(v);
	}
}
