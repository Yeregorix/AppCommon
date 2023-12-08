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
import java.util.function.Predicate;
import java.util.regex.Pattern;

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

	public static String unescape(String value) {
		if (value == null)
			return null;

		int size = value.length();
		StringBuilder b = new StringBuilder(size), code = new StringBuilder(4);
		boolean hadSlash = false, inUnicode = false;

		for (int i = 0; i < size; i++) {
			char ch = value.charAt(i);
			if (inUnicode) {
				code.append(ch);
				if (code.length() == 4) {
					try {
						b.append((char) Integer.parseInt(code.toString(), 16));

						code.setLength(0);
						inUnicode = false;
						hadSlash = false;
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("Unable to parse unicode value: " + code, e);
					}
				}
				continue;
			}
			if (hadSlash) {
				hadSlash = false;
				switch (ch) {
					case '\\':
						b.append('\\');
						break;
					case '\'':
						b.append('\'');
						break;
					case '\"':
						b.append('"');
						break;
					case 'r':
						b.append('\r');
						break;
					case 'f':
						b.append('\f');
						break;
					case 't':
						b.append('\t');
						break;
					case 'n':
						b.append('\n');
						break;
					case 'b':
						b.append('\b');
						break;
					case 'u':
						inUnicode = true;
						break;
					default:
						b.append(ch);
						break;
				}
				continue;
			}
			if (ch == '\\') {
				hadSlash = true;
				continue;
			}
			b.append(ch);
		}

		if (hadSlash)
			b.append('\\');

		return b.toString();
	}
	
	public static Predicate<String> regexPredicate(String arg) {
		return Pattern.compile(arg).asPredicate();
	}

	public static Predicate<String> simplePredicate(String arg) {
		List<String> l = new ArrayList<>();

		boolean escape = false;
		StringBuilder b = new StringBuilder(arg.length());
		for (int i = 0; i < arg.length(); i++) {
			char c = arg.charAt(i);
			if (c == '*') {
				if (escape) {
					b.append('*');
					escape = false;
				} else {
					l.add(b.toString());
					b.setLength(0);
				}
			} else if (c == '\\') {
				if (escape) {
					b.append('\\');
					escape = false;
				} else {
					escape = true;
				}
			} else {
				b.append(c);
				escape = false;
			}
		}
		l.add(b.toString());

		if (l.size() == 1) {
			String value = l.get(0);
			return s -> s.equals(value);
		}

		String[] parts = l.toArray(new String[0]);
		return s -> {
			if (s.length() < parts[0].length() + parts[parts.length - 1].length())
				return false;

			if (!s.startsWith(parts[0]) || !s.endsWith(parts[parts.length - 1]))
				return false;

			int offset = parts[0].length();
			for (int i = 1; i < parts.length - 1; i++) {
				int j = s.indexOf(parts[i], offset);
				if (j == -1)
					return false;

				offset = j + parts[i].length();
			}

			return true;
		};
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
