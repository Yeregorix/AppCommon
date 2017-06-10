package net.smoofyuniverse.common.logger.appender;

import net.smoofyuniverse.common.logger.core.LogMessage;
import net.smoofyuniverse.common.logger.formatter.LogFormatter;

public class FormattedAppender implements LogAppender {
	private LogAppender delegate;
	private LogFormatter formatter;

	public FormattedAppender(LogAppender delegate, LogFormatter formatter) {
		this.delegate = delegate;
		this.formatter = formatter;
	}
	
	public LogAppender getDelegate() {
		return this.delegate;
	}
	
	public LogFormatter getFormatter() {
		return this.formatter;
	}
	
	@Override
	public void append(LogMessage msg) {
		this.delegate.appendRaw(this.formatter.accept(msg));
	}
	
	@Override
	public void appendRaw(String msg) {
		this.delegate.appendRaw(msg);
	}
}
