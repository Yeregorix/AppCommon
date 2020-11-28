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

package net.smoofyuniverse.common.task.impl;

import net.smoofyuniverse.common.task.IncrementalListener;

/**
 * The default implementation an {@link IncrementalListener}.
 */
public class SimpleIncrementalListener extends SimpleBaseListener implements IncrementalListener {
	/**
	 * If not zero, this listener will be cancelled when the total reaches this value.
	 */
	public final long maximum;
	private long total;

	/**
	 * Creates an incremental listener.
	 *
	 * @param maximum If strictly positive, this listener will be cancelled when the total reaches this value.
	 */
	public SimpleIncrementalListener(long maximum) {
		this.maximum = maximum <= 0 ? 0 : maximum;
	}

	@Override
	public long getTotal() {
		return this.total;
	}

	@Override
	public void increment(long value) {
		if (value < 0)
			throw new IllegalArgumentException("negative value");

		this.total += value;
		if (this.maximum != 0 && this.total >= this.maximum)
			cancel();
	}
}
