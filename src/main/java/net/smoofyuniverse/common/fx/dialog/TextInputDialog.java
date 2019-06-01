/*
 * Copyright (c) 2017-2019 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.smoofyuniverse.common.fx.dialog;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class TextInputDialog extends Dialog<String> {
	private GridPane grid;
	private Label label;
	private TextField field;
	
	public TextInputDialog(TextField f) {
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
        
        setResultConverter((b) -> b == ButtonType.OK ? this.field.getText() : null);
	}
	
	public TextField getField() {
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
