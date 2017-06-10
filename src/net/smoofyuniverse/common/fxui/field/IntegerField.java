package net.smoofyuniverse.common.fxui.field;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class IntegerField extends NumberField {
	private IntegerProperty value = new SimpleIntegerProperty();
	public final int min, max;
	
	public IntegerField(int value) {
		this(Integer.MIN_VALUE, Integer.MAX_VALUE, value);
	}

	public IntegerField(int min, int max) {
		this(min, max, min);
	}

	public IntegerField(int min, int max, int value) {
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
				int defaultV = this.min > 0 ? this.min : 0;
				if (this.value.get() == defaultV)
					setText(oldV);
				else
					this.value.set(defaultV);
				return;
			}
			
			try {
				int intV = Integer.parseInt(newV);
				if (intV < this.min || intV > this.max)
					setText(oldV);
				else
					this.value.set(intV);
			} catch (Throwable t) {
				setText(oldV);
			}
		});
	}

	@Override
	public IntegerProperty valueProperty() {
		return this.value;
	}
	
	public int getValue() {
		return this.value.get();
	}
}
