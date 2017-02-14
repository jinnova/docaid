package com.jinnova.docaid;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class FieldTimestamp extends Field<Timestamp, String> {
	
	private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	
	private final long today;
	
	FieldTimestamp() {
		super("");
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		today = cal.getTimeInMillis();
	}

	@Override
	String trim(String v) {
		if (v == null) {
			return null;
		}
		return v.trim();
	}

	@Override
	String asEditing(Timestamp v) {
		if (v == null) {
			return "";
		}
		return df.format(v);
	}

	@Override
	Timestamp parse(String v) throws ParseException {
		if (v == null || "".equals(v.trim())) {
			return null;
		}
		//try {
			return new Timestamp(df.parse(v).getTime());
		//} catch (ParseException pe) {
		//	throw new ParseException(v + ": Không phải ngày/tháng/năm", 0);
		//}
	}
	
	@Override
	public boolean isModified() {
		
		Timestamp v = getValue();
		if (backupValue == null) {
			return v != null;
		} else {
			if (v == null) {
				return true;
			}
			return !df.format(backupValue).equals(df.format(v));
		}
	}
	
	private String easyLabel;
	private Date easyValue;
	
	public String getEasyLabel() {
		
		Timestamp date = this.getValue();
		if (date == null) {
			return "";
		}
		if (!date.equals(easyValue) || easyLabel == null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			long diff = today - cal.getTimeInMillis();
			long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
			String ddmmyyyy = df.format(date);
			String s;
			if (days == 0) {
				s = "Hôm nay";
			} else if (days < 8) {
				if (days > 0) {
					s = days + " ngày trước (" + ddmmyyyy + ")";
				} else {
					//<0
					s = Math.abs(days) + " ngày sau (" + ddmmyyyy + ")";
				}
			} else if (days <= 31) {
				s = ddmmyyyy + " (" + days + " ngày trước)";
			} else {
				s = ddmmyyyy;
			}
			easyLabel = s;
		}
		return easyLabel;
	}

}
