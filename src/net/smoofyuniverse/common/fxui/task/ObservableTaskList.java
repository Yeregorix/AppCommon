package net.smoofyuniverse.common.fxui.task;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
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
		private Button cancel = new Button("Annuler");
		private Label title = new Label();
		private Label message = new Label();
		
		private GridPane pane = new GridPane();
		
		public TaskCell() {
			this.title.setFont(FONT_14);
			this.message.setFont(FONT_12);
			
			this.progressBar.setPadding(new Insets(6, 0, 6, 0));
			this.progressBar.setMaxWidth(Double.MAX_VALUE);
			this.progressBar.setMaxHeight(8);
			
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
			} else if (item != null) {
				this.progressBar.progressProperty().bind(item.progressProperty());
				this.cancel.disableProperty().bind(Bindings.or(item.cancelledProperty(), Bindings.not(item.cancellableProperty())));
				this.title.textProperty().bind(item.titleProperty());
				this.message.textProperty().bind(item.messageProperty());
				
				setGraphic(this.pane);
			}
		}
	}
}
