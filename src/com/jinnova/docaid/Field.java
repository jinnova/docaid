package com.jinnova.docaid;

import java.text.ParseException;

public abstract class Field<T, E> implements FieldFunction {

	T backupValue;
	
	private E editingValue;
	
	private T editingValueParsed;
	
	private final E editingNull;
	
	String emptyErrorMsg;
	
	Field(E editingNull) {
		this.editingNull = editingNull;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final void copy(FieldFunction other, boolean forced) {
		T newValue = ((Field<T, E>) other).backupValue;
		if (!forced && this.isModified()) {
			this.backupValue = newValue;
		} else {
			this.loadValue(newValue);
		}
	}
	
	public T getValue() {
		if (editingValueParsed == null) {
			try {
				editingValueParsed = parse(editingValue);
			} catch (ParseException e) {
				//throw new RuntimeException(e);
				editingValueParsed = null;
			}
		}
		return editingValueParsed;
	}
	
	public E getValueAsEditing() {
		//getValue(); //for to parse / invalidate dirty editingValue
		if (editingValue == null) {
			return editingNull;
		}
		return editingValue;
	}
	
	public T getBackupValue() {
		return this.backupValue;
	}
	
	public E getBackupValueAsEditing() {
		return asEditing(this.backupValue);
	}
	
	public void loadValue(T v) {
		this.backupValue = v;
		this.editingValue = asEditing(v);
		this.editingValueParsed = null;
	}
	
	public void changeValue(E editingValue) {
		this.editingValue = editingValue;
		this.editingValueParsed = null;
	}
	
	abstract E asEditing(T v);
	
	abstract T parse(E v) throws ParseException;
	
	abstract E trim(E v);
	
	public boolean isModified() {
		
		T editingValueParsed;
		try {
			editingValueParsed = parse(this.editingValue);
		} catch (ParseException e) {
			return true;
		}
		if (backupValue == null) {
			return editingValueParsed != null;
		} else {
			return !backupValue.equals(editingValueParsed);
		}
	}
	
	public void cancelChanges() {
		this.editingValue = asEditing(this.backupValue);
		this.editingValueParsed = null;
	}
	
	public String validate() {
		if (emptyErrorMsg != null && editingNull.equals(trim(editingValue))) {
			return emptyErrorMsg;
		}
		try {
			parse(editingValue);
			return null;
		} catch (ParseException e) {
			return e.getMessage();
		}
	}
	
	public void applyChanges() {
		/*try {
			this.backupValue = parse(editingValue);
		} catch (ParseException e) {
			throw new RuntimeException();
		}*/
		this.backupValue = getValue();
	}
}
