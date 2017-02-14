package com.jinnova.docaid;

import java.util.ArrayList;

public class FieldSet implements FieldFunction {
	
	private final ArrayList<FieldFunction> allFields = new ArrayList<FieldFunction>();

	public void copy(FieldFunction other) {
		copy(other, false);
	}
	
	@Override
	public void copy(FieldFunction other, boolean forced) {
		//System.out.println("Copying from " + this + " to " + other + ": " + forced);
		for (int i = 0; i < allFields.size(); i++) {
			FieldFunction f = allFields.get(i);
			f.copy((FieldFunction) ((FieldSet) other).allFields.get(i), forced);
		}
	}

	FieldString newFieldString() {
		FieldString f = new FieldString();
		allFields.add(f);
		return f;
	}

	FieldString newFieldString(String emptyErrorMsg) {
		FieldString f = new FieldString();
		f.emptyErrorMsg = emptyErrorMsg;
		allFields.add(f);
		return f;
	}

	FieldInt newFieldInt() {
		FieldInt f = new FieldInt();
		allFields.add(f);
		return f;
	}

	FieldFloat newFieldFloat() {
		FieldFloat f = new FieldFloat();
		allFields.add(f);
		return f;
	}

	FieldDate newFieldDate() {
		FieldDate f = new FieldDate();
		allFields.add(f);
		return f;
	}

	FieldTimestamp newFieldTimestamp() {
		FieldTimestamp f = new FieldTimestamp();
		allFields.add(f);
		return f;
	}

	FieldBoolean newFieldBoolean() {
		FieldBoolean f = new FieldBoolean();
		allFields.add(f);
		return f;
	}

	FieldBoolean newFieldBoolean(boolean defValue) {
		FieldBoolean f = new FieldBoolean();
		f.loadValue(defValue);
		allFields.add(f);
		return f;
	}
	
	void addField(FieldFunction f) {
		allFields.add(f);
	}
	
	void removeField(FieldFunction f) {
		allFields.remove(f);
	}
	
	public boolean isModified() {
		for (FieldFunction f : allFields) {
			if (f.isModified()) {
				return true;
			}
		}
		return false;
	}
	
	public String validate() {
		for (FieldFunction f : allFields) {
			String err = f.validate();
			if (err != null) {
				return err;
			}
		}
		return null;
	}
	
	public void cancelChanges() {
		for (FieldFunction f : allFields) {
			f.cancelChanges();
		}
	}
	
	public void applyChanges() {
		for (FieldFunction f : allFields) {
			f.applyChanges();
		}
	}
}
