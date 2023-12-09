/*
 * Copyright (c) 2017-2023 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StringUtil {
	private static final char[] hexchars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	public static String capitalize(String value) {
		return capitalize(value, Locale.getDefault());
	}

	public static String capitalize(String value, Locale locale) {
		if (value.isEmpty())
			return value;
		return value.substring(0, 1).toUpperCase(locale) + value.substring(1);
	}

	public static List<String> parseCommandLine(String line) {
		List<String> args = new ArrayList<>();

		if (line == null)
			return args;

		StringBuilder b = new StringBuilder();

		boolean inQuote = false;
		int backslashs = 0;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);

			if (c == '\\')
				backslashs++;
			else if (c == '"') {
				b.append("\\".repeat(backslashs / 2));

				if (backslashs % 2 == 0)
					inQuote = !inQuote;
				else
					b.append('"');

				backslashs = 0;
			} else {
				b.append("\\".repeat(backslashs));
				backslashs = 0;

				if (c == ' ' && !inQuote) {
					args.add(b.toString());
					b.setLength(0);
				} else
					b.append(c);
			}
		}

		b.append("\\".repeat(backslashs));

		args.add(b.toString());
		return args;
	}

	public static String toCommandLine(Iterable<String> arguments) {
		StringBuilder b = new StringBuilder();
		int lastEnd = 0;

		for (String arg : arguments) {
			boolean quote = false;
			int backslashs = 0;

			for (int i = 0; i < arg.length(); i++) {
				char c = arg.charAt(i);

				if (c == '\\')
					backslashs++;
				else {
					if (c == '"') {
						b.append("\\".repeat(backslashs + 1));
					} else if (c == ' ') {
						quote = true;
					}

					backslashs = 0;
				}

				b.append(c);
			}

			if (arg.isEmpty())
				quote = true;

			if (quote) {
				b.insert(lastEnd, '"');
				b.append('"');
			}

			b.append(' ');
			lastEnd = b.length();
		}

		if (lastEnd != 0)
			b.setLength(lastEnd - 1);
		return b.toString();
	}
	
	public static String simpleFormat(Throwable t) {
		String s = t.getClass().getSimpleName();
		if (t.getMessage() != null)
			s += ": " + t.getMessage();
		return s;
	}

	public static String toHexString(byte[] bytes) {
		char[] chars = new char[bytes.length << 1];
		int i = 0;
		for (byte b : bytes) {
			chars[i++] = hexchars[(b >> 4) & 15];
			chars[i++] = hexchars[b & 15];
		}
		return new String(chars);
	}
}
