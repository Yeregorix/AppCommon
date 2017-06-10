package net.smoofyuniverse.common.logger.appender;

import java.io.BufferedWriter;
import java.io.IOException;

public class BufferedWriterAppender implements LogAppender {
	private BufferedWriter writer;
	
	public BufferedWriterAppender(BufferedWriter writer) {
		this.writer = writer;
	}
	
	public BufferedWriter getWriter() {
		return this.writer;
	}
	
	@Override
	public void appendRaw(String msg) {
		if (this.writer == null)
			return;
		try {
			this.writer.write(msg);
			this.writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void close() {
		if (this.writer == null)
			return;
		try {
			this.writer.close();
		} catch (IOException e) {}
		this.writer = null;
	}
}
