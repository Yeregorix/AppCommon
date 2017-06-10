package net.smoofyuniverse.common.util;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

public class GridUtil {
	public static ColumnConstraints createColumn() {
		return new ColumnConstraints();
	}

	public static ColumnConstraints createColumn(Priority p) {
		ColumnConstraints c = new ColumnConstraints();
		c.setHgrow(p);
		return c;
	}

	public static ColumnConstraints createColumn(boolean fill) {
		ColumnConstraints c = new ColumnConstraints();
		c.setFillWidth(fill);
		return c;
	}

	public static ColumnConstraints createColumn(double percent) {
		ColumnConstraints c = new ColumnConstraints();
		c.setPercentWidth(percent);
		return c;
	}

	public static RowConstraints createRow() {
		return new RowConstraints();
	}

	public static RowConstraints createRow(Priority p) {
		RowConstraints r = new RowConstraints();
		r.setVgrow(p);
		return r;
	}

	public static RowConstraints createRow(boolean fill) {
		RowConstraints r = new RowConstraints();
		r.setFillHeight(fill);
		return r;
	}

	public static RowConstraints createRow(double percent) {
		RowConstraints r = new RowConstraints();
		r.setPercentHeight(percent);
		return r;
	}
}
