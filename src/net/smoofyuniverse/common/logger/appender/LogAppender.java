/*******************************************************************************
 * Copyright (C) 2017 Hugo Dupanloup (Yeregorix)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package net.smoofyuniverse.common.logger.appender;

import net.smoofyuniverse.common.logger.core.LogMessage;
import net.smoofyuniverse.common.logger.formatter.DefaultFormatter;
import net.smoofyuniverse.common.logger.formatter.LogFormatter;

import java.util.Arrays;
import java.util.Collection;

public interface LogAppender {
	
	public static FormattedAppender formattedParent(LogAppender... childs) {
		return formattedParent(new DefaultFormatter(true), childs);
	}
	
	public static FormattedAppender formattedParent(Collection<LogAppender> childs) {
		return formattedParent(new DefaultFormatter(true), childs);
	}
	
	public static FormattedAppender formattedParent(LogFormatter formatter, LogAppender... childs) {
		return formattedParent(formatter, Arrays.asList(childs));
	}
	
	public static FormattedAppender formattedParent(LogFormatter formatter, Collection<LogAppender> childs) {
		return new FormattedAppender(new ParentAppender(childs), formatter);
	}

	public default void append(LogMessage msg) {
		appendRaw(msg.text + System.lineSeparator());
	}

	public default void appendRaw(String msg) {
		throw new UnsupportedOperationException("Row message not supported");
	}

	public default void close() {}
}
