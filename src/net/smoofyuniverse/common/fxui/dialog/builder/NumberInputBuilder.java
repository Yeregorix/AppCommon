package net.smoofyuniverse.common.fxui.dialog.builder;

import javafx.scene.control.Dialog;
import net.smoofyuniverse.common.fxui.dialog.NumberInputDialog;
import net.smoofyuniverse.common.fxui.field.NumberField;

public class NumberInputBuilder extends AbstractBuilder<Number> {
	private NumberField field;
	
	public NumberInputBuilder field(NumberField f) {
		this.field = f;
		return this;
	}
	
	@Override
	public boolean valid() {
		return super.valid() && this.field != null;
	}

	@Override
	protected Dialog<Number> provide() {
		return new NumberInputDialog(this.field);
	}
}
