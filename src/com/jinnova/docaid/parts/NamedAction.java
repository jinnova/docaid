package com.jinnova.docaid.parts;

public abstract class NamedAction {

	final String name;
	
	NamedAction(String name) {
		this.name = name;
	}
	
	abstract void run();
}
