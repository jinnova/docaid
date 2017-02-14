package com.jinnova.docaid;

import java.sql.SQLException;
import java.util.HashMap;

public enum SettingName {
	
	//public static final String QUEUEING_NEXT = "queueing.net";
	queueing_next,
	diag_populateFromLast,
	
	//when set, diag'stage changes from begin to end, not med
	diag_skipMedStage,
	diag_form_inputs_dob,
	diag_form_inputs_address,
	diag_form_inputs_height,
	diag_form_inputs_bodyTemp,
	diag_form_inputs_healthNote,
	diag_form_inputs_treatmentdays,
	daig_form_inputs_medicineTab,
	diag_form_defaulttab,
	
	services_id,
	services_default,
	
	autotext_field_healthnote_enabled,
	
	patient_search_result_field_id,
	patient_search_result_field_name,
	patient_search_result_field_dob,
	patient_search_result_field_lastvisit,
	patient_search_result_field_prevappoint,
	patient_search_result_field_address,
	patient_search_result_field_age,
	patient_search_result_field_weight,
	
	queue_table_field_queuenbr,
	queue_table_field_id,
	queue_table_field_name,
	queue_table_field_dob,
	queue_table_field_lastvisit,
	queue_table_field_prevappoint,
	queue_table_field_address,
	queue_table_field_age,
	queue_table_field_weight;
	
	public static HashMap<String, String> allSettings;

	public static void loadAllSettings() throws SQLException {
		allSettings = DBManager.loadAllSettings();
	}
	
	public static boolean isSet(String name) {
		return isSet(name, false);
	}
	
	public static boolean isSet(String name, boolean defValue) {
		String s = allSettings.get(name);
		if (s == null) {
			return defValue;
		}
		return Boolean.parseBoolean(s);
	}
	
	public static int getInt(String name, int defValue) {
		String s = allSettings.get(name);
		if (s == null) {
			return defValue;
		}
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException ne) {
			return defValue;
		}
	}
}
