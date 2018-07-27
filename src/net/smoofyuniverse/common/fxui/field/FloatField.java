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

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;

public class FloatField extends NumberField {
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

	public final float min, max;
	private FloatProperty value = new SimpleFloatProperty();

	public FloatField(float value) {
		this(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, value);
	}

	public FloatField(float min, float max) {
		this(min, max, min);
	}

	public FloatField(float min, float max, float value) {
		super(format(value));
		if (min > max)
			throw new IllegalArgumentException();
		if (value < min || value > max)
			throw new IllegalArgumentException();

		this.value.set(value);
		this.min = min;
		this.max = max;

		this.value.addListener((v, oldV, newV) -> setText(format(newV.floatValue())));

		textProperty().addListener((v, oldV, newV) -> {
			if (newV.isEmpty()) {
				float defaultV = this.min > 0 ? this.min : 0;
				if (this.value.get() == defaultV)
					setText(oldV);
				else
					this.value.set(defaultV);
				return;
			}

			try {
				float floatV = parse(newV);
				if (floatV < this.min || floatV > this.max)
					setText(oldV);
				else
					this.value.set(floatV);
			} catch (ParseException e) {
				setText(oldV);
			}
		});
	}

	@Override
	public FloatProperty valueProperty() {
		return this.value;
	}

	public float getValue() {
		return this.value.get();
	}

	public static String format(float value) {
		return DECIMAL_FORMAT.format(value);
	}

	public static float parse(String value) throws ParseException {
		ParsePosition position = new ParsePosition(0);
		Number number = DECIMAL_FORMAT.parse(value, position);
		if (position.getIndex() != value.length())
			throw new ParseException("Failed to parse the entire string: " + value, position.getIndex());
		return number.floatValue();
	}

	public void setValue(float value) {
		this.value.set(value);
	}

	static {
		DECIMAL_FORMAT.setMaximumFractionDigits(Integer.MAX_VALUE);
		DECIMAL_FORMAT.setGroupingUsed(false);
	}
}
