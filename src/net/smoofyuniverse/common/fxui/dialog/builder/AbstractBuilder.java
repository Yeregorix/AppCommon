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

package net.smoofyuniverse.common.fxui.dialog.builder;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import net.smoofyuniverse.common.app.App;
import net.smoofyuniverse.common.util.StringUtil;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractBuilder<T> {
	protected StringProperty titleP, headerP, messageP;
	protected String title, header, message;
	protected ButtonType[] buttonTypes;
	protected Node content, expandable;
	protected Stage owner;
	
	public AbstractBuilder<T> owner(Stage s) {
		this.owner = s;
		return this;
	}
	
	public AbstractBuilder<T> title(String s) {
		this.titleP = null;
		this.title = s;
		return this;
	}
	
	public AbstractBuilder<T> title(StringProperty s) {
		this.titleP = s;
		this.title = null;
		return this;
	}
	
	public AbstractBuilder<T> header(String s) {
		this.headerP = null;
		this.header = s;
		return this;
	}
	
	public AbstractBuilder<T> header(StringProperty s) {
		this.headerP = s;
		this.header = null;
		return this;
	}
	
	public AbstractBuilder<T> message(String s) {
		this.messageP = null;
		this.message = s;
		return this;
	}
	
	public AbstractBuilder<T> message(StringProperty s) {
		this.messageP = s;
		this.message = null;
		return this;
	}
	
	public AbstractBuilder<T> content(Node n) {
		this.content = n;
		return this;
	}
	
	public AbstractBuilder<T> expandable(Node n) {
		this.expandable = n;
		return this;
	}
	
	public AbstractBuilder<T> buttonTypes(ButtonType... t) {
		this.buttonTypes = t;
		return this;
	}
	
	public AbstractBuilder<T> message(Throwable t) {
		return message(StringUtil.simpleFormat(t));
	}
	
	public boolean valid() {
		return this.title != null;
	}
	
	public void validate() {
		if (!valid())
			throw new IllegalStateException("Invalid builder");
	}
	
	public Dialog<T> build() {
		validate();
		Dialog<T> d = provide();

		d.initOwner(this.owner == null ? App.get().getStage() : this.owner);
		
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
	
	public void show() {
		validate();
		if (Platform.isFxApplicationThread())
			build().show();
		else
			Platform.runLater(this::show);
	}
	
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
	
	public boolean submitAndWait() {
		throw new UnsupportedOperationException();
	}
}
