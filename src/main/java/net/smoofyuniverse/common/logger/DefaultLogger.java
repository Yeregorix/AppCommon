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

package net.smoofyuniverse.common.logger;

import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

import java.time.LocalTime;

public class DefaultLogger extends MarkerIgnoringBase {
	private final String simpleName;

	public DefaultLogger(String name) {
		this.name = name;
		int i = name.lastIndexOf(".");
		this.simpleName = i == -1 ? name : name.substring(i + 1);
	}

	protected String time() {
		LocalTime t = LocalTime.now();
		return String.format("%02d:%02d:%02d", t.getHour(), t.getMinute(), t.getSecond());
	}

	protected void log(String level, String msg) {
		System.out.println(time() + " [" + this.simpleName + "] " + level + " - " + msg);
	}

	protected void _trace(String msg) {
		log("TRACE", msg);
	}

	protected void _debug(String msg) {
		log("DEBUG", msg);
	}

	protected void _info(String msg) {
		log("INFO ", msg);
	}

	protected void _warn(String msg) {
		log("WARN ", msg);
	}

	protected void _error(String msg) {
		log("ERROR", msg);
	}

	@Override
	public boolean isTraceEnabled() {
		return false;
	}

	@Override
	public void trace(String msg) {
		if (isTraceEnabled())
			_trace(msg);
	}

	@Override
	public void trace(String format, Object arg) {
		if (isTraceEnabled())
			_trace(MessageFormatter.format(format, arg).getMessage());
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		if (isTraceEnabled())
			_trace(MessageFormatter.format(format, arg1, arg2).getMessage());
	}

	@Override
	public void trace(String format, Object... arguments) {
		if (isTraceEnabled())
			_trace(MessageFormatter.arrayFormat(format, arguments).getMessage());
	}

	@Override
	public void trace(String msg, Throwable t) {
		if (isTraceEnabled()) {
			_trace(msg);
			t.printStackTrace();
		}
	}

	@Override
	public boolean isDebugEnabled() {
		return true;
	}

	@Override
	public void debug(String msg) {
		if (isDebugEnabled())
			_debug(msg);
	}

	@Override
	public void debug(String format, Object arg) {
		if (isDebugEnabled())
			_debug(MessageFormatter.format(format, arg).getMessage());
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		if (isDebugEnabled())
			_debug(MessageFormatter.format(format, arg1, arg2).getMessage());
	}

	@Override
	public void debug(String format, Object... arguments) {
		if (isDebugEnabled())
			_debug(MessageFormatter.arrayFormat(format, arguments).getMessage());
	}

	@Override
	public void debug(String msg, Throwable t) {
		if (isDebugEnabled()) {
			_debug(msg);
			t.printStackTrace();
		}
	}

	@Override
	public boolean isInfoEnabled() {
		return true;
	}

	@Override
	public void info(String msg) {
		if (isInfoEnabled())
			_info(msg);
	}

	@Override
	public void info(String format, Object arg) {
		if (isInfoEnabled())
			_info(MessageFormatter.format(format, arg).getMessage());
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		if (isInfoEnabled())
			_info(MessageFormatter.format(format, arg1, arg2).getMessage());
	}

	@Override
	public void info(String format, Object... arguments) {
		if (isInfoEnabled())
			_info(MessageFormatter.arrayFormat(format, arguments).getMessage());
	}

	@Override
	public void info(String msg, Throwable t) {
		if (isInfoEnabled()) {
			_info(msg);
			t.printStackTrace();
		}
	}

	@Override
	public boolean isWarnEnabled() {
		return true;
	}

	@Override
	public void warn(String msg) {
		if (isWarnEnabled())
			_warn(msg);
	}

	@Override
	public void warn(String format, Object arg) {
		if (isWarnEnabled())
			_warn(MessageFormatter.format(format, arg).getMessage());
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		if (isWarnEnabled())
			_warn(MessageFormatter.format(format, arg1, arg2).getMessage());
	}

	@Override
	public void warn(String format, Object... arguments) {
		if (isWarnEnabled())
			_warn(MessageFormatter.arrayFormat(format, arguments).getMessage());
	}

	@Override
	public void warn(String msg, Throwable t) {
		if (isWarnEnabled()) {
			_warn(msg);
			t.printStackTrace();
		}
	}

	@Override
	public boolean isErrorEnabled() {
		return true;
	}

	@Override
	public void error(String msg) {
		if (isErrorEnabled())
			_error(msg);
	}

	@Override
	public void error(String format, Object arg) {
		if (isErrorEnabled())
			_error(MessageFormatter.format(format, arg).getMessage());
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		if (isErrorEnabled())
			_error(MessageFormatter.format(format, arg1, arg2).getMessage());
	}

	@Override
	public void error(String format, Object... arguments) {
		if (isErrorEnabled())
			_error(MessageFormatter.arrayFormat(format, arguments).getMessage());
	}

	@Override
	public void error(String msg, Throwable t) {
		if (isErrorEnabled()) {
			_error(msg);
			t.printStackTrace();
		}
	}
}
