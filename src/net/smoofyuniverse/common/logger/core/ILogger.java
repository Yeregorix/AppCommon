package net.smoofyuniverse.common.logger.core;

import java.time.LocalTime;

public interface ILogger {
	
	public String getName();
	
	public default void trace(String text) {
		log(LogLevel.TRACE, text);
	}
	
	public default void trace(Throwable throwable) {
		log(LogLevel.TRACE, throwable);
	}
	
	public default void trace(String text, Throwable throwable) {
		log(LogLevel.TRACE, text, throwable);
	}
	
	public default void debug(String text) {
		log(LogLevel.DEBUG, text);
	}
	
	public default void debug(Throwable throwable) {
		log(LogLevel.DEBUG, throwable);
	}
	
	public default void debug(String text, Throwable throwable) {
		log(LogLevel.DEBUG, text, throwable);
	}
	
	public default void info(String text) {
		log(LogLevel.INFO, text);
	}
	
	public default void info(Throwable throwable) {
		log(LogLevel.INFO, throwable);
	}
	
	public default void info(String text, Throwable throwable) {
		log(LogLevel.INFO, text, throwable);
	}
	
	public default void warn(String text) {
		log(LogLevel.WARN, text);
	}
	
	public default void warn(Throwable throwable) {
		log(LogLevel.WARN, throwable);
	}
	
	public default void warn(String text, Throwable throwable) {
		log(LogLevel.WARN, text, throwable);
	}
	
	public default void error(String text) {
		log(LogLevel.ERROR, text);
	}
	
	public default void error(Throwable throwable) {
		log(LogLevel.ERROR, throwable);
	}
	
	public default void error(String text, Throwable throwable) {
		log(LogLevel.ERROR, text, throwable);
	}
	
	public default void log(LogLevel level, String text) {
		log(new LogMessage(level, this, text), null);
	}
	
	public default void log(LogLevel level, Throwable throwable) {
		log(new LogMessage(level, this, "An error occurred."), throwable);
	}
	
	public default void log(LogLevel level, String text, Throwable throwable) {
		log(new LogMessage(level, this, text), throwable);
	}
	
	public default void log(LogLevel level, Thread thread, String text, Throwable throwable) {
		log(new LogMessage(level, this, thread, text), throwable);
	}
	
	public default void log(LocalTime time, LogLevel level, Thread thread, String text, Throwable throwable) {
		log(new LogMessage(time, level, this, thread, text), throwable);
	}
	
	public void log(LogMessage msg, Throwable throwable);
}
