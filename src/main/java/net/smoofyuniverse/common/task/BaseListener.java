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

package net.smoofyuniverse.common.task;

/**
 * Basically anything that might be cancellable.
 */
public interface BaseListener {

	/**
	 * Gets whether this listener is cancellable.
	 *
	 * @return Whether this listener is cancellable.
	 */
	boolean isCancellable();

	/**
	 * Sets whether this listener is cancellable.
	 *
	 * @param value Is cancellable.
	 */
	void setCancellable(boolean value);

	/**
	 * Gets whether this listener is cancelled.
	 *
	 * @return Whether this listener is cancelled.
	 */
	boolean isCancelled();

	/**
	 * Sets whether this listener is cancelled.
	 *
	 * @param value Is cancelled.
	 */
	void setCancelled(boolean value);

	/**
	 * Cancels this listener.
	 */
	default void cancel() {
		setCancelled(true);
	}
}
