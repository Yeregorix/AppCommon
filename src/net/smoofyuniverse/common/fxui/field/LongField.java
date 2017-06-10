package net.smoofyuniverse.common.fxui.field;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

public class LongField extends NumberField {
	private LongProperty value = new SimpleLongProperty();
	public final long min, max;
	
	public LongField(long value) {
		this(Long.MIN_VALUE, Long.MAX_VALUE, value);
	}

	public LongField(long min, long max) {
		this(min, max, min);
	}

	public LongField(long min, long max, long value) {
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
				long defaultV = this.min > 0 ? this.min : 0;
				if (this.value.get() == defaultV)
					setText(oldV);
				else
					this.value.set(defaultV);
				return;
			}
			
			try {
				long longV = Long.parseLong(newV);
				if (longV < min || longV > max)
					setText(oldV);
				else
					this.value.set(longV);
			} catch (Throwable t) {
				setText(oldV);
			}
		});
	}

	@Override
	public LongProperty valueProperty() {
		return this.value;
	}
	
	public long getValue() {
		return this.value.get();
	}
}
