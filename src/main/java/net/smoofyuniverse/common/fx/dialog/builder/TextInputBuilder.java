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

import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import net.smoofyuniverse.common.fx.dialog.TextInputDialog;

/**
 * A {@link TextInputDialog} builder.
 */
public class TextInputBuilder extends DialogBuilder<String> {
	private TextField field;

	/**
	 * Sets the field.
	 *
	 * @param field The field.
	 * @return this.
	 */
	public TextInputBuilder field(TextField field) {
		this.field = field;
		return this;
	}

	@Override
	protected Dialog<String> provide() {
		return new TextInputDialog(this.field == null ? new TextField() : this.field);
	}
}
