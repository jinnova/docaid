package com.jinnova.docaid;

public class FieldBoolean extends Field<Boolean, Boolean> {
	
	FieldBoolean() {
		super(true);
	}

	@Override
	Boolean trim(Boolean v) {
		return v;
	}


	@Override
	Boolean asEditing(Boolean v) {
		return v;
	}

	@Override
	Boolean parse(Boolean v) {
		return v;
	}
	
	private static boolean maskNull(Boolean f) {
		if (f == null) {
			return true;
		}
		return f;
	}
	
	public boolean isModified() {
		Boolean editingValue = getValue();
		return maskNull(backupValue) != maskNull(editingValue);
	}
	
}
