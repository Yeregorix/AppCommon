package net.smoofyuniverse.common.fxui.field;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class DoubleField extends NumberField {
	private DoubleProperty value = new SimpleDoubleProperty();
	public final double min, max;
	
	public DoubleField(double value) {
		this(Double.MIN_VALUE, Double.MAX_VALUE, value);
	}

	public DoubleField(double min, double max) {
		this(min, max, min);
	}

	public DoubleField(double min, double max, double value) {
		super(String.valueOf(value));
		if (min > max)
			throw new IllegalArgumentException();
		if (value < min || value > max)
			throw new IllegalArgumentException();
		
		this.value.set(value);
		this.min = min;
		this.max = max;

		this.value.addListener((v, oldV, newV) -> setText(String.valueOf(newV)));

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
				double doubleV = Double.parseDouble(newV);
				if (doubleV < this.min || doubleV > this.max)
					setText(oldV);
				else
					this.value.set(doubleV);
			} catch (Throwable t) {
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
}
