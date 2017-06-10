package net.smoofyuniverse.common.logger.appender;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import net.smoofyuniverse.common.app.Application;

public class DatedRollingFileAppender implements LogAppender {
	public static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	private LocalDate currentDate;
	private BufferedWriter writer;
	private Path file;
	
	private Path directory;
	private String prefix, suffix;
	private DateTimeFormatter formatter;
	
	public DatedRollingFileAppender(Path dir, String prefix, String suffix) {
		this(dir, prefix, DEFAULT_FORMAT, suffix);
	}
	
	public DatedRollingFileAppender(Path dir, String prefix, DateTimeFormatter formatter, String suffix) {
		this.directory = dir;
		this.suffix = suffix;
		this.formatter = formatter;
		this.prefix = prefix;
		
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {}
	}
	
	public Path getDirectory() {
		return this.directory;
	}
	
	public String getPrefix() {
		return this.prefix;
	}
	
	public String getSuffix() {
		return this.suffix;
	}
	
	public DateTimeFormatter getFormatter() {
		return this.formatter;
	}
	
	public BufferedWriter getWriter() throws IOException {
		if (this.file != getFile()) {
			close();
			this.writer = Files.newBufferedWriter(this.file, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		}
		return this.writer;
	}
	
	public Path getFile() {
		LocalDate today = LocalDate.now();
		if (!today.equals(this.currentDate)) {
			this.file = this.directory.resolve(this.prefix + this.formatter.format(today) + this.suffix);
			this.currentDate = today;
		}
		return this.file;
	}
	
	@Override
	public void appendRaw(String msg) {
		try {
			getWriter().write(msg);
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
	
	public static DatedRollingFileAppender logs() {
		return new DatedRollingFileAppender(Application.get().getWorkingDirectory().resolve("logs/"), "", ".log");
	}
}
