/*
 * Copyright (c) 2017-2020 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.common.fx.dialog.fix;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.lang.reflect.Field;

public class CloseRequestFixer implements EventHandler<WindowEvent> {
	private static final Field dialogF, stageF;

	private final Dialog<?> dialog;
	private final EventHandler<WindowEvent> stageHandler;

	private CloseRequestFixer(Dialog<?> dialog, EventHandler<WindowEvent> stageHandler) {
		this.dialog = dialog;
		this.stageHandler = stageHandler;
	}

	@Override
	public void handle(WindowEvent event) {
		DialogEvent dialogEvent = new DialogEvent(this.dialog, DialogEvent.DIALOG_CLOSE_REQUEST);
		Event.fireEvent(this.dialog, dialogEvent);
		if (dialogEvent.isConsumed())
			event.consume();
		else
			this.stageHandler.handle(event);
	}

	public static void apply(Dialog<?> dialog) {
		Stage stage = getStage(dialog);
		if (stage == null)
			return;

		EventHandler<WindowEvent> stageHandler = stage.getOnCloseRequest();
		if (!(stageHandler instanceof CloseRequestFixer))
			stage.setOnCloseRequest(new CloseRequestFixer(dialog, stageHandler));
	}

	private static Stage getStage(Dialog<?> dialog) {
		try {
			return (Stage) stageF.get(dialogF.get(dialog));
		} catch (Exception e) {
			return null;
		}
	}

	static {
		try {
			dialogF = Dialog.class.getDeclaredField("dialog");
			dialogF.setAccessible(true);
			stageF = Class.forName("javafx.scene.control.HeavyweightDialog").getDeclaredField("stage");
			stageF.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
