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

package net.smoofyuniverse.common.logger.core;

import java.time.LocalTime;

public interface ILogger {
	
	public String getName();
	
	public default void trace(String text) {
		log(LogLevel.TRACE, text);
	}
	
	public default void trace(Throwable throwable) {
		log(LogLevel.TRACE, throwable);
	}
	
	public default void trace(String text, Throwable throwable) {
		log(LogLevel.TRACE, text, throwable);
	}
	
	public default void debug(String text) {
		log(LogLevel.DEBUG, text);
	}
	
	public default void debug(Throwable throwable) {
		log(LogLevel.DEBUG, throwable);
	}
	
	public default void debug(String text, Throwable throwable) {
		log(LogLevel.DEBUG, text, throwable);
	}
	
	public default void info(String text) {
		log(LogLevel.INFO, text);
	}
	
	public default void info(Throwable throwable) {
		log(LogLevel.INFO, throwable);
	}
	
	public default void info(String text, Throwable throwable) {
		log(LogLevel.INFO, text, throwable);
	}
	
	public default void warn(String text) {
		log(LogLevel.WARN, text);
	}
	
	public default void warn(Throwable throwable) {
		log(LogLevel.WARN, throwable);
	}
	
	public default void warn(String text, Throwable throwable) {
		log(LogLevel.WARN, text, throwable);
	}
	
	public default void error(String text) {
		log(LogLevel.ERROR, text);
	}
	
	public default void error(Throwable throwable) {
		log(LogLevel.ERROR, throwable);
	}
	
	public default void error(String text, Throwable throwable) {
		log(LogLevel.ERROR, text, throwable);
	}
	
	public default void log(LogLevel level, String text) {
		log(new LogMessage(level, this, text), null);
	}
	
	public default void log(LogLevel level, Throwable throwable) {
		log(new LogMessage(level, this, "An error occurred."), throwable);
	}
	
	public default void log(LogLevel level, String text, Throwable throwable) {
		log(new LogMessage(level, this, text), throwable);
	}
	
	public default void log(LogLevel level, Thread thread, String text, Throwable throwable) {
		log(new LogMessage(level, this, thread, text), throwable);
	}
	
	public default void log(LocalTime time, LogLevel level, Thread thread, String text, Throwable throwable) {
		log(new LogMessage(time, level, this, thread, text), throwable);
	}
	
	public void log(LogMessage msg, Throwable throwable);
}
