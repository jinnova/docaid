package com.jinnova.docaid.parts;

import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class PartialTextContentAdapter extends TextContentAdapter {

	@Override
	public void insertControlContents(Control control, String text, int cursorPosition) {
		Text ui = (Text) control;
		Point selection = ui.getSelection();
		int[] sel = selectPhrase(ui.getText(), ui.getCaretPosition());
		ui.setSelection(sel[0], sel[1]);
		
		String insert;
		String uiText = ui.getText();
		if (sel[0] == 0 || uiText.charAt(sel[0] - 1) == '.') {
			insert = text.substring(0, 1).toUpperCase() + text.substring(1);
		} else {
			insert = text;
		}
		if (sel[0] != 0) {
			insert = " " + insert;
		}
		((Text) control).insert(insert);
		// Insert will leave the cursor at the end of the inserted text. If this
		// is not what we wanted, reset the selection.
		if (cursorPosition < text.length()) {
			ui.setSelection(selection.x + cursorPosition, selection.x + cursorPosition);
		}
	}
	
	static int[] selectPhrase(String text, int caretPosition) {
		String s = text.substring(0, caretPosition);
		int start = Math.max(s.lastIndexOf(","), s.lastIndexOf("."));
		if (start < 0) {
			start = 0;
		} else {
			if (!text.isEmpty()) {
				start++;
			}
			
		}
		
		//s = text.substring(caretPosition);
		int firstComma = text.indexOf(",", caretPosition);
		if (firstComma < 0) {
			firstComma = text.length();
		}
		int firstDot = text.indexOf(".", caretPosition);
		if (firstDot < 0) {
			firstDot = text.length();
		}
		int end = Math.min(firstComma, firstDot);
		
		System.out.println("select text from " + start + " to " + end);
		return new int[] {start, end};
	}

}
