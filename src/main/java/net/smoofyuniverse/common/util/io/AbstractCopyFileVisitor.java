/*
 * Copyright (c) 2017-2020 Hugo Dupanloup (Yeregorix)
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

import net.smoofyuniverse.common.util.ArrayUtil;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public abstract class AbstractCopyFileVisitor implements FileVisitor<Path> {
	protected final CopyOption[] options;
	protected final boolean replace;

	protected AbstractCopyFileVisitor(CopyOption... options) {
		this.options = options;
		this.replace = ArrayUtil.contains(options, StandardCopyOption.REPLACE_EXISTING);
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		Path dest = getDestination(dir);
		if (Files.exists(dest)) {
			if (Files.isDirectory(dest))
				return FileVisitResult.CONTINUE;

			if (this.replace)
				Files.delete(dest);
			else
				throw new FileAlreadyExistsException(dest.toString());
		}
		Files.createDirectory(dest);
		return FileVisitResult.CONTINUE;
	}

	protected abstract Path getDestination(Path path);

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		Files.copy(file, getDestination(file), this.options);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
		throw e;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException e) {
		return FileVisitResult.CONTINUE;
	}
}
