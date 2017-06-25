/*
 * Copyright (c) 2017 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
		} catch (IOException ignored) {
		}
		this.writer = null;
	}
}
