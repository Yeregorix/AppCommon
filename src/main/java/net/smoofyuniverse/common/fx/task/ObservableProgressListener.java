/*
 * Copyright (c) 2017-2019 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.common.fx.task;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import net.smoofyuniverse.common.task.ProgressListener;

import java.util.concurrent.atomic.AtomicReference;

public class ObservableProgressListener extends ObservableBaseListener implements ProgressListener {
	private final AtomicReference<Double> progressUpdate = new AtomicReference<>();
	private final DoubleProperty progress = new SimpleDoubleProperty(-1);

	public DoubleProperty progressProperty() {
		return this.progress;
	}

	@Override
	public double getProgress() {
		return this.progress.get();
	}

	@Override
	public void setProgress(double value) {
		if (Platform.isFxApplicationThread())
			this.progress.set(value);
		else if (this.progressUpdate.getAndSet(value) == null)
			Platform.runLater(() -> this.progress.set(this.progressUpdate.getAndSet(null)));
	}
}
