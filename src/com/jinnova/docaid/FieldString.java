package com.jinnova.docaid;

public class FieldString extends Field<String, String> {
	
	FieldString() {
		super("");
	}

	@Override
	String asEditing(String v) {
		return v;
	}

	@Override
	String parse(String v) {
		return v;
	}

	@Override
	String trim(String v) {
		if (v == null) {
			return null;
		}
		return v.trim();
	}
	
	private static String maskNull(String f) {
		if (f == null) {
			return "";
		}
		return f;
	}
	
	public boolean isModified() {
		String editingValue = getValue();
		return !maskNull(backupValue).equals(maskNull(editingValue));
	}
	
}