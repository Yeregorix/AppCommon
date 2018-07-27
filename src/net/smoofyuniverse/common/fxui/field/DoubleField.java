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

package net.smoofyuniverse.common.fxui.field;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;

public class DoubleField extends NumberField {
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

	public final double min, max;
	private DoubleProperty value = new SimpleDoubleProperty();

	public DoubleField(double value) {
		this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, value);
	}

	public DoubleField(double min, double max) {
		this(min, max, min);
	}

	public DoubleField(double min, double max, double value) {
		super(format(value));
		if (min > max)
			throw new IllegalArgumentException();
		if (value < min || value > max)
			throw new IllegalArgumentException();

		this.value.set(value);
		this.min = min;
		this.max = max;

		this.value.addListener((v, oldV, newV) -> setText(format(newV.doubleValue())));

		textProperty().addListener((v, oldV, newV) -> {
			if (newV.isEmpty()) {
				double defaultV = this.min > 0 ? this.min : 0;
				if (this.value.get() == defaultV)
					setText(oldV);
				else
					this.value.set(defaultV);
				return;
			}

			try {
				double doubleV = parse(newV);
				if (doubleV < this.min || doubleV > this.max)
					setText(oldV);
				else
					this.value.set(doubleV);
			} catch (ParseException e) {
				setText(oldV);
			}
		});
	}

	@Override
	public DoubleProperty valueProperty() {
		return this.value;
	}

	public double getValue() {
		return this.value.get();
	}

	public static String format(double value) {
		return DECIMAL_FORMAT.format(value);
	}

	public static double parse(String value) throws ParseException {
		ParsePosition position = new ParsePosition(0);
		Number number = DECIMAL_FORMAT.parse(value, position);
		if (position.getIndex() != value.length())
			throw new ParseException("Failed to parse the entire string: " + value, position.getIndex());
		return number.doubleValue();
	}

	public void setValue(double value) {
		this.value.set(value);
	}

	static {
		DECIMAL_FORMAT.setMaximumFractionDigits(Integer.MAX_VALUE);
		DECIMAL_FORMAT.setGroupingUsed(false);
	}
}
