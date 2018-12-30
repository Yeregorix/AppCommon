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

package net.smoofyuniverse.common.fxui.task;

import javafx.application.Platform;
import javafx.beans.property.*;
import net.smoofyuniverse.common.app.App;
import net.smoofyuniverse.common.app.State;
import net.smoofyuniverse.common.event.Order;
import net.smoofyuniverse.common.task.Task;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

public final class ObservableTask implements Task {
	private static final Set<ObservableTask> tasks = Collections.newSetFromMap(new WeakHashMap<>());

	@Override
	public Optional<String> getTitle() {
		return Optional.ofNullable(this.title.get());
	}

	private AtomicReference<String> titleUpdate = new AtomicReference<>();
	private AtomicReference<String> messageUpdate = new AtomicReference<>();
	private AtomicReference<Double> progressUpdate = new AtomicReference<>();
	private AtomicReference<Boolean> cancellableUpdate = new AtomicReference<>();
	private AtomicReference<Boolean> cancelledUpdate = new AtomicReference<>();
	private StringProperty title = new SimpleStringProperty();
	private StringProperty message = new SimpleStringProperty();
	private DoubleProperty progress = new SimpleDoubleProperty(-1);
	private BooleanProperty cancellable = new SimpleBooleanProperty(true);
	private BooleanProperty cancelled = new SimpleBooleanProperty(false);

	public ObservableTask() {
		tasks.add(this);
	}

	public StringProperty titleProperty() {
		return this.title;
	}

	public StringProperty messageProperty() {
		return this.message;
	}

	public DoubleProperty progressProperty() {
		return this.progress;
	}
	
	public BooleanProperty cancellableProperty() {
		return this.cancellable;
	}

	public BooleanProperty cancelledProperty() {
		return this.cancelled;
	}

	@Override
	public void setTitle(String value) {
		if (Platform.isFxApplicationThread())
			this.title.set(value);
		else if (this.titleUpdate.getAndSet(value) == null)
			Platform.runLater(() -> this.title.set(this.titleUpdate.getAndSet(null)));
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
	public boolean isCancelled() {
		return this.cancelled.get();
	}

	@Override
	public void setCancelled(boolean value) {
		if (Platform.isFxApplicationThread())
			this.cancelled.set(value);
		else if (this.cancelledUpdate.getAndSet(value) == null)
			Platform.runLater(() -> this.cancelled.set(this.cancelledUpdate.getAndSet(null)));
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

	@Override
	public boolean isCancellable() {
		return this.cancellable.get();
	}

	@Override
	public void setCancellable(boolean value) {
		if (Platform.isFxApplicationThread())
			this.cancellable.set(value);
		else if (this.cancellableUpdate.getAndSet(value) == null)
			Platform.runLater(() -> this.cancellable.set(this.cancellableUpdate.getAndSet(null)));
	}

	static {
		App.registerListener(State.SHUTDOWN.newListener(e -> tasks.forEach(Task::cancel), Order.EARLY));
	}
}
