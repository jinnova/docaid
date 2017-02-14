package com.jinnova.docaid;

public class PrescriptionItemExtra {

	public final boolean amountPackage;
	public final PrescriptionItem item;
	public final int lineCount;
	
	public PrescriptionItemExtra(boolean amountPackage, PrescriptionItem pi, int lineCount) {
		this.amountPackage = amountPackage;
		this.item = pi;
		this.lineCount = lineCount;
	}
}
