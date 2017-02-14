package com.jinnova.docaid;

public class PrescriptionAmount {
	
	public Integer packageAmount;
	public Integer unitAmount;
	
	PrescriptionAmount(Integer packageAmount, Integer unitAmount) {
		this.packageAmount = packageAmount;
		this.unitAmount = unitAmount;
	}

	public String getPackageAmount() {
		if (packageAmount == null) {
			return "";
		}
		return String.valueOf(packageAmount);
	}
	
	public String getUnitAmount() {
		if (unitAmount == null) {
			return "";
		}
		return String.valueOf(unitAmount);
	}
}
