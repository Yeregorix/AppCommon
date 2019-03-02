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

package net.smoofyuniverse.common.util;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class StringUtil {
	public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault()),
			DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneId.systemDefault()),
			TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
	
	private static final char[] hexchars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	public static String replaceParameters(String value, String... parameters) {
		int size = parameters.length;
		if (size == 0)
			return value;

		if (size > 40) {
			Map<String, String> map = new HashMap<>(size / 2 + 1);
			for (int i = 0; i + 1 < size; i += 2)
				map.put(parameters[i], parameters[i + 1]);

			return replaceParameters(value, map::get);
		} else {
			return replaceParameters(value, arg -> {
				for (int i = 0; i + 1 < size; i += 2) {
					if (arg.equals(parameters[i]))
						return parameters[i + 1];
				}
				return null;
			});
		}
	}

	public static String replaceParameters(String value, Function<String, String> params) {
		if (value == null)
			return null;

		int size = value.length();
		StringBuilder b = new StringBuilder(), argB = new StringBuilder();
		boolean inArg = false;

		for (int i = 0; i < size; i++) {
			char ch = value.charAt(i);
			if (inArg) {
				if (ch == '}') {
					String arg = argB.toString(), param = params.apply(arg);
					if (param == null)
						b.append('{').append(arg).append('}');
					else
						b.append(param);

					argB.setLength(0);
					inArg = false;
				} else
					argB.append(ch);
			} else {
				if (ch == '{')
					inArg = true;
				else
					b.append(ch);
			}
		}

		if (inArg)
			b.append('{').append(argB);

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

	public static String format(LocalTime time) {
		return String.format("%02d:%02d:%02d", time.getHour(), time.getMinute(), time.getSecond());
	}
	
	public static Duration parseDuration(String s) {
		long value = 0;
		Duration result = Duration.ZERO;
		for (char c : s.toCharArray()) {
			switch (c) {
			case 'y':
				result = result.plus(value *365, ChronoUnit.DAYS);
				value = 0;
				break;
			case 'M':
				result = result.plus(value *30, ChronoUnit.DAYS);
				value = 0;
				break;
			case 'd':
				result = result.plus(value, ChronoUnit.DAYS);
				value = 0;
				break;
			case 'h':
				result = result.plus(value, ChronoUnit.HOURS);
				value = 0;
				break;
			case 'm':
				result = result.plus(value, ChronoUnit.MINUTES);
				value = 0;
				break;
			case 's':
				result = result.plus(value, ChronoUnit.SECONDS);
				value = 0;
				break;
			default:
				int n = Character.getNumericValue(c);
				if (n < 0 || n > 9)
					return Duration.ZERO;
				value *= 10;
				value += n;
			}
		}
		return result;
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
		StringBuilder s = new StringBuilder(bytes.length * 2);
		for (byte b : bytes)
			s.append(hexchars[(b & 0xF0) >> 4]).append(hexchars[b & 0x0F]);
		 return s.toString();
	}
}
