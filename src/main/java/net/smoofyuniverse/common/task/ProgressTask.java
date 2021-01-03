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

import net.smoofyuniverse.common.task.impl.ProgressIncrementalTask;
import net.smoofyuniverse.logger.core.Logger;

import java.util.function.Consumer;

/**
 * A {@link ProgressListener} with a title and a message;
 */
public interface ProgressTask extends BaseTask, ProgressListener, IncrementalTaskProvider {

	@Override
	default IncrementalTask limit(long total) {
		return new ProgressIncrementalTask(this, total, true);
	}

	@Override
	default IncrementalTask expect(long total) {
		return new ProgressIncrementalTask(this, total, false);
	}

	/**
	 * Passes this task to the consumer.
	 * Handles any exceptions the may occur during execution.
	 *
	 * @param consumer The consumer.
	 * @return Whether the task hasn't been cancelled.
	 */
	default boolean submit(Consumer<ProgressTask> consumer) {
		setCancelled(false);
		try {
			consumer.accept(this);
		} catch (Exception e) {
			cancel();
			Logger.get("ProgressTask").error("Failed to execute: " + this, e);
		}
		return !isCancelled();
	}
}
