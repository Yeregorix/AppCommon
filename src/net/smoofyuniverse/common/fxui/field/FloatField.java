package net.smoofyuniverse.common.fxui.field;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;

public class FloatField extends NumberField {
	private FloatProperty value = new SimpleFloatProperty();
	public final float min, max;
	
	public FloatField(float value) {
		this(Float.MIN_VALUE, Float.MAX_VALUE, value);
	}

	public FloatField(float min, float max) {
		this(min, max, min);
	}

	public FloatField(float min, float max, float value) {
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
				float defaultV = this.min > 0 ? this.min : 0;
				if (this.value.get() == defaultV)
					setText(oldV);
				else
					this.value.set(defaultV);
				return;
			}
			
			try {
				float floatV = Float.parseFloat(newV);
				if (floatV < this.min || floatV > this.max)
					setText(oldV);
				else
					this.value.set(floatV);
			} catch (Throwable t) {
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
}
