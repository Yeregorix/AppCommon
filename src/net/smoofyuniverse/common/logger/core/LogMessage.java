package net.smoofyuniverse.common.logger.core;

import java.time.LocalTime;

public final class LogMessage {
	public final LocalTime time;
	public final LogLevel level;
	public final ILogger logger;
	public final Thread thread;
	public final String text;
	
	public LogMessage(LogLevel level, ILogger logger, String text) {
		this(level, logger, Thread.currentThread(), text);
	}
	
	public LogMessage(LogLevel level, ILogger logger, Thread thread, String text) {
		this(LocalTime.now(), level, logger, thread, text);
	}
	
	public LogMessage(LocalTime time, LogLevel level, ILogger logger, Thread thread, String text) {
		this.time = time;
		this.level = level;
		this.logger = logger;
		this.thread = thread;
		this.text = text;
	}
}
