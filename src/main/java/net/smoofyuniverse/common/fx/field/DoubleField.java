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

package net.smoofyuniverse.common.fx.field;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.text.DecimalFormat;
import java.text.ParsePosition;

public class DoubleField extends NumberField {
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

	public final double minValue, maxValue;
	private DoubleProperty value = new SimpleDoubleProperty();
	private boolean ignore = false;

	public DoubleField(double value) {
		this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, value);
	}

	public DoubleField(double min, double max) {
		this(min, max, min);
	}

	public DoubleField(double min, double max, double value) {
		if (min > max)
			throw new IllegalArgumentException("min, max");
		if (value < min || value > max)
			throw new IllegalArgumentException("value");

		this.minValue = min;
		this.maxValue = max;

		this.value.addListener((v, oldV, newV) -> {
			if (!this.ignore)
				setText(format(newV.doubleValue()));
		});
		setValue(value);
	}

	private static String format(double value) {
		return DECIMAL_FORMAT.format(value);
	}

	@Override
	public DoubleProperty valueProperty() {
		return this.value;
	}

	public double getValue() {
		return this.value.get();
	}

	public void setValue(double value) {
		this.value.set(value);
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

		Number n = parse(newText);
		if (n == null)
			return;

		double newValue = n.doubleValue();
		if (newValue < this.minValue || newValue > this.maxValue)
			return;

		this.ignore = true;
		setValue(newValue);
		setText(newText);
		selectRange(newPos, newPos);
		this.ignore = false;
	}

	private static Number parse(String value) {
		ParsePosition position = new ParsePosition(0);
		Number number = DECIMAL_FORMAT.parse(value, position);
		return position.getIndex() == value.length() ? number : null;
	}

	static {
		DECIMAL_FORMAT.setMaximumFractionDigits(Integer.MAX_VALUE);
		DECIMAL_FORMAT.setGroupingUsed(false);
	}
}
