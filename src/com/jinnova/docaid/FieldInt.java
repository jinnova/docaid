package com.jinnova.docaid;

import java.text.ParseException;

/*
 * Empty string is for zero
 * */
public class FieldInt extends Field<Integer, String> {
	
	public boolean maskZero = false;
	
	FieldInt() {
		super("");
	}

	@Override
	String trim(String v) {
		if (v == null) {
			return null;
		}
		return v.trim();
	}

	@Override
	String asEditing(Integer v) {
		if (v == null) {
			return "";
		}
		if (maskZero && v == 0) {
			return "";
		}
		return String.valueOf(v);
	}

	@Override
	Integer parse(String v) throws ParseException {
		if (v == null || "".equals(v.trim())) {
			return null;
		}
		try {
			return Integer.parseInt(v);
		} catch (NumberFormatException ne) {
			throw new ParseException(v + ": Không phải con số", 0);
		}
	}
	
	private static int maskNull(Integer f) {
		if (f == null) {
			return 0;
		}
		return f;
	}
	
	public boolean isModified() {
		Integer editingValue = getValue();
		return maskNull(backupValue) != maskNull(editingValue);
	}
}