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

package net.smoofyuniverse.common.fx.dialog;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import net.smoofyuniverse.common.fx.dialog.builder.AlertBuilder;
import net.smoofyuniverse.common.fx.dialog.builder.NumberInputBuilder;
import net.smoofyuniverse.common.fx.dialog.builder.TextInputBuilder;
import net.smoofyuniverse.common.fx.field.NumberField;
import net.smoofyuniverse.common.task.ProgressTask;

import java.util.function.Consumer;

public class Popup {
	
	public static TextInputBuilder textInput(TextField f) {
		return textInput().field(f);
	}
	
	public static TextInputBuilder textInput() {
		return new TextInputBuilder();
	}
	
	public static NumberInputBuilder numberInput(NumberField f) {
		return numberInput().field(f);
	}

	public static NumberInputBuilder numberInput() {
		return new NumberInputBuilder();
	}

	public static AlertBuilder consumer(Consumer<ProgressTask> c) {
		return alert().consumer(c);
	}
	
	public static AlertBuilder info() {
		return alert(AlertType.INFORMATION);
	}
	
	public static AlertBuilder warning() {
		return alert(AlertType.WARNING);
	}
	
	public static AlertBuilder confirmation() {
		return alert(AlertType.CONFIRMATION);
	}
	
	public static AlertBuilder error() {
		return alert(AlertType.ERROR);
	}
	
	public static AlertBuilder alert(AlertType t) {
		return alert().type(t);
	}
	
	public static AlertBuilder alert() {
		return new AlertBuilder();
	}
}
