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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.task.BaseListener;

import java.util.concurrent.atomic.AtomicReference;

/**
 * An observable implementation of {@link BaseListener}.
 */
public class ObservableBaseListener implements BaseListener {
	private final AtomicReference<Boolean> cancellableUpdate = new AtomicReference<>();
	private final AtomicReference<Boolean> cancelledUpdate = new AtomicReference<>();
	private final BooleanProperty cancellable = new SimpleBooleanProperty(true);
	private final BooleanProperty cancelled = new SimpleBooleanProperty(false);

	/**
	 * Creates a listener and registers it (weak reference).
	 * See {@link Application#registerListener(BaseListener)}.
	 */
	public ObservableBaseListener() {
		Application.get().registerListener(this);
	}

	/**
	 * Gets the cancellable property.
	 *
	 * @return The cancellable property.
	 */
	public BooleanProperty cancellableProperty() {
		return this.cancellable;
	}

	/**
	 * Gets the cancelled property.
	 *
	 * @return The cancelled property.
	 */
	public BooleanProperty cancelledProperty() {
		return this.cancelled;
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
}
