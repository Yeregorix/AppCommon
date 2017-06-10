package net.smoofyuniverse.common.fxui.dialog.builder;

import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import net.smoofyuniverse.common.fxui.dialog.TextInputDialog;

public class TextInputBuilder extends AbstractBuilder<String> {
	private TextField field;
	
	public TextInputBuilder field(TextField f) {
		this.field = f;
		return this;
	}

	@Override
	protected Dialog<String> provide() {
		return new TextInputDialog(this.field == null ? new TextField() : this.field);
	}
}
