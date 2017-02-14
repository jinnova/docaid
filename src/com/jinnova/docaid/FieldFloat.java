package com.jinnova.docaid;

import java.text.ParseException;

public class FieldFloat extends Field<Float, String> {
	
	public boolean maskZero = false;
	
	FieldFloat() {
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
	String asEditing(Float v) {
		if (v == null) {
			return "";
		}
		if (maskZero && v == 0) {
			return "";
		}
		if (Math.ceil(v) == v) {
			return String.valueOf(v.intValue());
		} else {
			return String.valueOf(v);
		}
	}

	@Override
	Float parse(String v) throws ParseException {
		if (v == null || "".equals(v.trim())) {
			return null;
		}
		try {
			return Float.parseFloat(v);
		} catch (NumberFormatException ne) {
			throw new ParseException(v + ": Không phải con số", 0);
		}
	}
	
	private static float maskNull(Float f) {
		if (f == null) {
			return 0;
		}
		return f;
	}
	
	public boolean isModified() {
		Float editingValue = getValue();
		return maskNull(backupValue) != maskNull(editingValue);
	}
	
	public static String getString(Float f) {
		if (f == null || f == 0) {
			return "";
		}
		if (Math.ceil(f) == f) {
			return String.valueOf(f.intValue());
		} else {
			return String.valueOf(f);
		}
	}

}
