package net.smoofyuniverse.common.fxui.control;

import java.util.function.Function;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

public class LabelCell<T> extends ListCell<T> {
	public final Function<T, String> itemToString;
	
	private Label label = new Label();
	private String text;
	
	public LabelCell(Function<T, String> itemToString) {
		this.itemToString = itemToString;
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	}
	
	@Override
	public void updateIndex(int i) {
		super.updateIndex(i);
		if (i == -1)
			setGraphic(null);
		else
			updateLabel();
	}
	
	@Override
	protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		
		if (empty || item == null) {
			this.text = null;
			setGraphic(null);
		} else if (item != null) {
			this.text = this.itemToString.apply(item);
			setGraphic(this.label);
			updateLabel();
		}
	}
	
	private void updateLabel() {
		this.label.setText(this.text == null ? null : this.text.replace("%index%", Integer.toString(getIndex() +1)));
	}
}
