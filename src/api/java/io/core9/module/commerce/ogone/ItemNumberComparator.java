package io.core9.module.commerce.ogone;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemNumberComparator implements Comparator<String> {
	
	private final static Pattern ITEM_NUMBER_PATTERN = Pattern.compile("([^0-9]+)([0-9]+)$");

	private static Integer findIntegerPart(String input) {
		Matcher matcher = ITEM_NUMBER_PATTERN.matcher(input);
		if(matcher.find()) {
			try {
				return Integer.valueOf(matcher.group(2));
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	private static String findStringPart(String input) {
		Matcher matcher = ITEM_NUMBER_PATTERN.matcher(input);
		if(matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

	@Override
    public int compare(String s1, String s2) {
		String s1Str = findStringPart(s1);
		if(s1Str == null) {
			return s1.compareTo(s2);
		}
		String s2Str = findStringPart(s2);
		if(s2Str == null || !s1Str.equals(s2Str)) {
			return s1.compareTo(s2);
		}
		Integer s1Int = findIntegerPart(s1);
		Integer s2Int = findIntegerPart(s2);
		if(s1Int == null || s2Int == null) {
			return s1.compareTo(s2);
		} else {
            return s1Int.compareTo(s2Int);
        }
    }
}