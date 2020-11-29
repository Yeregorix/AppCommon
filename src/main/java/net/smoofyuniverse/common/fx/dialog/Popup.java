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

package net.smoofyuniverse.common.fx.dialog;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import net.smoofyuniverse.common.fx.dialog.builder.AlertBuilder;
import net.smoofyuniverse.common.fx.dialog.builder.NumberInputBuilder;
import net.smoofyuniverse.common.fx.dialog.builder.TextInputBuilder;
import net.smoofyuniverse.common.fx.field.NumberField;
import net.smoofyuniverse.common.task.ProgressTask;

import java.util.function.Consumer;

/**
 * A static helper with essential dialog methods.
 */
public class Popup {

	/**
	 * Creates a new text input builder with the given field.
	 *
	 * @param field The field.
	 * @return The new builder.
	 */
	public static TextInputBuilder textInput(TextField field) {
		return textInput().field(field);
	}

	/**
	 * Creates a new text input builder.
	 *
	 * @return The new builder.
	 */
	public static TextInputBuilder textInput() {
		return new TextInputBuilder();
	}

	/**
	 * Creates a new number input builder with the given field.
	 *
	 * @param field The field.
	 * @return The new builder.
	 */
	public static NumberInputBuilder numberInput(NumberField field) {
		return numberInput().field(field);
	}

	/**
	 * Creates a new number input builder.
	 *
	 * @return The new builder.
	 */
	public static NumberInputBuilder numberInput() {
		return new NumberInputBuilder();
	}

	/**
	 * Creates a new alert builder with the given task consumer.
	 *
	 * @param consumer The task consumer.
	 * @return The new builder.
	 */
	public static AlertBuilder consumer(Consumer<ProgressTask> consumer) {
		return alert().consumer(consumer);
	}

	/**
	 * Creates a new alert builder.
	 *
	 * @return The new builder.
	 */
	public static AlertBuilder alert() {
		return new AlertBuilder();
	}

	/**
	 * Creates a new alert builder with type {@link AlertType#INFORMATION}.
	 *
	 * @return The new builder.
	 */
	public static AlertBuilder info() {
		return alert(AlertType.INFORMATION);
	}

	/**
	 * Creates a new alert builder with the given type.
	 *
	 * @param type The type.
	 * @return The new builder.
	 */
	public static AlertBuilder alert(AlertType type) {
		return alert().type(type);
	}

	/**
	 * Creates a new alert builder with type {@link AlertType#WARNING}.
	 *
	 * @return The new builder.
	 */
	public static AlertBuilder warning() {
		return alert(AlertType.WARNING);
	}

	/**
	 * Creates a new alert builder with type {@link AlertType#CONFIRMATION}.
	 *
	 * @return The new builder.
	 */
	public static AlertBuilder confirmation() {
		return alert(AlertType.CONFIRMATION);
	}

	/**
	 * Creates a new alert builder with type {@link AlertType#ERROR}.
	 *
	 * @return The new builder.
	 */
	public static AlertBuilder error() {
		return alert(AlertType.ERROR);
	}
}
