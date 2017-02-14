package com.jinnova.docaid;

public interface FieldFunction {

	boolean isModified();
	
	String validate();
	
	void applyChanges();
	
	void cancelChanges();

	void copy(FieldFunction fieldFunction, boolean forced);
}
