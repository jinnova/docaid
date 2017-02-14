package com.jinnova.docaid;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PrescriptionItem extends FieldSet {

	//private int order;
	
	//public Medicine medicine;
	
	public Prescription prescription;
	
	public final FieldString medName = newFieldString();
	
	public String medUnit;
	
	public Integer medUnitPrice;
	
	public String medPackage;
	
	public Integer medPackagePrice;
	
	public Float medPackageSize;
	
	public Boolean medPackageBreakable;
	
	public final FieldInt dayCount = newFieldInt();
	
	public final FieldInt takingCountPerDay = newFieldInt();
	
	//public String takingUnit;
	
	public final FieldFloat amountPerTaking = newFieldFloat();
	
	public Integer amountTotalUnit;
	
	public Integer amountTotalPackage;
	
	public final FieldString note = newFieldString(); //sang/trua/chieu/toi, truoc an, sau an
	
	public final FieldBoolean noteSTC = newFieldBoolean(false);
	public final FieldBoolean noteSC = newFieldBoolean(false);
	public final FieldBoolean noteS = newFieldBoolean(false);
	public final FieldBoolean noteT = newFieldBoolean(false);
	public final FieldBoolean noteC = newFieldBoolean(false);
	public final FieldBoolean noteToi = newFieldBoolean(false);
	public final FieldBoolean noteBeforeLaunch = newFieldBoolean(false);
	public final FieldBoolean noteAfterLaunch = newFieldBoolean(false);
	public final FieldBoolean noteHungry = newFieldBoolean(false);
	public final FieldBoolean noteFull = newFieldBoolean(false);
	
	PrescriptionItem(Prescription pres) {
		this.prescription = pres;
		
		medName.emptyErrorMsg = "Thiếu tên thuốc";
		//medUnit.emptyErrorMsg = "Thiếu đơn vị thuốc";
		//medPackage.emptyErrorMsg = "Thiếu đơn vị đóng gói";
		//medPackageSize.emptyErrorMsg = "Thiếu số lượng mỗi bao bì";
		//dayCount.emptyErrorMsg = "Thiếu số ngày dùng thuốc";
		//takingCountPerDay.emptyErrorMsg = "Thiếu số lần dùng thuốc mỗi ngày";
		//amountPerTaking.emptyErrorMsg = "Thiếu liều lượng mỗi lần";
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.medName.getValue()).
				append(this.medUnit).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PrescriptionItem)) {
			return false;
		}
		PrescriptionItem p = (PrescriptionItem) obj;
		return new EqualsBuilder().append(this.medName.getValue(), p.medName.getValue()).
				append(this.medUnit, p.medUnit).isEquals();
	}
	
	public int computeCost() {
		int c = 0;
		if (amountTotalUnit != null && medUnitPrice != null) {
			c = c + amountTotalUnit * medUnitPrice;
		}
		if (amountTotalPackage != null && medPackagePrice != null) {
			c = c + amountTotalPackage * medPackagePrice;
		}
		return c;
	}
	
	private static boolean isDefined(Integer i) {
		return i != null && i != 0;
	}
	
	PrescriptionItemExtra[] getItemLines() {
		/*if (isUndefined(amountTotalPackage) || isUndefined(amountTotalUnit)) {
			return new Object[] {this};
		} else {
			return new Object[] {this, new PrescriptionItemExtra(this)};
		}*/
		
		boolean byPackage = isDefined(amountTotalPackage);
		boolean byUnit = isDefined(amountTotalUnit);
		if (byPackage && byUnit) {
			return new PrescriptionItemExtra[] {new PrescriptionItemExtra(true, this, 2),
					new PrescriptionItemExtra(false, this, 2)};
		} else if (byPackage) {
			return new PrescriptionItemExtra[] {new PrescriptionItemExtra(true, this, 1)};
		} else {
			//by unit
			return new PrescriptionItemExtra[] {new PrescriptionItemExtra(false, this, 1)};
		}
	}
	
	private static Integer addOne(Integer f) {
		if (f == null) {
			return 1;
		} else {
			return f + 1;
		}
	}
	
	public PrescriptionAmount computeTotalAmount() {
		
		if (/*medUnit == null ||*/ dayCount.getValue() == null || 
				takingCountPerDay.getValue() == null || amountPerTaking.getValue() == null) {
			return new PrescriptionAmount(null, null);
		}
		BigDecimal amountUnitTotal = BigDecimal.valueOf(dayCount.getValue()).
				multiply(BigDecimal.valueOf(takingCountPerDay.getValue())).
				multiply(BigDecimal.valueOf(amountPerTaking.getValue()));
		//amountUnitTotal = amountUnitTotal.round(new MathContext(0, RoundingMode.CEILING));

		//Float f = medPackageSize.getValue();
		float packageSize = medPackageSize != null ? medPackageSize : 1;

		if (packageSize == 1 || medPackage == null || 
				medPackage.trim().isEmpty() || medPackage.equals(medUnit)) {
			//amount by unit only
			BigDecimal d = amountUnitTotal.setScale(0, RoundingMode.CEILING);
			return new PrescriptionAmount(null, d.intValueExact());
		}
		
		PrescriptionAmount amount = convertUnit(amountUnitTotal, packageSize);
		//Integer amountPackage = amount.packageAmount;
		//Float amountUnit = amount.unitAmount;
		if (amount.unitAmount == null || amount.unitAmount == 0) { 
			return new PrescriptionAmount(amount.packageAmount, null);
		}

		boolean breakable = Medicine.isBreakable(medPackageBreakable);
		if (!breakable) {
			//amount by package only
			return new PrescriptionAmount(addOne(amount.packageAmount), null);
		} else if (medUnitPrice != null && medPackagePrice != null && amount.unitAmount * medUnitPrice >= medPackagePrice) {
			return new PrescriptionAmount(addOne(amount.packageAmount), 0);
		} else {
			return amount;
		}
	}
	
	private static PrescriptionAmount convertUnit(BigDecimal totalUnit, float packageSize) {
		if (packageSize == 0) {
			return new PrescriptionAmount(1, null);
		}
		BigDecimal totalPackageCal = totalUnit.divide(BigDecimal.valueOf(packageSize), RoundingMode.DOWN);
		PrescriptionAmount amount = new PrescriptionAmount(null, null);
		amount.packageAmount = totalPackageCal.intValue();
		BigDecimal d = totalUnit.subtract(BigDecimal.valueOf(amount.packageAmount).multiply(BigDecimal.valueOf(packageSize)));
		d = d.setScale(0, RoundingMode.CEILING);
		amount.unitAmount = d.intValueExact();
		return amount;
	}
	
	/*public String getTotalAmountReading() {
		
		Float totalUnit = amountTotalUnit.getValue();
		Float totalPackage = amountTotalPackage.getValue();
		if (totalUnit != null || totalPackage != null) {
			String s = null;
			if (totalUnit != null) {
				s = totalUnit + " " + medUnit.getValue();
			}
			if (totalPackage != null) {
				if (s == null) {
					return totalPackage + " " + medPackage.getValue();
				}
				return s + ", " + totalPackage + " " + medPackage.getValue();
			}
			return s;
		}
		
		if (medUnit.getValue() == null || medPackage.getValue() == null ||
				dayCount.getValue() == null || takingCountPerDay.getValue() == null ||
				amountPerTaking.getValue() == null || medPackageSize.getValue() == null ||
				medPackageBreakable.getValue() == null) {
			return "";
		}
		BigDecimal totalUnitCal = BigDecimal.valueOf(dayCount.getValue()).
				multiply(BigDecimal.valueOf(takingCountPerDay.getValue())).
				multiply(BigDecimal.valueOf(amountPerTaking.getValue()));
		BigDecimal totalPackageCal = totalUnitCal.divide(BigDecimal.valueOf(medPackageSize.getValue()));
		if (medUnit.getValue().equalsIgnoreCase(medPackage.getValue()) || medPackageBreakable.getValue()) {
			return totalUnitCal.floatValue() + " " + medUnit.getValue();
		} else {
			return totalPackageCal.round(new MathContext(0, RoundingMode.UP)).intValue() + 
					" " + medPackage.getValue();
		}
	}*/
	
	/*public float getTotalAmountBySmallestUnit() {
		return 0;
	}
	
	public String getSmallestUnit() {
		return null;
	}*/
	
	private static String append(FieldBoolean f, String dest, String s) {
		if (f.getValue() == null || !f.getValue()) {
			return dest;
		}
		
		if ("".equals(dest)) {
			return s;
		} else {
			return dest + ", " + s;
		}
	}
	
	public String getNoteReading() {
		String s = this.note.getValueAsEditing();
		if (s == null) {
			s = "";
		} 
		s = append(noteAfterLaunch, s, "sau khi ăn");
		s = append(noteBeforeLaunch, s, "trước khi ăn");
		s = append(noteC, s, "chiều");
		s = append(noteFull, s, "khi no");
		s = append(noteHungry, s, "khi đói");
		s = append(noteS, s, "sáng");
		s = append(noteSC, s, "sáng, chiều");
		s = append(noteSTC, s, "sáng, trưa, chiều");
		s = append(noteT, s, "trưa");
		s = append(noteToi, s, "tối");
		return s;
	}

	public JsonObject generateJson() {
		JsonObject json = new JsonObject();
		json.addProperty("amountPerTaking", this.amountPerTaking.getValue());
		json.addProperty("dayCount", this.dayCount.getValue());
		json.addProperty("amountTotalPackage", this.amountTotalPackage);
		json.addProperty("amountTotalUnit", this.amountTotalUnit);
		json.addProperty("medName", this.medName.getValue());
		json.addProperty("medPackageBreakable", this.medPackageBreakable);
		json.addProperty("medPackageSize", this.medPackageSize);
		json.addProperty("medPackage", this.medPackage);
		json.addProperty("medUnit", this.medUnit);
		json.addProperty("medPackagePrice", this.medPackagePrice);
		json.addProperty("medUnitPrice", this.medUnitPrice);
		json.addProperty("note", this.note.getValue());
		json.addProperty("noteAfterLaunch", this.noteAfterLaunch.getValue());
		json.addProperty("noteBeforeLaunch", this.noteBeforeLaunch.getValue());
		json.addProperty("noteC", this.noteC.getValue());
		json.addProperty("noteFull", this.noteFull.getValue());
		json.addProperty("noteHungry", this.noteHungry.getValue());
		json.addProperty("noteS", this.noteS.getValue());
		json.addProperty("noteSC", this.noteSC.getValue());
		json.addProperty("noteSTC", this.noteSTC.getValue());
		json.addProperty("noteT", this.noteT.getValue());
		json.addProperty("noteToi", this.noteToi.getValue());
		json.addProperty("takingCountPerDay", this.takingCountPerDay.getValue());
		return json;
	}

	public void loadJson(JsonObject json) {
		load(json, "amountPerTaking", this.amountPerTaking);
		load(json, "dayCount", this.dayCount);
		this.amountTotalPackage = getAsIntNull(json, "amountTotalPackage");
		this.amountTotalUnit = getAsIntNull(json, "amountTotalUnit");
		load(json, "medName", this.medName);
		this.medPackageBreakable = getAsBooleanNull(json, "medPackageBreakable");
		this.medPackageSize = getAsFloatNull(json, "medPackageSize");
		this.medPackage = getAsStringNull(json, "medPackage");
		this.medUnit = getAsStringNull(json, "medUnit");
		this.medUnitPrice = getAsIntNull(json, "medUnitPrice");
		this.medPackagePrice = getAsIntNull(json, "medPackagePrice");
		load(json, "note", this.note);
		load(json, "noteAfterLaunch", this.noteAfterLaunch);
		load(json, "noteBeforeLaunch", this.noteBeforeLaunch);
		load(json, "noteC", this.noteC);
		load(json, "noteFull", this.noteFull);
		load(json, "noteHungry", this.noteHungry);
		load(json, "noteS", this.noteS);
		load(json, "noteSC", this.noteSC);
		load(json, "noteSTC", this.noteSTC);
		load(json, "noteT", this.noteT);
		load(json, "noteToi", this.noteToi);
		load(json, "takingCountPerDay", this.takingCountPerDay);
	}
	
	private static void load(JsonObject json, String propName, FieldString f) {
		f.loadValue(getAsStringNull(json.get(propName)));
	}
	
	private static String getAsStringNull(JsonObject o, String propName) {
		if (o == null || o.isJsonNull()) {
			return null;
		}
		return getAsStringNull(o.get(propName));
	}
	
	private static String getAsStringNull(JsonElement e) {
		if (e == null || e.isJsonNull()) {
			return null;
		}
		return e.getAsString();
	}
	
	private static void load(JsonObject json, String propName, FieldFloat f) {
		f.loadValue(getAsFloatNull(json.get(propName)));
	}
	
	private static Float getAsFloatNull(JsonObject o, String propName) {
		if (o == null || o.isJsonNull()) {
			return null;
		}
		return getAsFloatNull(o.get(propName));
	}
	
	private static Float getAsFloatNull(JsonElement e) {
		if (e == null || e.isJsonNull()) {
			return null;
		}
		return e.getAsFloat();
	}
	
	private static void load(JsonObject json, String propName, FieldInt f) {
		f.loadValue(getAsIntNull(json.get(propName)));
	}
	
	private static Integer getAsIntNull(JsonObject o, String propName) {
		if (o == null || o.isJsonNull()) {
			return null;
		}
		return getAsIntNull(o.get(propName));
	}
	
	private static Integer getAsIntNull(JsonElement e) {
		if (e == null || e.isJsonNull()) {
			return null;
		}
		return e.getAsInt();
	}
	
	private static void load(JsonObject json, String propName, FieldBoolean f) {
		f.loadValue(getAsBooleanNull(json.get(propName)));
	}
	
	private static Boolean getAsBooleanNull(JsonObject o, String propName) {
		if (o == null || o.isJsonNull()) {
			return null;
		}
		return getAsBooleanNull(o.get(propName));
	}
	
	private static Boolean getAsBooleanNull(JsonElement e) {
		if (e == null || e.isJsonNull()) {
			return null;
		}
		return e.getAsBoolean();
	}
	
	/*private static int testCount = 0;
	
	private static void assertEqual(int v, int exp) {
		if (v == exp) {
			System.out.println(++testCount + ": Got " + v + ", expected " + exp + ": OK");
		} else { 
		    //throw new RuntimeException("Got " + v + ", expected " + exp);
			System.out.println(++testCount + ": Got " + v + ", expected " + exp + ": Failed");
		}
	}
	
	public static void main(String[] args) {
		Medicine m = new Medicine();
		m.packageAmount = 1;
		PrescriptionItem p = new PrescriptionItem();
		p.medicine = m;
		p.dayCount = 1;
		p.takingCountPerDay = 1;
		p.amountPerTaking = 1;
		assertEqual(p.getTotalAmount(), 1);
		
		p.dayCount = 10;
		p.takingCountPerDay = 1;
		p.amountPerTaking = 1;
		assertEqual(p.getTotalAmount(), 10);
		
		p.dayCount = 1;
		p.takingCountPerDay = 10;
		p.amountPerTaking = 1;
		assertEqual(p.getTotalAmount(), 10);
		
		p.dayCount = 1;
		p.takingCountPerDay = 1;
		p.amountPerTaking = 10;
		assertEqual(p.getTotalAmount(), 10);
		
		p.dayCount = 5;
		p.takingCountPerDay = 3;
		p.amountPerTaking = 2;
		assertEqual(p.getTotalAmount(), 30);
		
		p.dayCount = 3;
		p.takingCountPerDay = 2;
		p.amountPerTaking = 0.5f;
		assertEqual(p.getTotalAmount(), 3);
		
		p.dayCount = 5;
		p.takingCountPerDay = 3;
		p.amountPerTaking = 0.5f;
		assertEqual(p.getTotalAmount(), 8);
		
		m.packageAmount = 10;
		p.dayCount = 5;
		p.takingCountPerDay = 3;
		p.amountPerTaking = 1;
		assertEqual(p.getTotalAmount(), 1);
		
		p.dayCount = 5;
		p.takingCountPerDay = 3;
		p.amountPerTaking = 1;
		assertEqual(p.getTotalAmount(), 2);
	}*/
}