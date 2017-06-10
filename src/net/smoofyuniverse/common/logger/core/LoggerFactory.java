package net.smoofyuniverse.common.logger.core;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import net.smoofyuniverse.common.logger.appender.LogAppender;

public class LoggerFactory {
	private Map<String, Logger> loggers = new ConcurrentHashMap<>();
	private LogAppender appender;
	
	public LoggerFactory(LogAppender appender) {
		this.appender = appender;
	}
	
	public LogAppender getAppender() {
		return this.appender;
	}
	
	public Logger provideLogger(String name) {
		Logger l = this.loggers.get(name);
		if (l == null) {
			l = new Logger(this, name);
			this.loggers.put(name, l);
		}
		return l;
	}
	
	public Optional<Logger> getLogger(String name) {
		return Optional.ofNullable(this.loggers.get(name));
	}
	
	public Collection<Logger> getLoggers() {
		return this.loggers.values();
	}
}
