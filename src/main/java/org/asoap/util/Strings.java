package org.asoap.util;

/**
 * Utility class to help manipulate Strings. 
 * 
 * @author Marlon Silva Carvalho
 */
final public class Strings {

	/**
	 * Check if the last char in the passed "string" is 'ch'.
	 * 
	 * @param string String to be checked.
	 * @param ch Character to search for.
	 * @return True if exists.
	 */
	public final static boolean hasCharAtEnd(final String string, final char ch) {
		boolean result = false;

		if (string.charAt(string.length() - 1) == ch) {
			result = true;
		}

		return result;
	}

	/**
	 * If there is a backslasha at the of passed string, remove it!
	 * 
	 * @param string String to search for.
	 * @return Result string.
	 */
	public final static String removeLastBackslashIfExists(final String string) {
		String result = string;

		if (Strings.hasCharAtEnd(string, '/')) {
			result = string.substring(0, string.length() - 1);
		}

		return result;
	}

	public final static boolean isEmpty(final String string) {
		return string == null || "".equals(string);
	}

}
