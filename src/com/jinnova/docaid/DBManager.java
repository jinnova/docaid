package com.jinnova.docaid;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class DBManager {
	
	private interface ResultSetLoader {
		
		abstract void load(ResultSet rs) throws SQLException;
		
		void setParams(PreparedStatement stmt) throws SQLException;
	}
	
	private interface ResultSetLoader2 {
		
		abstract void load(ResultSet rs) throws SQLException;
	}
	
	private interface PreparedUpdateHandler {
		
		void setParams(PreparedStatement stmt) throws SQLException;
	}
	
	private static void executeQuery(String sql, ResultSetLoader2 loader) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			System.out.println(sql);
			stmt = Activator.dbcon.createStatement();
			rs = stmt.executeQuery(sql);
			loader.load(rs);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}
	
	private static void prepareQuery(String sql, ResultSetLoader loader) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			System.out.println(sql);
			stmt = Activator.dbcon.prepareStatement(sql);
			loader.setParams(stmt);
			rs = stmt.executeQuery();
			loader.load(rs);
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}
	
	private static int prepareUpdate(String sql, PreparedUpdateHandler handler) throws SQLException {
		PreparedStatement stmt = null;
		try {
			System.out.println(sql);
			stmt = Activator.dbcon.prepareStatement(sql);
			handler.setParams(stmt);
			return stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public static LinkedList<QueueTicket> loadQueue() throws SQLException {
		
		//String sql = "select * from waiting_queue order by ord_number";
		String sql = "select * from waiting_queue";
		LinkedList<QueueTicket> list = new LinkedList<>();
		prepareQuery(sql, new ResultSetLoader() {
			
			@Override
			public void load(ResultSet rs) throws SQLException {
				while (rs.next()) {
					QueueTicket q = loadQueueItem(rs);
					list.add(q);
				}
			}

			@Override
			public void setParams(PreparedStatement stmt) {
				
			}
		});
		return list;
	}

	public static QueueTicket loadDiagQueueNoException(int patientId) {
		try {
			return loadDiagQueue(patientId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static QueueTicket loadDiagQueue(int patientId) throws SQLException {
		
		String sql = "select * from waiting_queue where patient_id=?";
		QueueTicket[] loadedItem = new QueueTicket[1];
		prepareQuery(sql, new ResultSetLoader() {
			
			@Override
			public void load(ResultSet rs) throws SQLException {
				while (rs.next()) {
					loadedItem[0] = loadQueueItem(rs);
				}
			}

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, patientId);
			}
		});
		return loadedItem[0];
	}
	
	private static QueueTicket loadQueueItem(ResultSet rs) throws SQLException {
		//Patient p = new Patient();
		//p.id.loadValue(rs.getInt("patient_id"));
		/*p.setLastSymptoms(rs.getString("last_symptoms"));
		p.setLastDiagbrief(rs.getString("last_diagbrief"));
		p.setLastTreatments(rs.getString("last_treatments"));*/
		Patient p = loadPatientInfo(rs.getInt("patient_id"), rs, false);
		//DiagRecord diag = DiagRecord.load(p);
		//p.createDiag();
		DiagRecord diag = p.todayDiag;
		//diag.weight.loadValue(rs.getFloat("weight"));
		//diag.height.loadValue(rs.getInt("height"));
		diag.age.loadValue(rs.getString("age"));
		diag.loadExtras(rs.getString("extras"));
		diag.symptons.loadValue(rs.getString("symptoms"));
		diag.diagnosis.loadValue(rs.getString("diag_brief"));
		diag.date.loadValue(rs.getTimestamp("diag_date"));
		diag.noRevisitIfGood.loadValue(rs.getBoolean("norevisit_ifgood"));
		diag.revisitDays.loadValue(rs.getInt("revisit_days"));
		diag.treatmentDays.loadValue(rs.getInt("treatment_days"));
		diag.treatment.loadValue(rs.getString("treatments"));
		diag.id.loadValue(rs.getInt("diag_id"));
		diag.prescription.loadJson(rs.getString("prescription"));
		QueueTicket q = new QueueTicket(rs.getInt("ord_number"), p);
		q.previousAppointment.loadValue(rs.getDate("last_appointment"));
		q.previousVisit.loadValue(rs.getDate("last_visit"));
		q.stage = QueueStage.valueOf(rs.getString("queue_stage"));
		//diag.ticket = q;
		return q;
	}

	public static LinkedList<DiagRecord> loadDiagRecords(Patient p) throws SQLException {
		
		String sql = "select * from diagnosis where patient_id=" + p.id.getValue() + " order by diag_date";
		LinkedList<DiagRecord> list = new LinkedList<>();
		prepareQuery(sql, new ResultSetLoader() {
			
			@Override
			public void load(ResultSet rs) throws SQLException {
				while (rs.next()) {
					//DiagRecord r = DiagRecord.load(p);
					DiagRecord r = p.createHistoricalDiag();
					r.date.loadValue(rs.getTimestamp("diag_date"));
					//r.weight.loadValue(rs.getFloat("weight"));
					//r.height.loadValue(rs.getInt("height"));
					r.age.loadValue(rs.getString("age"));
					r.loadExtras(rs.getString("extras"));
					r.id.loadValue(rs.getInt("id"));
					r.noRevisitIfGood.loadValue(rs.getBoolean("norevisit_ifgood"));
					r.revisitDays.loadValue(rs.getInt("revisit_days"));
					r.symptons.loadValue(rs.getString("symptoms"));
					r.diagnosis.loadValue(rs.getString("diag_brief"));
					r.treatment.loadValue(rs.getString("treatments"));
					r.treatmentDays.loadValue(rs.getInt("treatment_days"));
					r.prescription.loadJson(rs.getString("prescription"));
					//r.inDay = false;
					list.add(r);
				}
			}

			@Override
			public void setParams(PreparedStatement stmt) {
				
			}
		});
		return list;
	}

	public static String loadSetting(String name) throws SQLException {
		String[] value = new String[1];
		prepareQuery("select value from app_settings where name=?", new ResultSetLoader() {
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, name);
			}
			
			@Override
			public void load(ResultSet rs) throws SQLException {
				if (rs.next()) {
					value[0] = rs.getString("value");
				} else {
					value[0] = null;
				}
			}
		});
		return value[0];
	}

	/*public static HashMap<String, String> getSettingByPrefix(String prefix) throws SQLException {
		
		HashMap<String, String> map = new HashMap<String, String>();
		prepareQuery("select * from app_settings where name like ?", new ResultSetLoader() {
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, prefix + "%");
			}
			
			@Override
			public void load(ResultSet rs) throws SQLException {
				while (rs.next()) {
					map.put(rs.getString("name"), rs.getString("value"));
				}
			}
		});
		return map;
	}*/

	public static HashMap<String, String> loadAllSettings() throws SQLException {
		
		HashMap<String, String> map = new HashMap<String, String>();
		prepareQuery("select * from app_settings", new ResultSetLoader() {
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				//stmt.setString(1, prefix + "%");
			}
			
			@Override
			public void load(ResultSet rs) throws SQLException {
				while (rs.next()) {
					map.put(rs.getString("name"), rs.getString("value"));
				}
			}
		});
		return map;
	}
	
	public static int updateSetting(String name, int value) throws SQLException {
		return updateSetting(name, String.valueOf(value));
	}
	
	public static int updateSetting(String name, float value) throws SQLException {
		return updateSetting(name, String.valueOf(value));
	}
	
	public static int updateSetting(String name, String value) throws SQLException {
		int count = prepareUpdate("update app_settings set value=? where name=?", new PreparedUpdateHandler() {
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, value);
				stmt.setString(2, name);
			}
		});
		if (count > 0) {
			return count;
		}
		return prepareUpdate("insert app_settings (name, value) values (?, ?)", new PreparedUpdateHandler() {
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, name);
				stmt.setString(2, value);
			}
		});
	}
	
	private static float maskNull(Float f) {
		if (f == null) {
			return 0;
		}
		return f;
	}
	
	private static int maskNull(Integer i) {
		if ( i== null) {
			return 0;
		}
		return i;
	}
	
	private static int maskNull(Integer i, int defValue) {
		if ( i== null) {
			return defValue;
		}
		return i;
	}
	
	private static boolean maskNull(Boolean value, boolean defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	/*public static void updatePatient(Patient p) {
		try {
			updatePatientInternal(p);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}*/
	
	private static int setPatientValues(Patient p, PreparedStatement stmt, int i) throws SQLException {
		stmt.setString(i++, p.name.getValue());
		stmt.setDate(i++, p.dob.getValue());
		stmt.setString(i++, p.age);
		//stmt.setFloat(i++, maskNull(p.weight));
		stmt.setString(i++, p.weight);
		stmt.setString(i++, p.address.getValue());
		stmt.setString(i++, p.phone.getValue());
		stmt.setString(i++, p.getExtras());
		//stmt.setFloat(i++, maskNull(p.weight.getValue()));
		//stmt.setInt(i++, maskNull(p.height.getValue()));
		stmt.setString(i++, p.healthNote.getValue());
		return i;
	}

	public static int insertPatient(Patient p) throws SQLException {
		String sql = "insert into patient "
				+ "(name, dob, age, weight, address, phone, extras, health_note) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?)";
		System.out.println(sql);
		
		PreparedStatement stmt = null;
		ResultSet keys = null;
		try {
			stmt = Activator.dbcon.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			int i = 1;
			setPatientValues(p, stmt, i);
			stmt.executeUpdate();
			keys = stmt.getGeneratedKeys();
			keys.next();
			return keys.getInt(1);
		} finally {
			if (keys != null) {
				keys.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public static void updatePatient(Patient p) throws SQLException {
		String sql = "update patient set "
				+ "name=?, dob=?, age=?, weight=?, address=?, phone=?, extras=?, health_note=?, "
				+ "last_symptoms=?, last_diagbrief=?, last_treatments=?, "
				+ "last_visit=?, last_appointment=?, updated_time=? where id=?";
		System.out.println(sql);
		
		PreparedStatement stmt = null;
		try {
			stmt = Activator.dbcon.prepareStatement(sql);
			int i = 1;
			i = setPatientValues(p, stmt, i);
			stmt.setString(i++, p.getLastSymptoms());
			stmt.setString(i++, p.getLastDiagbrief());
			stmt.setString(i++, p.getLastTreatments());
			stmt.setDate(i++, p.lastVisit.getValue());
			stmt.setDate(i++, p.previousAppointment.getValue());
			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
			stmt.setInt(i++, p.id.getValue());
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public static void updatePatientFamily(int id, String extras) throws SQLException {
		String sql = "update patient set family=? where id=?";
		prepareUpdate(sql, new PreparedUpdateHandler() {
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, extras);
				stmt.setInt(2, id);
			}
		});
	}

	public static void updateQueueFamily(int id, String extras) throws SQLException {
		String sql = "update waiting_queue set family=? where patient_id=?";
		prepareUpdate(sql, new PreparedUpdateHandler() {
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, extras);
				stmt.setInt(2, id);
			}
		});
	}

	public static Integer insertDiag(DiagRecord diag) throws SQLException {
		String sql = "insert into diagnosis (patient_id, diag_date, age, extras, "
				+ "symptoms, diag_brief, treatments, treatment_days, revisit_days, norevisit_ifgood, prescription, updated_time) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		System.out.println(sql);
		PreparedStatement stmt = null;
		ResultSet keys = null;
		try {
			stmt = Activator.dbcon.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			setDiagValues(stmt, diag, 1);
			stmt.executeUpdate();
			keys = stmt.getGeneratedKeys();
			if (keys.next()) {
				return keys.getInt(1);
			} else {
				return null;
			}
		} finally {
			if (keys != null) {
				keys.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public static void updateDiag(DiagRecord diag) throws SQLException {
		String sql = "update diagnosis set patient_id=?, diag_date=?, age=?, extras=?, "
				+ "symptoms=?, diag_brief=?, treatments=?, treatment_days=?, revisit_days=?, norevisit_ifgood=?, "
				+ "prescription=?, updated_time=? where id=?";
		System.out.println(sql);
		PreparedStatement stmt = null;
		ResultSet keys = null;
		try {
			stmt = Activator.dbcon.prepareStatement(sql);
			int i = 1;
			i = setDiagValues(stmt, diag, i);
			stmt.setInt(i++, diag.id.getValue());
			stmt.executeUpdate();
		} finally {
			if (keys != null) {
				keys.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}
	
	private static int setDiagValues(PreparedStatement stmt, DiagRecord diag, int i) throws SQLException {
		stmt.setInt(i++, diag.patient.id.getValue());
		stmt.setTimestamp(i++, diag.date.getBackupValue()); //use backup to get precise timestamp
		//stmt.setFloat(i++, maskNull(diag.weight.getValue()));
		//stmt.setInt(i++, maskNull(diag.height.getValue()));
		stmt.setString(i++, diag.age.getValue());
		stmt.setString(i++, diag.getExtras());
		stmt.setString(i++, diag.symptons.getValue());
		stmt.setString(i++, diag.diagnosis.getValue());
		stmt.setString(i++, diag.treatment.getValue());
		stmt.setInt(i++, maskNull(diag.treatmentDays.getValue()));
		stmt.setInt(i++, maskNull(diag.revisitDays.getValue()));
		stmt.setBoolean(i++, maskNull(diag.noRevisitIfGood.getValue(), true));
		stmt.setString(i++, diag.prescription.generateJson());
		stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));
		return i;
	}
	
	public static void insertWaitingList(QueueTicket p) throws SQLException {
		String sql = "insert into waiting_queue ("
				+ "patient_id, ord_number, name, dob, address, phone, health_note, family, "
				+ "age, extras, last_visit, last_appointment, "
				+ "diag_id, diag_date, symptoms, diag_brief, treatments, treatment_days, revisit_days, "
				+ "norevisit_ifgood, prescription, queue_stage) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		System.out.println(sql);
		PreparedStatement stmt = null;
		try {
			stmt = Activator.dbcon.prepareStatement(sql);
			int i = 1;
			stmt.setInt(i++, p.patient.id.getValue());
			setQueueEntryInfo(p, stmt, i);
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public static int updateWaitingList(QueueTicket p) throws SQLException {
		String sql = "update waiting_queue set "
				+ "ord_number=?, name=?, dob=?, address=?, phone=?, health_note=?, family=?, "
				+ "age=?, extras=?, last_visit=?, last_appointment=?, "
				+ "diag_id=?, diag_date=?, symptoms=?, diag_brief=?, treatments=?, treatment_days=?, "
				+ "revisit_days=?, norevisit_ifgood=?, prescription=?, queue_stage=? where patient_id=?";
		System.out.println(sql);

		PreparedStatement stmt = null;
		try {
			stmt = Activator.dbcon.prepareStatement(sql);
			int i = 1;
			i = setQueueEntryInfo(p, stmt, i);
			stmt.setInt(i++, p.patient.id.getValue());
			return stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public static int updateWaitingStage(int patientId, String stage) throws SQLException {
		String sql = "update waiting_queue set queue_stage='" + stage + "' where patient_id=" + patientId;
		System.out.println(sql);

		Statement stmt = null;
		try {
			stmt = Activator.dbcon.createStatement();
			return stmt.executeUpdate(sql);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}
	
	private static int setQueueEntryInfo(QueueTicket p, PreparedStatement stmt, int i) throws SQLException {

		stmt.setInt(i++, p.queueNumber.getValue());
		stmt.setString(i++, p.patient.name.getValue());
		stmt.setDate(i++, p.patient.dob.getValue());
		stmt.setString(i++, p.patient.address.getValue());
		stmt.setString(i++, p.patient.phone.getValue());
		stmt.setString(i++, p.patient.healthNote.getValue());
		stmt.setString(i++, p.patient.buildFamilyJson());
		
		stmt.setString(i++, p.patient.todayDiag.age.getValue());
		//stmt.setFloat(i++, maskNull(p.diag.weight.getValue()));
		stmt.setString(i++, p.patient.todayDiag.getExtras());
		//stmt.setFloat(i++, maskNull(p.diag.weight.getValue()));
		//stmt.setInt(i++, maskNull(p.diag.height.getValue()));
		//stmt.setDate(i++, p.diag.patient.lastVisit.getValue());
		//stmt.setDate(i++, p.diag.patient.previousAppointment.getValue());
		stmt.setDate(i++, p.previousVisit.getValue());
		stmt.setDate(i++, p.previousAppointment.getValue());
		stmt.setInt(i++, maskNull(p.patient.todayDiag.id.getValue(), -1));
		stmt.setTimestamp(i++, p.patient.todayDiag.date.getValue());
		stmt.setString(i++, p.patient.todayDiag.symptons.getValue());
		stmt.setString(i++, p.patient.todayDiag.diagnosis.getValue());
		stmt.setString(i++, p.patient.todayDiag.treatment.getValue());
		stmt.setInt(i++, maskNull(p.patient.todayDiag.treatmentDays.getValue()));
		stmt.setInt(i++, maskNull(p.patient.todayDiag.revisitDays.getValue()));
		stmt.setBoolean(i++, maskNull(p.patient.todayDiag.noRevisitIfGood.getValue(), true));
		stmt.setString(i++, p.patient.todayDiag.prescription.generateJson());
		stmt.setString(i++, p.stage.name());
		return i;
	}
	
	public static LinkedList<Patient> lastVisits() throws SQLException {
		//return search("select * from patient order by last_visit desc limit 100", null);
		//return search("select * from patient order by updated_time desc limit 100", null);
		LinkedList<Patient> list = new LinkedList<>();
		executeQuery("select * from patient order by updated_time desc limit 1000", new ResultSetLoader2() {
			
			@Override
			public void load(ResultSet rs) throws SQLException {

				while (rs.next()) {
					//Patient p = new Patient();
					//p.id.loadValue(rs.getInt("id"));
					Patient p = loadPatientInfo(rs.getInt("id"), rs, true);
					//p.createDiag();
					list.add(p);
				}
			}
		});
		return list;
	}
	
	public static Patient loadPatient(int id) throws SQLException {
		//return search("select * from patient where id = " + id, null);
		Patient[] patient = new Patient[1];
		executeQuery("select * from patient where id = " + id, new ResultSetLoader2() {
			
			@Override
			public void load(ResultSet rs) throws SQLException {

				if (rs.next()) {
					//Patient p = new Patient();
					//p.id.loadValue(rs.getInt("id"));
					Patient p = loadPatientInfo(rs.getInt("id"), rs, true);
					//p.createDiag();
					patient[0] = p;
				}
			}
		});
		return patient[0];
	}
	
	public static LinkedList<Patient> searchName(String name) throws SQLException {
		//return search("select * from patient where match(name) against (?) order by name", new String[] {name});
		String sql = "select * from patient where match(name) against (? in boolean mode) order by name";
		LinkedList<Patient> list = new LinkedList<>();
		prepareQuery(sql, new ResultSetLoader() {
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				String param = null;
				for (String one : name.split(" ")) {
					if (param == null) {
						param = "+" + one;
					} else {
						param = param + " +" + one;
					}
				}
				stmt.setString(1, param);
			}
			
			@Override
			public void load(ResultSet rs) throws SQLException {
				while (rs.next()) {
					//Patient p = new Patient();
					//p.id.loadValue(rs.getInt("id"));
					Patient p = loadPatientInfo(rs.getInt("id"), rs, true);
					//p.createDiag();
					list.add(p);
				}	
			}
		});
		return list;
	}
	
	/*public static LinkedList<Patient> searchName(String name) throws SQLException {
		String[] tokens = name.split(" ");
		String exp = null;
		for (int i = 0; i < tokens.length; i++) {
			//String oneExp = " ' ' + name like ?";
			String oneExp = "name REGEXP ?";
			if (exp == null) {
				exp = oneExp;
			} else {
				exp = exp + " and " + oneExp;
			}
		}
		String sql = "select * from patient where " + exp + " order by name limit 100";
		LinkedList<Patient> list = new LinkedList<>();
		prepareQuery(sql, new ResultSetLoader() {
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				for (int i = 0; i < tokens.length; i++) {
					//stmt.setString(i + 1, "%" + tokens[i] + "%");
					stmt.setString(i + 1, "[[:<:]]" + tokens[i] + "[[:>:]]");
					//System.out.println("param: " + "'[[:<:]]" + tokens[i] + "[[:>:]]'");
				}
			}
			
			@Override
			public void load(ResultSet rs) throws SQLException {
				while (rs.next()) {
					Patient p = new Patient();
					p.id.loadValue(rs.getInt("id"));
					loadPatientInfo(p, rs);
					list.add(p);
				}
			}
		});
		return list;
	}*/
	
	private static Patient loadPatientInfo(int id, ResultSet rs, boolean readWeight) throws SQLException {
		Patient p = new Patient();
		p.id.loadValue(id);
		p.name.loadValue(rs.getString("name"));
		p.dob.loadValue(rs.getDate("dob"));
		p.age = rs.getString("age");
		if (readWeight) {
			//p.weight = rs.getFloat("weight");
			p.weight = rs.getString("weight");
		}
		p.address.loadValue(rs.getString("address"));
		p.phone.loadValue(rs.getString("phone"));
		//p.weight.loadValue(rs.getFloat("weight"));
		//p.height.loadValue(rs.getInt("height"));
		p.healthNote.loadValue(rs.getString("health_note"));
		p.loadFamilyMembers(rs.getString("family"));
		p.loadExtras(rs.getString("extras"));
		p.lastVisit.loadValue(rs.getDate("last_visit"));
		p.setLastSymptoms(rs.getString("last_symptoms"));
		p.setLastDiagbrief(rs.getString("last_diagbrief"));
		p.setLastTreatments(rs.getString("last_treatments"));
		p.previousAppointment.loadValue(rs.getDate("last_appointment"));
		p.createTodayDiag();
		return p;
	}

	public static ArrayList<Medicine> selectMeds(String namePrefix) throws SQLException {
		
		String namePrefixEscaped = namePrefix
			    .replace("!", "!!")
			    .replace("%", "!%")
			    .replace("_", "!_")
			    .replace("[", "![");
		//System.out.println(namePrefixEscaped);
		
		boolean selectAll = namePrefix.trim().equals("");
		String sql;
		if (selectAll) {
			sql = "select * from medicine";
		} else {
			sql = "select * from medicine where name like ? escape '!'";
		}
		
		ArrayList<Medicine> list = new ArrayList<Medicine>();
		prepareQuery(sql, new ResultSetLoader() {
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				if (!selectAll) {
					stmt.setString(1, namePrefixEscaped + "%");
				}
			}
			
			@Override
			public void load(ResultSet rs) throws SQLException {
				while (rs.next()) {
					Medicine m = new Medicine();
					m.name.loadValue(rs.getString("name"));
					m.keywords.loadValue(rs.getString("keywords"));
					m.packageSize.loadValue(rs.getFloat("package_size"));
					m.packageBreakable.loadValue(rs.getBoolean("package_breakable"));
					m.packageUnit.loadValue(rs.getString("package_unit"));
					m.unit.loadValue(rs.getString("unit"));
					m.unitPrice.loadValue(rs.getInt("unit_price"));
					m.packageUnitPrice.loadValue(rs.getInt("package_price"));
					list.add(m);
				}
			}
		});
		return list;
	}

	public static ArrayList<AutoText> selectAutotexts(String fieldId) throws SQLException {
		
		String sql = "select * from autotext where field_id=?";
		
		ArrayList<AutoText> list = new ArrayList<>();
		prepareQuery(sql, new ResultSetLoader() {
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, fieldId);
			}
			
			@Override
			public void load(ResultSet rs) throws SQLException {
				while (rs.next()) {
					AutoText t = new AutoText();
					t.fieldId = rs.getString("field_id");
					t.content = rs.getString("content");
					t.keywords = rs.getString("keywords");
					list.add(t);
				}
			}
		});
		return list;
	}

	public static int insertAutotext(AutoText at) throws SQLException {
		
		String sql = "insert into autotext (field_id, content, keywords) values (?, ?, ?)";
		return prepareUpdate(sql, new PreparedUpdateHandler() {
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				int i = 1;
				stmt.setString(i++, at.fieldId);
				stmt.setString(i++, at.content);
				stmt.setString(i++, at.keywords);
			}
		});
	}

	public static int updateAutotext(AutoText at) throws SQLException {
		
		String sql = "update autotext set keywords=? where field_id=? and content=?";
		return prepareUpdate(sql, new PreparedUpdateHandler() {
			
			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				int i = 1;
				stmt.setString(i++, at.keywords);
				stmt.setString(i++, at.fieldId);
				stmt.setString(i++, at.content);
			}
		});
	}
	
	private static void setMedicineFields(int i, Medicine med, PreparedStatement stmt) throws SQLException {
		stmt.setString(i++, med.keywords.getValue());
		stmt.setString(i++, med.packageUnit.getValue());
		stmt.setFloat(i++, maskNull(med.packageSize.getValue()));
		stmt.setBoolean(i++, med.packageBreakable.getValue());
		stmt.setInt(i++, maskNull(med.unitPrice.getValue()));
		stmt.setInt(i++, maskNull(med.packageUnitPrice.getValue()));
		stmt.setString(i++, med.name.getValue());
		stmt.setString(i++, med.unit.getValue());
	}

	public static void insertMed(Medicine med) throws SQLException {
		String sql = "insert into medicine ("
				+ "keywords, package_unit, package_size, "
				+ "package_breakable, unit_price, package_price, name, unit) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?)";
		System.out.println(sql);
		PreparedStatement stmt = null;
		try {
			stmt = Activator.dbcon.prepareStatement(sql);
			setMedicineFields(1, med, stmt);
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public static int updateMed(Medicine med) throws SQLException {
		String sql = "update medicine set "
				+ "keywords=?, package_unit=?, package_size=?, "
				+ "package_breakable=?, unit_price=?, package_price=? "
				+ "where name=? and unit=?";
		System.out.println(sql);
		PreparedStatement stmt = null;
		try {
			stmt = Activator.dbcon.prepareStatement(sql);
			setMedicineFields(1, med, stmt);
			return stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public static int deleteMed(String name, String unit) throws SQLException {
		String sql = "delete from medicine "
				+ "where name=? and unit=?";
		System.out.println(sql);
		PreparedStatement stmt = null;
		try {
			stmt = Activator.dbcon.prepareStatement(sql);
			int i = 1;
			stmt.setString(i++, name);
			stmt.setString(i++, unit);
			return stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public static int deleteAllQueueItems() throws SQLException {
		String sql = "delete from waiting_queue";
		System.out.println(sql);
		Statement stmt = null;
		try {
			stmt = Activator.dbcon.createStatement();
			return stmt.executeUpdate(sql);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/*public static Integer getWaitingNumber(int id) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = Activator.dbcon.createStatement();
			String sql = "select ord_number from waiting_queue where patient_id=" + id;
			System.out.println(sql);
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				return rs.getInt("ord_number");
			} else {
				return null;
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}*/
}
