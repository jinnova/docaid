package com.jinnova.docaid.parts;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

public class Snippet {

public static void main (String [] args) {
	Display display = new Display ();
	final Shell shell = new Shell (display);
	final TabFolder tabFolder = new TabFolder (shell, SWT.BORDER);
	Rectangle clientArea = shell.getClientArea ();
	tabFolder.setLocation (clientArea.x, clientArea.y);
	for (int i=0; i<6; i++) {
		TabItem item = new TabItem (tabFolder, SWT.NONE);
		item.setText ("TabItem " + i);
		Button button = new Button (tabFolder, SWT.PUSH);
		button.setText ("Page " + i);
		item.setControl (button);
	}
	tabFolder.pack ();
	shell.pack ();
	shell.open ();
	while (!shell.isDisposed ()) {
		if (!display.readAndDispatch ()) display.sleep ();
	}
	display.dispose ();
}
} 
