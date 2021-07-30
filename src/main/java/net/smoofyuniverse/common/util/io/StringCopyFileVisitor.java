/*
 * Copyright (c) 2017-2021 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.common.util.io;

import java.nio.file.CopyOption;
import java.nio.file.Path;

public class StringCopyFileVisitor extends AbstractCopyFileVisitor {
	protected final Path source, target;
	protected final int sourceLength;

	public StringCopyFileVisitor(Path source, Path target, CopyOption... options) {
		super(options);
		this.source = source;
		this.target = target;

		String sep = source.getFileSystem().getSeparator();
		String str = source.toString();
		this.sourceLength = str.endsWith(sep) ? str.length() : (str.length() + sep.length());
	}

	@Override
	protected Path getDestination(Path path) {
		String str = path.toString();
		return str.length() > this.sourceLength ? this.target.resolve(str.substring(this.sourceLength)) : this.target;
	}
}
