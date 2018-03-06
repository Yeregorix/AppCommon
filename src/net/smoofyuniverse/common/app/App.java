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

import net.smoofyuniverse.common.event.Event;
import net.smoofyuniverse.common.event.core.ListenerRegistration;
import net.smoofyuniverse.common.util.ResourceUtil;
import net.smoofyuniverse.common.util.StringUtil;
import net.smoofyuniverse.logger.core.LogMessage;
import net.smoofyuniverse.logger.core.Logger;

public final class App {

	public static Logger getLogger(String name) {
		return get().getLoggerFactory().provideLogger(name);
	}

	public static Application get() {
		return Application.get();
	}

	public static String translate(String key) {
		return get().getTranslator().translate(key);
	}

	public static String translate(String key, String... parameters) {
		return get().getTranslator().translate(key, parameters);
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

	public static String format(LogMessage msg) {
		return StringUtil.format(msg.time) + " [" + msg.logger.getName() + "] " + msg.level.name() + " - " + msg.text.replace(ResourceUtil.USER_HOME, "USER_HOME") + System.lineSeparator();
	}
}
