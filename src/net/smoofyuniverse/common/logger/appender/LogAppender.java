package net.smoofyuniverse.common.logger.appender;

import java.util.Arrays;
import java.util.Collection;
import net.smoofyuniverse.common.logger.core.LogMessage;
import net.smoofyuniverse.common.logger.formatter.DefaultFormatter;
import net.smoofyuniverse.common.logger.formatter.LogFormatter;

public interface LogAppender {
	
	public default void append(LogMessage msg) {
		appendRaw(msg.text + System.lineSeparator());
	};
	
	public default void appendRaw(String msg) {
		throw new UnsupportedOperationException("Row message not supported");
	};
	
	public default void close() {};
	
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
}
