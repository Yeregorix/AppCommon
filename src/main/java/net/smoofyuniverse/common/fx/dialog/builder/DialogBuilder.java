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

package net.smoofyuniverse.common.fx.dialog.builder;

import javafx.application.Platform;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.logger.ApplicationLogger;
import net.smoofyuniverse.common.util.StringUtil;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link Dialog} builder.
 *
 * @param <T> The dialog type.
 */
public abstract class DialogBuilder<T> {
	private static final Logger logger = ApplicationLogger.get(DialogBuilder.class);

	protected ObservableValue<String> titleP, headerP, messageP;
	protected String title, header, message;
	protected ButtonType[] buttons;
	protected Map<ButtonType, ObservableBooleanValue> disable;
	protected Node content, expandable;
	protected Stage owner;

	/**
	 * Sets the owner.
	 *
	 * @param value The owner.
	 * @return this.
	 */
	public DialogBuilder<T> owner(Stage value) {
		this.owner = value;
		return this;
	}

	/**
	 * Sets the title.
	 *
	 * @param value The title.
	 * @return this.
	 */
	public DialogBuilder<T> title(String value) {
		this.titleP = null;
		this.title = value;
		return this;
	}

	/**
	 * Sets the title.
	 * Binds the value.
	 *
	 * @param value The title.
	 * @return this.
	 */
	public DialogBuilder<T> title(ObservableValue<String> value) {
		this.titleP = value;
		this.title = null;
		return this;
	}

	/**
	 * Sets the header.
	 *
	 * @param value The header.
	 * @return this.
	 */
	public DialogBuilder<T> header(String value) {
		this.headerP = null;
		this.header = value;
		return this;
	}

	/**
	 * Sets the header.
	 * Binds the value.
	 *
	 * @param value The header.
	 * @return this.
	 */
	public DialogBuilder<T> header(ObservableValue<String> value) {
		this.headerP = value;
		this.header = null;
		return this;
	}

	/**
	 * Sets the message.
	 * See {@link StringUtil#simpleFormat(Throwable)}.
	 *
	 * @param value The message.
	 * @return this.
	 */
	public DialogBuilder<T> message(Throwable value) {
		return message(StringUtil.simpleFormat(value));
	}

	/**
	 * Sets the message.
	 *
	 * @param value The message.
	 * @return this.
	 */
	public DialogBuilder<T> message(String value) {
		this.messageP = null;
		this.message = value;
		return this;
	}

	/**
	 * Sets the message.
	 * Binds the value.
	 *
	 * @param value The message.
	 * @return this.
	 */
	public DialogBuilder<T> message(ObservableValue<String> value) {
		this.messageP = value;
		this.message = null;
		return this;
	}

	/**
	 * Sets the content node.
	 *
	 * @param value The content node.
	 * @return this.
	 */
	public DialogBuilder<T> content(Node value) {
		this.content = value;
		return this;
	}

	/**
	 * Sets the expandable node.
	 *
	 * @param value The expandable node.
	 * @return this.
	 */
	public DialogBuilder<T> expandable(Node value) {
		this.expandable = value;
		return this;
	}

	/**
	 * Sets the button types.
	 *
	 * @param values The button types.
	 * @return this.
	 */
	public DialogBuilder<T> buttons(ButtonType... values) {
		this.buttons = values;
		return this;
	}

	/**
	 * Sets whether the button is disabled.
	 *
	 * @param buttonType The button type.
	 * @param value      The observable value.
	 * @return this.
	 */
	public DialogBuilder<T> disable(ButtonType buttonType, ObservableBooleanValue value) {
		if (buttonType == null)
			throw new IllegalArgumentException("buttonType");

		if (this.disable == null)
			this.disable = new HashMap<>();

		this.disable.put(buttonType, value);
		return this;
	}

	/**
	 * Builds a new dialog from this builder.
	 *
	 * @return The new dialog.
	 */
	public final Dialog<T> build() {
		prepare();
		return buildDialog();
	}

	/**
	 * Validates options and sets default ones when possible.
	 */
	protected void prepare() {
		if (this.title == null && this.titleP == null)
			throw new IllegalArgumentException("title");
	}

	protected Dialog<T> buildDialog() {
		Dialog<T> d = provide();

		d.initOwner(this.owner == null ? Application.get().getStage() : this.owner);

		if (this.titleP != null)
			d.titleProperty().bind(this.titleP);
		else
			d.setTitle(this.title);

		DialogPane p = d.getDialogPane();

		if (this.headerP != null)
			p.headerTextProperty().bind(this.headerP);
		else
			p.setHeaderText(this.header);

		if (this.messageP != null)
			p.contentTextProperty().bind(this.messageP);
		else
			p.setContentText(this.message);

		if (this.content != null)
			p.setContent(this.content);

		if (this.expandable != null)
			p.setExpandableContent(this.expandable);

		if (this.buttons != null)
			p.getButtonTypes().setAll(this.buttons);

		if (this.disable != null) {
			for (Entry<ButtonType, ObservableBooleanValue> e : this.disable.entrySet()) {
				if (e.getValue() == null)
					continue;

				Node button = p.lookupButton(e.getKey());
				if (button != null)
					button.disableProperty().bind(e.getValue());
			}
		}

		return d;
	}

	protected abstract Dialog<T> provide();

	/**
	 * Shows the dialog.
	 */
	public void show() {
		prepare();

		if (Platform.isFxApplicationThread())
			buildDialog().show();
		else
			Platform.runLater(this::show);
	}

	/**
	 * Shows the dialog and blocks until the window is closed.
	 *
	 * @return The result. See {@link Dialog#resultProperty()}.
	 */
	public Optional<T> showAndWait() {
		prepare();

		if (Platform.isFxApplicationThread()) {
			return buildDialog().showAndWait();
		} else {
			AtomicReference<Optional<T>> result = new AtomicReference<>();
			CountDownLatch lock = new CountDownLatch(1);

			Platform.runLater(() -> {
				result.set(showAndWait());
				lock.countDown();
			});

			try {
				lock.await();
			} catch (InterruptedException e) {
				logger.error("Interruption", e);
			}
			return result.get();
		}
	}

	/**
	 * Submits the dialog.
	 * The behavior depends on the implementation.
	 * See {@link AlertBuilder#submitAndWait()}.
	 *
	 * @return The result.
	 */
	public boolean submitAndWait() {
		throw new UnsupportedOperationException();
	}
}
