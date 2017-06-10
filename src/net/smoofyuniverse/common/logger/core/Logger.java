package net.smoofyuniverse.common.logger.core;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.smoofyuniverse.common.logger.appender.LogAppender;

public final class Logger implements ILogger {
	private LoggerFactory factory;
	private LogAppender appender;
	private String name;
	
	protected Logger(LoggerFactory factory, String name) {
		this.factory = factory;
		this.appender = factory.getAppender();
		this.name = name;
	}
	
	public LoggerFactory getFactory() {
		return this.factory;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void log(LogMessage msg, Throwable throwable) {
		this.appender.append(msg);
		if (throwable != null) {
			StringWriter buffer = new StringWriter();
			throwable.printStackTrace(new PrintWriter(buffer));
			this.appender.appendRaw(buffer.toString());
		}
	}
}
