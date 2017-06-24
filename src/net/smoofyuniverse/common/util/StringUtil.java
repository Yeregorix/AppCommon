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
package net.smoofyuniverse.common.util;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class StringUtil {
	public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault()),
			DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneId.systemDefault()),
			TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
	
	private static final char[] hexchars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	
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
		int first = arg.indexOf('*');
		if (first == -1)
			return (s) -> s.equals(arg);
			
		if (arg.length() == 1)
			return (s) -> true;
			
		int last = arg.lastIndexOf('*');
		String start = arg.substring(0, first), end = arg.substring(last +1);
		if (first == last)
			return (s) -> s.startsWith(start) && s.endsWith(end);
			
		String[] parts = arg.substring(first +1, last).split("\\*"); // TODO Order
		
		return (s) -> {
			if (!(s.startsWith(start) && s.endsWith(end)))
				return false;
			
			for (String p : parts) {
				if (!s.contains(p))
					return false;
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
		StringBuffer s = new StringBuffer(bytes.length *2);
		for (byte b : bytes)
			s.append(hexchars[(b & 0xF0) >> 4]).append(hexchars[b & 0x0F]);
		 return s.toString();
	}
}
