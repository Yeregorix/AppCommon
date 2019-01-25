/*
 * Copyright (c) 2017 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.common.fxui.task;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import net.smoofyuniverse.common.app.Translations;
import net.smoofyuniverse.common.fxui.control.EmptySelectionModel;
import net.smoofyuniverse.common.util.GridUtil;

public class ObservableTaskList extends ListView<ObservableTask> {
	
	public ObservableTaskList() {
		this(FXCollections.observableArrayList());
	}
	
	public ObservableTaskList(ObservableList<ObservableTask> tasks) {
		super(tasks);
		setCellFactory(l -> new TaskCell());
		setSelectionModel(new EmptySelectionModel<>());
	}
	
	private static class TaskCell extends ListCell<ObservableTask> {
		private static final Font FONT_14 = new Font("Monospaced", 14), FONT_12 = new Font("Monospaced", 12);
		
		private ProgressBar progressBar = new ProgressBar();
		private Button cancel = new Button();
		private Label title = new Label();
		private Label message = new Label();
		
		private GridPane pane = new GridPane();
		
		public TaskCell() {
			this.title.setFont(FONT_14);
			this.message.setFont(FONT_12);
			
			this.progressBar.setPadding(new Insets(6, 0, 6, 0));
			this.progressBar.setMaxWidth(Double.MAX_VALUE);
			this.progressBar.setMaxHeight(8);

			this.cancel.textProperty().bind(Translations.task_list_cancel);
			this.cancel.setPrefWidth(80);
			this.cancel.setOnAction((e) -> {
				ObservableTask t = getItem();
				if (t != null)
					t.setCancelled(true);
			});
			
			this.pane.add(this.title, 0, 0);
			this.pane.add(this.cancel, 1, 0);
			this.pane.add(this.progressBar, 0, 1, 2, 1);
			this.pane.add(this.message, 0, 2, 2, 1);
			
			this.pane.getColumnConstraints().addAll(GridUtil.createColumn(Priority.ALWAYS), GridUtil.createColumn());
			
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		}
		
		@Override
		public void updateIndex(int i) {
			super.updateIndex(i);
			if (i == -1)
				setGraphic(null);
		}
		
		@Override
		protected void updateItem(ObservableTask item, boolean empty) {
			super.updateItem(item, empty);
			
			if (empty || item == null) {
				this.progressBar.progressProperty().unbind();
				this.cancel.disableProperty().unbind();
				this.title.textProperty().unbind();
				this.message.textProperty().unbind();
				
				setGraphic(null);
			} else {
				this.progressBar.progressProperty().bind(item.progressProperty());
				this.cancel.disableProperty().bind(Bindings.or(item.cancelledProperty(), Bindings.not(item.cancellableProperty())));
				this.title.textProperty().bind(item.titleProperty());
				this.message.textProperty().bind(item.messageProperty());
				
				setGraphic(this.pane);
			}
		}
	}
}
