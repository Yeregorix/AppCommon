package net.smoofyuniverse.common.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class StringUtil {
	public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault()),
			DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneId.systemDefault()),
			TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
	
	private static final char[] hexchars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	
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
		
		if (start.isEmpty()) {
			if (end.isEmpty()) {
				String middle = start.substring(last +1);
				return (s) -> s.contains(middle);
			}
			return (s) -> s.endsWith(end);
		}
		
		if (end.isEmpty())
			return (s) -> s.startsWith(start);
		
		return (s) -> s.startsWith(start) && s.endsWith(end);
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
