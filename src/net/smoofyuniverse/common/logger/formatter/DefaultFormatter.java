package net.smoofyuniverse.common.logger.formatter;

import java.time.LocalTime;

import net.smoofyuniverse.common.logger.core.LogMessage;
import net.smoofyuniverse.common.util.ResourceUtil;

public class DefaultFormatter implements LogFormatter {
	public final boolean hideUserHome;
	
	public DefaultFormatter(boolean hideUserHome) {
		this.hideUserHome = hideUserHome;
	}

	@Override
	public String accept(LogMessage msg) {
		return format(msg.time) + " [" + msg.logger.getName() + "] " + msg.level.name() + " - " + (this.hideUserHome ? msg.text.replace(ResourceUtil.USER_HOME, "USER_HOME") : msg.text) + System.lineSeparator();
	}
	
	private String format(LocalTime time) {
		return String.format("%02d:%02d:%02d", time.getHour(), time.getMinute(), time.getSecond());
	}
}
