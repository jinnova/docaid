package com.jinnova.docaid;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.jinnova.docaid.messages"; //$NON-NLS-1$
	public static String Prescription_day_comma_spaces;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
