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

package net.smoofyuniverse.common.app;

import javafx.application.Platform;
import net.smoofyuniverse.common.event.Event;
import net.smoofyuniverse.common.event.core.ListenerRegistration;
import net.smoofyuniverse.common.task.SimpleTask;
import net.smoofyuniverse.common.task.Task;
import net.smoofyuniverse.common.util.IOUtil;
import net.smoofyuniverse.common.util.StringUtil;
import net.smoofyuniverse.logger.core.LogMessage;
import net.smoofyuniverse.logger.core.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public final class App {

	public static Logger getLogger(String name) {
		return get().getLoggerFactory().provideLogger(name);
	}

	public static Path getResource(String localResource) throws IOException, URISyntaxException {
		return get().getResourceLoader().toPath(App.class, localResource);
	}

	public static Application get() {
		return Application.get();
	}

	public static boolean registerListener(ListenerRegistration l) {
		return get().getEventManager().register(l);
	}

	public static boolean postEvent(Event e) {
		return get().getEventManager().postEvent(e);
	}

	public static boolean isShutdown() {
		return get().getState() == State.SHUTDOWN;
	}

	public static String formatLog(LogMessage msg) {
		return StringUtil.format(msg.time) + " [" + msg.logger.getName() + "] " + msg.level.name() + " - " + msg.getText() + System.lineSeparator();
	}

	public static String transformLog(String msg) {
		return msg.replace(IOUtil.USER_HOME, "USER_HOME");
	}

	public static boolean submit(Consumer<Task> consumer) {
		return submit(consumer, new SimpleTask());
	}

	public static boolean submit(Consumer<Task> consumer, Task task) {
		task.setCancelled(false);
		try {
			consumer.accept(task);
		} catch (Exception e) {
			task.cancel();
			App.getLogger("App").error("An exception occurred while executing a task.", e);
		}
		return task.isCancelled();
	}

	public static void runLater(Runnable runnable) {
		CountDownLatch lock = new CountDownLatch(1);
		Platform.runLater(() -> {
			runnable.run();
			lock.countDown();
		});
		try {
			lock.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
