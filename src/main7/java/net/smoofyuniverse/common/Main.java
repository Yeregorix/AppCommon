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

package net.smoofyuniverse.common;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;

public class Main {
	private static Instrumentation instrumentation;

	public static void premain(String args, Instrumentation inst) {
		System.out.println("Instrumentation initialized (premain).");
		instrumentation = inst;
	}

	public static void agentmain(String args, Instrumentation inst) {
		System.out.println("Instrumentation initialized (agentmain).");
		instrumentation = inst;
	}

	public static Instrumentation getInstrumentation() {
		return instrumentation;
	}

	public static void main(String[] args) throws Throwable {
		JavaVersionChecker.requireVersion(17);
		launchApplication(Main.class.getClassLoader().loadClass("net.smoofyuniverse.common.app.ApplicationManager"), args);
	}

	public static void launchApplication(Class<?> main, String[] args) throws Throwable {
		System.out.println("Launching main class " + main.getName() + " ...");
		try {
			main.getMethod("main", String[].class).invoke(null, new Object[]{args});
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
}
