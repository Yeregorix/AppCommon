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

package net.smoofyuniverse.common.fx.task;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import net.smoofyuniverse.common.task.ProgressTask;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An observable implementation of {@link ProgressTask}.
 */
public class ObservableProgressTask extends ObservableProgressListener implements ProgressTask {
	private final AtomicReference<String> titleUpdate = new AtomicReference<>();
	private final AtomicReference<String> messageUpdate = new AtomicReference<>();
	private final StringProperty title = new SimpleStringProperty();
	private final StringProperty message = new SimpleStringProperty();

	/**
	 * Gets the title property.
	 *
	 * @return The title property.
	 */
	public StringProperty titleProperty() {
		return this.title;
	}

	/**
	 * Gets the message property.
	 *
	 * @return The message property.
	 */
	public StringProperty messageProperty() {
		return this.message;
	}

	@Override
	public Optional<String> getTitle() {
		return Optional.ofNullable(this.title.get());
	}

	@Override
	public void setTitle(String value) {
		if (Platform.isFxApplicationThread())
			this.title.set(value);
		else if (this.titleUpdate.getAndSet(value) == null)
			Platform.runLater(() -> this.title.set(this.titleUpdate.getAndSet(null)));
	}

	@Override
	public void setTitle(ObservableValue<String> value) {
		if (Platform.isFxApplicationThread())
			this.title.bind(value);
		else {
			this.titleUpdate.set(null);
			Platform.runLater(() -> this.title.bind(value));
		}
	}

	@Override
	public Optional<String> getMessage() {
		return Optional.ofNullable(this.message.get());
	}

	@Override
	public void setMessage(String value) {
		if (Platform.isFxApplicationThread())
			this.message.set(value);
		else if (this.messageUpdate.getAndSet(value) == null)
			Platform.runLater(() -> this.message.set(this.messageUpdate.getAndSet(null)));
	}

	@Override
	public void setMessage(ObservableValue<String> value) {
		if (Platform.isFxApplicationThread())
			this.message.bind(value);
		else {
			this.messageUpdate.set(null);
			Platform.runLater(() -> this.message.bind(value));
		}
	}
}
