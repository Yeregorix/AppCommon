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

import javafx.beans.value.ObservableValue;
import net.smoofyuniverse.common.task.IncrementalTask;
import net.smoofyuniverse.common.task.ProgressTask;

import java.util.Optional;

public class ProgressIncrementalTask extends ProgressIncrementalListener implements IncrementalTask {
	public final ProgressTask taskDelegate;

	public ProgressIncrementalTask(ProgressTask delegate, long maximum, boolean limit) {
		super(delegate, maximum, limit);
		this.taskDelegate = delegate;
	}

	@Override
	public Optional<String> getTitle() {
		return this.taskDelegate.getTitle();
	}

	@Override
	public void setTitle(String value) {
		this.taskDelegate.setTitle(value);
	}

	@Override
	public void setTitle(ObservableValue<String> value) {
		this.taskDelegate.setTitle(value);
	}

	@Override
	public Optional<String> getMessage() {
		return this.taskDelegate.getMessage();
	}

	@Override
	public void setMessage(String value) {
		this.taskDelegate.setMessage(value);
	}

	@Override
	public void setMessage(ObservableValue<String> value) {
		this.taskDelegate.setMessage(value);
	}
}
