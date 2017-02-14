package com.jinnova.docaid.parts;

import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.jinnova.docaid.QueueTicket;
import com.jinnova.docaid.WaitingList;

public class WaitingQueueInfoPart {
	
	private Label currentLabel;
	private Label nextsLabel;
	private Label patientLabel;
	private Label lastLabel;

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		Composite comp = new Composite(parent, SWT.None);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label l = new Label(comp, SWT.None);
		l.setText("Số cuối: ");
		FontData fontData = l.getFont().getFontData()[0];
		fontData.setHeight(40);
		l.setFont(new Font(parent.getDisplay(), fontData));
		
		lastLabel = new Label(comp, SWT.LEFT);
		lastLabel.setText("199");
		fontData = lastLabel.getFont().getFontData()[0];
		int smallSize = 60;
		fontData.setHeight(smallSize);
		lastLabel.setFont(new Font(parent.getDisplay(), fontData));
		lastLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Color blue = parent.getDisplay().getSystemColor(SWT.COLOR_BLUE);
		lastLabel.setForeground(blue);
		
		currentLabel = new Label(parent, SWT.CENTER);
		currentLabel.setText("99");
		fontData = currentLabel.getFont().getFontData()[0];
		fontData.setHeight(300);
		currentLabel.setFont(new Font(parent.getDisplay(), fontData));
		currentLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		currentLabel.setForeground(blue);
		
		patientLabel = new Label(parent, SWT.CENTER);
		patientLabel.setText("Nguyen Van Hung");
		fontData = patientLabel.getFont().getFontData()[0];
		fontData.setHeight(smallSize);
		patientLabel.setFont(new Font(parent.getDisplay(), fontData));
		patientLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		patientLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_MAGENTA));
		
		Label empty = new Label(parent, SWT.None);
		empty.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		comp = new Composite(parent, SWT.None);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		l = new Label(comp, SWT.None);
		l.setText("Kế tiếp: ");
		fontData = l.getFont().getFontData()[0];
		fontData.setHeight(40);
		l.setFont(new Font(parent.getDisplay(), fontData));
		
		nextsLabel = new Label(comp, SWT.LEFT);
		nextsLabel.setText("99 (Huy), 100 (Dung), 101 (Huong) ...");
		fontData = nextsLabel.getFont().getFontData()[0];
		fontData.setHeight(smallSize);
		nextsLabel.setFont(new Font(parent.getDisplay(), fontData));
		nextsLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nextsLabel.setForeground(blue);
		new Label(parent, SWT.None);
		populate();
		WaitingList.queueListener = new Runnable() {
			
			@Override
			public void run() {
				if (parent.isDisposed()) {
					return;
				}
				parent.getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						populate();
					}
				});
			}
		};
	}
	
	public void populate() {
		lastLabel.setText(String.valueOf(WaitingList.diagQueue.getLastNumber()));
		List<QueueTicket> heads = WaitingList.diagQueue.getHeads();
		String nexts = null;
		boolean first = true;
		for (QueueTicket q : heads) {
			if (first) {
				first = false;
				continue;
			}
			if (nexts == null) {
				nexts = "";
			} else {
				nexts = nexts + ", ";
			}
			nexts = nexts + toText(q);
		}
		if (nexts == null) {
			nexts = "";
		}
		nextsLabel.setText(nexts);
		
		if (heads.isEmpty()) {
			currentLabel.setText("");
			patientLabel.setText("");
		} else {
			QueueTicket current = heads.get(0);
			currentLabel.setText(String.valueOf(WaitingList.getQueueNumber(current)));
			String name = current.patient.name.getValue();
			if (name == null) {
				name = "";
			}
			patientLabel.setText(name);
		}
	}
	
	public static String toText(QueueTicket q) {
		if (q == null) {
			return "";
		}
		Integer i = q.queueNumber.getValue();
		if (i == null) {
			return "";
		}
		String name = q.patient.name.getValue();
		if (name == null) {
			name = "";
		} else {
			String[] a = name.split(" ");
			name = a[a.length - 1];
		}
		return i + " (" + name + ")";
	}

}
