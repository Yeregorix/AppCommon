package net.smoofyuniverse.common.fxui.field;

import javafx.beans.property.Property;
import javafx.scene.control.TextField;

public abstract class NumberField extends TextField {
	
	public NumberField() {
	}
	
	public NumberField(String text) {
		super(text);
	}
	
	public abstract Property<Number> valueProperty();
}
