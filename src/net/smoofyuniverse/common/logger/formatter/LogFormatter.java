package net.smoofyuniverse.common.logger.formatter;

import net.smoofyuniverse.common.logger.core.LogMessage;

public interface LogFormatter {
	public String accept(LogMessage msg);
}
