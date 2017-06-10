package net.smoofyuniverse.common.logger.filter;

import java.util.Optional;

import net.smoofyuniverse.common.logger.core.LogLevel;
import net.smoofyuniverse.common.logger.core.LogMessage;

public class MinLevelFilter implements LogFilter {
	private LogLevel level;
	
	public Optional<LogLevel> getMinLevel() {
		return Optional.ofNullable(this.level);
	}
	
	public void setMinLevel(LogLevel l) {
		this.level = l;
	}

	@Override
	public boolean allow(LogMessage msg) {
		return this.level == null ? true : msg.level.ordinal() >= this.level.ordinal();
	}
}
