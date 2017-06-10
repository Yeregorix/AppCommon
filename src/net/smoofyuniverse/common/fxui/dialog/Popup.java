package net.smoofyuniverse.common.fxui.dialog;

import java.util.function.Consumer;

import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import net.smoofyuniverse.common.fxui.dialog.builder.AlertBuilder;
import net.smoofyuniverse.common.fxui.dialog.builder.NumberInputBuilder;
import net.smoofyuniverse.common.fxui.dialog.builder.TextInputBuilder;
import net.smoofyuniverse.common.fxui.field.NumberField;
import net.smoofyuniverse.common.fxui.task.ObservableTask;

public class Popup {
	
	public static TextInputBuilder textInput(TextField f) {
		return textInput().field(f);
	}
	
	public static TextInputBuilder textInput() {
		return new TextInputBuilder();
	}
	
	public static NumberInputBuilder numberInput(NumberField f) {
		return numberInput().field(f);
	}
	
	public static NumberInputBuilder numberInput() {
		return new NumberInputBuilder();
	}
	
	public static AlertBuilder consumer(Consumer<ObservableTask> c) {
		return alert().consumer(c);
	}
	
	public static AlertBuilder info() {
		return alert(AlertType.INFORMATION);
	}
	
	public static AlertBuilder warning() {
		return alert(AlertType.WARNING);
	}
	
	public static AlertBuilder confirmation() {
		return alert(AlertType.CONFIRMATION);
	}
	
	public static AlertBuilder error() {
		return alert(AlertType.ERROR);
	}
	
	public static AlertBuilder alert(AlertType t) {
		return alert().type(t);
	}
	
	public static AlertBuilder alert() {
		return new AlertBuilder();
	}
}
