package com.pp.trenerpol.util;

public enum Util {
	;

	public static boolean isValidLong(String code) {
		try {
			Long.parseLong(code);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
}
