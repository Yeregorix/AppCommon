/*******************************************************************************
 * Copyright (C) 2017 Hugo Dupanloup (Yeregorix)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package net.smoofyuniverse.common.installation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class InstallationDetails {
	public static final int FORMAT_VERSION = 1;
	
	private Map<String, Integer> versions = new HashMap<>();
	private boolean changed = false;
	private Path file;
	
	public InstallationDetails(Path file) {
		this.file = file;
	}
	
	public int getVersion(String key) {
		return this.versions.getOrDefault(key, -1);
	}
	
	public void setVersion(String key, int value) {
		Integer oldV = this.versions.put(key, value);
		if (oldV == null || oldV != value)
			this.changed = true;
	}
	
	public void clear() {
		if (!this.versions.isEmpty())
			this.changed = true;
		this.versions.clear();
	}
	
	public Optional<Path> getDefaultFile() {
		return Optional.ofNullable(this.file);
	}
	
	public boolean changed() {
		return this.changed;
	}
	
	public void read() throws IOException {
		read(this.file);
	}
	
	public void read(Path file) throws IOException {
		if (!Files.exists(file)) {
			this.versions.clear();
			this.changed = false;
			return;
		}
		
		try (DataInputStream in = new DataInputStream(Files.newInputStream(file))) {
			 read(in);
		}
	}
	
	public void read(DataInputStream in) throws IOException {
		int version = in.readInt();
		if (version != FORMAT_VERSION)
			throw new IOException("Invalid format version: " + version);
		
		this.versions.clear();
		this.changed = false;
		
		int count = in.readInt();
		for (int i = 0; i < count; i++)
			this.versions.put(in.readUTF(), in.readInt());
	}
	
	public void save() throws IOException {
		save(this.file);
		this.changed = false;
	}
	
	public void save(Path file) throws IOException {
		try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(file))) {
			save(out);
		}
	}
	
	public void save(DataOutputStream out) throws IOException {
		out.writeInt(FORMAT_VERSION);
		
		out.writeInt(this.versions.size());
		for (Entry<String, Integer> e : this.versions.entrySet()) {
			out.writeUTF(e.getKey());
			out.writeInt(e.getValue());
		}
	}
}
