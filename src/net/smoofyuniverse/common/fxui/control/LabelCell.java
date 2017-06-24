/*******************************************************************************
 * Copyright (C) 2017 Hugo Dupanloup (Yeregorix)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package net.smoofyuniverse.common.fxui.control;

import java.util.function.BiFunction;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

public class LabelCell<T> extends ListCell<T> {
	private BiFunction<Integer, T, String> toText;
	private Label label = new Label();
	private String text;
	
	public LabelCell(BiFunction<Integer, T, String> toText) {
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		this.toText = toText;
	}
	
	@Override
	public void updateIndex(int i) {
		super.updateIndex(i);
		if (i == -1)
			setGraphic(null);
		else
			updateLabel();
	}
	
	@Override
	protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		
		if (empty || item == null) {
			setGraphic(null);
		} else if (item != null) {
			setGraphic(this.label);
			this.text = this.toText.apply(getIndex(), item);
			updateLabel();
		}
	}
	
	private void updateLabel() {
		this.label.setText(text);
	}
}
