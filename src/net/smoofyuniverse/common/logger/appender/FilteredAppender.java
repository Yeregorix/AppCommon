package net.smoofyuniverse.common.logger.appender;

import net.smoofyuniverse.common.logger.core.LogMessage;
import net.smoofyuniverse.common.logger.filter.LogFilter;

public class FilteredAppender implements LogAppender {
	private LogAppender delegate;
	private LogFilter filter;
	
	public FilteredAppender(LogAppender delegate, LogFilter filter) {
		this.delegate = delegate;
		this.filter = filter;
	}
	
	public LogAppender getDelegate() {
		return this.delegate;
	}
	
	public LogFilter getFilter() {
		return this.filter;
	}
	
	@Override
	public void append(LogMessage msg) {
		if (this.filter.allow(msg))
			this.delegate.append(msg);
	}
}
