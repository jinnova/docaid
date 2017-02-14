package com.jinnova.docaid;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/*
 * Empty string is for null
 * */
public class FieldDate extends Field<Date, String> {
	
	private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	
	private final long today;
	
	private final int todayDay;
	
	private final int todayMonth;
	
	FieldDate() {
		super("");
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		today = cal.getTimeInMillis();
		todayDay = cal.get(Calendar.DAY_OF_MONTH);
		todayMonth = cal.get(Calendar.MONTH);
	}

	@Override
	String trim(String v) {
		if (v == null) {
			return null;
		}
		return v.trim();
	}

	@Override
	String asEditing(Date v) {
		if (v == null) {
			return "";
		}
		return df.format(v);
	}

	@Override
	Date parse(String v) throws ParseException {
		if (v == null || "".equals(v.trim())) {
			return null;
		}
		try {
			return new Date(df.parse(v).getTime());
		} catch (ParseException pe) {
			throw new ParseException(v + ": Không phải ngày/tháng/năm", 0);
		}
	}
	
	@Override
	public boolean isModified() {
		
		Date v = getValue();
		if (backupValue == null) {
			return v != null;
		} else {
			if (v == null) {
				return true;
			}
			return !df.format(backupValue).equals(df.format(v));
		}
	}
	
	public Integer getDaysFromNow() {
		
		Date date = this.getValue();
		if (date == null) {
			return null;
		}
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long diff = today - cal.getTimeInMillis();
		return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}
	
	private String easyLabel;
	private Date easyValue;
	
	public String getEasyLabel() {
		
		Date date = this.getValue();
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
	
	private String easyLabel2;
	private Date easyValue2;
	
	public String getEasyLabel2() {
		
		Date date = this.getValue();
		if (date == null) {
			return "";
		}
		if (!date.equals(easyValue2) || easyLabel2 == null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			long diff = today - cal.getTimeInMillis();
			long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
			String s;
			if (days == 0) {
				s = "Hôm nay";
			} else if (days < 31) {
				if (days > 0) {
					s = days + " ngày trước";
				} else {
					s = Math.abs(days) + " ngày sau";
				}
			} else {
				s = "";
			}
			easyLabel2 = s;
		}
		return easyLabel2;
	}
	
	private Boolean todayPreviousYear;
	private Date todayPreviousYearValue;
	
	public boolean isTodayPreviousYear() {
		
		Date date = this.getValue();
		if (date == null) {
			return false;
		}
		if (!date.equals(todayPreviousYearValue) || todayPreviousYearValue == null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			todayPreviousYear = cal.get(Calendar.MONTH) == todayMonth && cal.get(Calendar.DAY_OF_MONTH) == todayDay;
		}
		return todayPreviousYear;
		
	}

	public String getAgeReading() {
		//Date date = dob.getValue();
		Date date = this.getValue();
		if (date == null) {
			return "";
		}
		LocalDate dobDate = new LocalDate(date);
		LocalDate now = new LocalDate();
		PeriodType pt = PeriodType.years();
		Period diff = new Period(dobDate, now, pt);
		int years = diff.getYears();
		if (years > 150) {
			return "";
		}
		if (years >= 4) {
			return years + " tuổi";
		}
		
		pt = PeriodType.months();
		diff = new Period(dobDate, now, pt);
		return diff.getMonths() + " tháng";
	}
}