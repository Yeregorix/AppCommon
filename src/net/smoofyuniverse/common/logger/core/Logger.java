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

import net.smoofyuniverse.common.logger.appender.LogAppender;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Logger implements ILogger {
	private LoggerFactory factory;
	private LogAppender appender;
	private String name;
	
	protected Logger(LoggerFactory factory, String name) {
		this.factory = factory;
		this.appender = factory.getAppender();
		this.name = name;
	}
	
	public LoggerFactory getFactory() {
		return this.factory;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void log(LogMessage msg, Throwable throwable) {
		this.appender.append(msg);
		if (throwable != null) {
			StringWriter buffer = new StringWriter();
			throwable.printStackTrace(new PrintWriter(buffer));
			this.appender.appendRaw(buffer.toString());
		}
	}
}
