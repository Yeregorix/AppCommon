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

package net.smoofyuniverse.common.fx.dialog.builder;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import net.smoofyuniverse.common.app.App;
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.fx.task.ObservableProgressTask;
import net.smoofyuniverse.common.task.ProgressTask;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * An {@link Alert} builder.
 */
public class AlertBuilder extends DialogBuilder<ButtonType> {
	private Consumer<ProgressTask> consumer;
	private ExecutorService executor;
	private ObservableProgressTask task;
	private AlertType type;

	/**
	 * Sets the type.
	 *
	 * @param value The type.
	 * @return this.
	 */
	public AlertBuilder type(AlertType value) {
		this.type = value;
		return this;
	}

	/**
	 * Sets the task.
	 *
	 * @param value The task.
	 * @return this.
	 */
	public AlertBuilder task(ObservableProgressTask value) {
		this.task = value;
		return this;
	}

	/**
	 * Sets the task executor.
	 *
	 * @param value The task executor.
	 * @return this.
	 */
	public AlertBuilder executor(ExecutorService value) {
		this.executor = value;
		return this;
	}

	/**
	 * Sets the task consumer.
	 *
	 * @param value The task consumer.
	 * @return this.
	 */
	public AlertBuilder consumer(Consumer<ProgressTask> value) {
		this.consumer = value;
		return this;
	}

	@Override
	protected void validate() {
		if (this.consumer != null)
			prepareTask();

		super.validate();

		if (this.type == null)
			throw new IllegalArgumentException("type");
	}
	
	private void prepareTask() {
		if (this.task == null)
			this.task = new ObservableProgressTask();
		
		if (this.type == null)
			this.type = AlertType.INFORMATION;
		
		if (this.buttonTypes == null)
			this.buttonTypes = new ButtonType[] { ButtonType.CANCEL };
		
		if (this.message == null && this.messageP == null)
			this.messageP = this.task.titleProperty();
		
		if (this.expandable == null)
			this.expandable = createTaskContent();

		if (this.executor == null)
			this.executor = Application.get().getExecutor();
	}
	
	private Node createTaskContent() {
		Label msg = new Label();
		ProgressBar p = new ProgressBar();
		p.setMaxWidth(Integer.MAX_VALUE);

		msg.textProperty().bind(this.task.messageProperty());
		p.progressProperty().bind(this.task.progressProperty());

		return new VBox(5, msg, p);
	}

	@Override
	protected Dialog<ButtonType> provide() {
		return new Alert(this.type);
	}

	/**
	 * If the task is set, executes the task and returns whether the task hasn't been cancelled.
	 * <p>
	 * Otherwise, shows the dialog and returns whether the OK button has been selected.
	 * <p>
	 * In both case, blocks until the window is closed.
	 *
	 * @return The result.
	 */
	@Override
	public boolean submitAndWait() {
		validate();

		if (Platform.isFxApplicationThread()) {
			Dialog<ButtonType> d = build();

			if (this.task == null)
				return d.showAndWait().orElse(null) == ButtonType.OK;

			Node cancel = d.getDialogPane().lookupButton(ButtonType.CANCEL);
			if (cancel != null)
				cancel.disableProperty().bind(this.task.cancellableProperty().not());

			AtomicBoolean ended = new AtomicBoolean(false);
			Future<?> future = this.executor.submit(() -> {
				App.submit(this.consumer, this.task);
				ended.set(true);
				Platform.runLater(d::hide);
			});

			d.getDialogPane().getScene().getWindow().setOnCloseRequest(e -> {
				if (!ended.get() && !this.task.isCancellable())
					e.consume();
			});

			d.showAndWait();

			if (!ended.get()) {
				this.task.cancel();

				try {
					future.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return !this.task.isCancelled();
		} else {
			AtomicBoolean result = new AtomicBoolean();
			CountDownLatch lock = new CountDownLatch(1);
			Platform.runLater(() -> {
				result.set(submitAndWait());
				lock.countDown();
			});
			try {
				lock.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return result.get();
		}
	}
}
