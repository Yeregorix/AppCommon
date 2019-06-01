/*
 * Copyright (c) 2017-2019 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.common.fx.field;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class IntegerField extends NumberField {
	public final int minValue, maxValue;
	private IntegerProperty value = new SimpleIntegerProperty();
	private boolean ignore = false;

	public IntegerField(int value) {
		this(Integer.MIN_VALUE, Integer.MAX_VALUE, value);
	}

	public IntegerField(int min, int max) {
		this(min, max, min);
	}

	public IntegerField(int min, int max, int value) {
		if (min > max)
			throw new IllegalArgumentException("min, max");
		if (value < min || value > max)
			throw new IllegalArgumentException("value");

		this.minValue = min;
		this.maxValue = max;

		setValue(value);
		setText(Integer.toString(value));

		this.value.addListener((v, oldV, newV) -> {
			if (!this.ignore)
				setText(newV.toString());
		});
	}

	@Override
	public void replaceText(int start, int end, String text) {
		String newText, curText = getText();
		int newPos;

		if (text.equals("-")) {
			if (curText.startsWith("-")) {
				newText = curText.substring(1);
				newPos = start - 1;
			} else {
				newText = "-" + curText;
				newPos = start + 1;
			}
		} else {
			newText = curText.substring(0, start) + text + curText.substring(end);
			newPos = start + text.length();
		}

		int newValue;
		try {
			newValue = Integer.parseInt(newText);
		} catch (NumberFormatException e) {
			return;
		}

		if (newValue < this.minValue || newValue > this.maxValue)
			return;

		this.ignore = true;
		setValue(newValue);
		setText(newText);
		selectRange(newPos, newPos);
		this.ignore = false;
	}

	@Override
	public IntegerProperty valueProperty() {
		return this.value;
	}

	public int getValue() {
		return this.value.get();
	}

	public void setValue(int value) {
		this.value.set(value);
	}
}
