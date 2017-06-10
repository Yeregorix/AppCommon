package net.smoofyuniverse.common.fxui.dialog;

import com.sun.javafx.scene.control.skin.resources.ControlResources;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import net.smoofyuniverse.common.fxui.field.NumberField;

@SuppressWarnings("restriction")
public class NumberInputDialog extends Dialog<Number> {
	private GridPane grid;
	private Label label;
	private NumberField field;
	
	public NumberInputDialog(NumberField f) {
        DialogPane dialogPane = getDialogPane();
        
        this.field = f;
        this.field.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(this.field, Priority.ALWAYS);
        GridPane.setFillWidth(this.field, true);
        
        this.label = new Label();
        this.label.setMaxWidth(Double.MAX_VALUE);
        this.label.setMaxHeight(Double.MAX_VALUE);
        this.label.getStyleClass().add("content");
        this.label.setWrapText(true);
        this.label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        this.label.textProperty().bind(dialogPane.contentTextProperty());
        
        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);
        
        dialogPane.contentTextProperty().addListener(o -> updateGrid());
        
        setTitle(ControlResources.getString("Dialog.confirm.title"));
        dialogPane.setHeaderText(ControlResources.getString("Dialog.confirm.header"));
        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        updateGrid();
        
        setResultConverter((b) -> b == ButtonType.OK ? this.field.valueProperty().getValue() : null);
	}
	
	public NumberField getField() {
		return this.field;
	}
	
    private void updateGrid() {
    	this.grid.getChildren().clear();
    	this.grid.add(this.label, 0, 0);
    	this.grid.add(this.field, 1, 0);
        getDialogPane().setContent(this.grid);
        Platform.runLater(this.field::requestFocus);
    }
}
