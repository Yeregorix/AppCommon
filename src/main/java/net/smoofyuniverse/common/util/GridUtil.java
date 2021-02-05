/*
 * Copyright (c) 2017-2021 Hugo Dupanloup (Yeregorix)
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
