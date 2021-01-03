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
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.util.StringUtil;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link Dialog} builder.
 *
 * @param <T> The dialog type.
 */
public abstract class DialogBuilder<T> {
	protected ObservableValue<String> titleP, headerP, messageP;
	protected String title, header, message;
	protected ButtonType[] buttonTypes;
	protected Node content, expandable;
	protected Stage owner;

	public DialogBuilder() {
		Application.get().requireGUI();
	}

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
	public DialogBuilder<T> buttonTypes(ButtonType... values) {
		this.buttonTypes = values;
		return this;
	}

	protected void validate() {
		if (this.title == null && this.titleP == null)
			throw new IllegalArgumentException("title");
	}

	/**
	 * Builds a new dialog from this builder.
	 *
	 * @return The new dialog.
	 */
	public Dialog<T> build() {
		validate();
		Dialog<T> d = provide();

		d.initOwner(this.owner == null ? Application.get().getStage().orElse(null) : this.owner);

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

		if (this.buttonTypes != null)
			p.getButtonTypes().setAll(this.buttonTypes);

		return d;
	}

	protected abstract Dialog<T> provide();

	/**
	 * Shows the dialog.
	 */
	public void show() {
		validate();
		if (Platform.isFxApplicationThread())
			build().show();
		else
			Platform.runLater(this::show);
	}

	/**
	 * Shows the dialog and blocks until the window is closed.
	 *
	 * @return The result. See {@link Dialog#resultProperty()}.
	 */
	public Optional<T> showAndWait() {
		validate();
		if (Platform.isFxApplicationThread())
			return build().showAndWait();
		else {
			AtomicReference<Optional<T>> result = new AtomicReference<>();
			CountDownLatch lock = new CountDownLatch(1);
			Platform.runLater(() -> {
				result.set(showAndWait());
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
