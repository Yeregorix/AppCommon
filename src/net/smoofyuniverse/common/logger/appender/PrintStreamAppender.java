package net.smoofyuniverse.common.logger.appender;

import java.io.PrintStream;

public class PrintStreamAppender implements LogAppender {
	private PrintStream stream;
	
	public PrintStreamAppender(PrintStream stream) {
		this.stream = stream;
	}
	
	public PrintStream getStream() {
		return this.stream;
	}
	
	@Override
	public void appendRaw(String msg) {
		this.stream.print(msg);
	}
	
	@Override
	public void close() {
		this.stream.close();
	}
	
	public static PrintStreamAppender system() {
		return new PrintStreamAppender(System.out);
	}
}
