package net.smoofyuniverse.common.logger.appender;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileAppender implements LogAppender {
	private BufferedWriter writer;
	
	private StandardOpenOption[] options;
	private Charset charset;
	private Path file;
	
	public FileAppender(Path file, StandardOpenOption... options) {
		this(file, StandardCharsets.UTF_8, options);
	}
	
	public FileAppender(Path file, Charset charset, StandardOpenOption... options) {
		this.file = file;
	}
	
	public BufferedWriter getWriter() {
		return this.writer;
	}
	
	public StandardOpenOption[] getOptions() {
		return this.options;
	}
	
	public Charset getCharset() {
		return this.charset;
	}
	
	public Path getFile() {
		return this.file;
	}
	
	@Override
	public void appendRaw(String msg) {
		try {
			if (this.writer == null)
				this.writer = Files.newBufferedWriter(this.file, this.charset, this.options);
			
			this.writer.write(msg);
			this.writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void close() {
		try {
			this.writer.close();
		} catch (IOException e) {}
		this.writer = null;
	}
}
