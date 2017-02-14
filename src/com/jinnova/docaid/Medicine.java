package com.jinnova.docaid;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Medicine extends FieldSet {

	public FieldString name = newFieldString("Thiếu tên thuốc");
	
	public FieldString keywords = newFieldString();
	
	public FieldString unit = newFieldString("Thiếu đơn vị tính");
	
	public FieldInt unitPrice = newFieldInt();
	
	//public FieldString packageUnit = newFieldString("Thiếu đơn vị bao bì");
	public FieldString packageUnit = newFieldString();
	
	public FieldInt packageUnitPrice = newFieldInt();
	
	public FieldFloat packageSize = newFieldFloat();
	
	public FieldBoolean packageBreakable = newFieldBoolean();
	
	public Medicine() {
		//packageSize.emptyErrorMsg = "Thiếu số lượng trong mỗi bao bì";
		unitPrice.maskZero = true;
		packageUnitPrice.maskZero = true;
	}
	
	public boolean isTwoUnit() {
		/*return unit.getValue() != null && packageUnit.getValue() != null &&
				!unit.getValue().equals(packageUnit.getValue());*/
		return isTwoUnit(unit.getValue(), packageUnit.getValue());
	}
	
	public static boolean isTwoUnit(String medUnit, String medPackage) {
		return !isEmpty(medUnit) && !isEmpty(medPackage) &&
				!medUnit.equals(medPackage)/* &&
				packageBreakable.getValue() != null &&
				packageBreakable.getValue()*/;
	}
	
	private static boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}
	
	public boolean isBreakable() {
		Boolean b = packageBreakable.getValue();
		return isBreakable(b);
	}
	
	public static boolean isBreakable(Boolean b) {
		return b != null && b;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.name.getValue()).append(unit.getValue()).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Medicine)) {
			return false;
		}
		Medicine m = (Medicine) obj;
		return new EqualsBuilder().append(this.name.getValue(), m.name.getValue()).
				append(this.unit.getValue(), m.unit.getValue()).isEquals();
	}
}
