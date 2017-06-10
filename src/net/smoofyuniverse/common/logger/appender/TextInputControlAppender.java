package net.smoofyuniverse.common.logger.appender;

import javafx.application.Platform;
import javafx.scene.control.TextInputControl;

public class TextInputControlAppender implements LogAppender {
	private TextInputControl textInput;

	public TextInputControlAppender(TextInputControl textInput) {
		this.textInput = textInput;
	}
	
	public TextInputControl getTextInput() {
		return this.textInput;
	}
	
	@Override
	public void appendRaw(String msg) {
		if (Platform.isFxApplicationThread())
			this.textInput.appendText(msg);
		else
			Platform.runLater(() -> this.textInput.appendText(msg));
	}
}
