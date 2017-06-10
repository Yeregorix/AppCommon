package net.smoofyuniverse.common.logger.filter;

import net.smoofyuniverse.common.logger.core.LogMessage;

public interface LogFilter {
	public static final LogFilter DENY_ALL = (msg) -> false, ALLOW_ALL = (msg) -> true;
	
	public boolean allow(LogMessage msg);
}
