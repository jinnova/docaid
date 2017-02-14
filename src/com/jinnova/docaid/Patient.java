package com.jinnova.docaid;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.Date;
import java.sql.SQLException;

public class Patient extends FieldSet {
	
	public final FieldInt id = newFieldInt();

	public final FieldString name = newFieldString();
	
	public final FieldString address = newFieldString();
	
	public final FieldDate dob = newFieldDate();
	
	public String age;
	
	public String weight;
	
	public final FieldString phone = newFieldString();
	
	//public final FieldFloat weight = newFieldFloat();
	//public float weight;
	
	//public final FieldInt height = new FieldInt();
	//public int height;
	
	//public final FieldString measurements = newFieldString();
	
	public final FieldString healthNote = newFieldString();
	
	public final FieldDate lastVisit = newFieldDate();
	
	public final FieldDate previousAppointment = newFieldDate();

	private String lastSymptoms;
	
	private String lastDiagbrief;
	
	private String lastTreatments;
	
	//public final DiagRecord directDiag = new DiagRecord(this);
	public DiagRecord todayDiag;
	public QueueTicket ticket;

	//private String measurementsPropagated;
	
	//private JsonObject measurements;
	
	private String extras;
	
	private final HashMap<Integer, PatientFamilyMember> familyMembers = new HashMap<>();
	
	private static final LinkedList<Runnable> patientSelectionEmptyListeners = new LinkedList<Runnable>();
	
	//private LinkedList<DiagRecord> diagRecords = new LinkedList<DiagRecord>();
	
	//private boolean inDB = false;
	
	public static void addPatientSelectionEmptyListener(Runnable listener) {
		patientSelectionEmptyListeners.add(listener);
	}
	
	public static void triggerPatientSelectionEmptyEvent() {
		for (Runnable listener : patientSelectionEmptyListeners) {
			listener.run();
		}
	}
	
	//constructor for loaded patient
	/*public Patient(int id, String name, Date dob, String address, String phone, 
			Date lastVisit, Date previousAppointment) {
		
		this();
		this.id.loadValue(id);
		this.name.loadValue(name);
		this.dob.loadValue(dob);
		this.address.loadValue(address);
		this.phone.loadValue(phone);
		this.lastVisit.loadValue(lastVisit);
		this.previousAppointment.loadValue(previousAppointment);
	}*/
	
	public Patient() {
		//constructor for new patient
		this.name.emptyErrorMsg = "Thiếu tên bệnh nhân";
		//this.weight.maskZero = true;
		//this.height.maskZero = true;
	}
	
	public DiagRecord createTodayDiag() {
		this.todayDiag = new DiagRecord(this);
		//this.todayDiag.propagateData(this);
		addField(this.todayDiag);
		if (Service.serviceDefault != null) {
			todayDiag.servicePrices.put(Service.serviceDefault, Service.serviceDefault.price);
		}
		return this.todayDiag;
	}
	
	public DiagRecord createHistoricalDiag() {
		return new DiagRecord(this);
	}
	
	/*public void save() {
		
		try {
			Integer newId = DBManager.save(id.getValue(), name.getValue(), 
					dob.getValue(), address.getValue(), phone.getValue());
			if (this.id.getValue() == null) {
				this.id.loadValue(newId);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}*/
	
	/*public void loadDiagRecords() {
		
	}*/
	
	/*public DiagRecord getDiagRecord() {
		return this.directDiag;
	}
	
	public DiagRecord getOrCreateDirectDiag() {
		if (this.directDiag == null) {
			this.directDiag = new DiagRecord(this);
			this.directDiag.loadPropagatedData(this);
		}
		return this.directDiag;
	}*/
	/*public QueueTicket getDirectTicket() {
		return this.directTicket;
	}*/
	
	/*public QueueTicket getOrCreateDirectTicket() {
		if (this.directTicket == null) {
			DiagRecord diag = DiagRecord.newInstance(this);
			//diag.propagatedData(this);
			this.directTicket = new QueueTicket(-1, this);
			this.directTicket.propagateData(this);
		}
		return this.directTicket;
	}*/
	
	private static SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	
	public boolean hasAppointmentToday() {
		Date d = previousAppointment.getValue();
		return d != null && df.format(d).equals(df.format(new java.util.Date()));
	}

	public boolean isInDB() {
		//return inDB;
		return id.getValue() != null;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.id.getValue()).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Patient)) {
			return false;
		}
		Patient p = (Patient) obj;
		return new EqualsBuilder().append(this.id.getValue(), p.id.getValue()).isEquals();
	}
	
	public String getExtras() {
		return extras;
	}
	
	private static Gson gson = new Gson();
	
	public void loadExtras(String s) {
		
		this.extras = s;
	}
	
	public void loadFamilyMembers(String jsonString) {
		familyMembers.clear();
		if (jsonString == null) {
			familyMembers.put(this.id.getValue(), new PatientFamilyMember(this));
			return;
		}

		JsonObject familyJson = gson.fromJson(jsonString, JsonObject.class);
		JsonArray ja = DiagRecord.getElementArray(familyJson, "members");
		if (ja == null) {
			familyMembers.put(this.id.getValue(), new PatientFamilyMember(this));
			return;
		}
		
		for (int i = 0; i < ja.size(); i++) {
			JsonObject one = DiagRecord.getElementObject(ja.get(i));
			if (one != null) {
				Integer id = DiagRecord.getElementInt(one, "id");
				String name = DiagRecord.getElementString(one, "name");
				if (id != null && name != null) {
					PatientFamilyMember member = new PatientFamilyMember(id, name);
					familyMembers.put(id, member);
					QueueTicket t = WaitingList.getQueueItemAny(id);
					if (t != null) {
						member.patient = t.patient;
					}
				}
			}
		}
	}
	
	public PatientFamilyMember[] getFamilyMembers() {
		if (familyMembers == null) {
			return null;
		}
		return familyMembers.values().toArray(new PatientFamilyMember[familyMembers.size()]);
	}
	
	public void addFamilyMember(Patient newMember) throws SQLException {

		this.familyMembers.put(newMember.id.getValue(), new PatientFamilyMember(newMember));
		this.familyMembers.putAll(newMember.familyMembers);
		//newMember.familyMembers.putAll(this.familyMembers);
		
		//HashMap<Integer, PatientFamilyMember> copy = new HashMap<>();
		//copy.putAll(this.familyMembers);
		String familyJson = this.buildFamilyJson();
		for (PatientFamilyMember m : this.familyMembers.values()) {
			if (m.patient != null && m.patient != this) {
				m.patient.familyMembers.clear();
				m.patient.familyMembers.putAll(this.familyMembers);
			}
			DBManager.updateQueueFamily(m.id, familyJson);
			DBManager.updatePatientFamily(m.id, familyJson);
		}
	}
	
	public void removeFamilyMember(int removingId, Patient removingPatient) throws SQLException {

		this.familyMembers.remove(removingId);
		//HashMap<Integer, PatientFamilyMember> copy = new HashMap<>();
		//copy.putAll(this.familyMembers);
		String familyJson = this.buildFamilyJson();
		for (PatientFamilyMember m : this.familyMembers.values()) {
			if (m.patient != null && m.patient != this) {
				m.patient.familyMembers.clear();
				m.patient.familyMembers.putAll(this.familyMembers);
			}
			DBManager.updateQueueFamily(m.id, familyJson);
			DBManager.updatePatientFamily(m.id, familyJson);
		}

		if (removingPatient != null) {
			removingPatient.familyMembers.clear();
			removingPatient.familyMembers.put(
					removingPatient.id.getValue(), new PatientFamilyMember(removingPatient));
		}
		DBManager.updateQueueFamily(removingId, null);
		DBManager.updatePatientFamily(removingId, null);
	}
	
	public boolean isInFamily(int id) {
		return this.familyMembers.containsKey(id);
	}
	
	public String buildFamilyJson() throws SQLException {
		JsonArray ja = new JsonArray();
		for (PatientFamilyMember m : this.familyMembers.values()) {
			JsonObject o = new JsonObject();
			o.addProperty("id", m.id);
			o.addProperty("name", m.name);
			ja.add(o);
		}
		JsonObject familyJson = new JsonObject();
		familyJson.add("members", ja);
		return gson.toJson(familyJson);
	}
	
	public String getAgeReading() {
		if (this.age != null && !"".equals(this.age)) {
			return this.age;
		} else {
			return this.dob.getAgeReading();
		}
	}
	
	/*public String getMeasurement(MeasurementName name) {
		if (measurements == null) {
			return "";
		}
		JsonElement e = measurements.get(name.name());
		if (e == null || e.isJsonNull()) {
			return "";
		}
		return e.getAsString();
	}
	
	public Float getMeasurementFloat(MeasurementName name) {
		String s = getMeasurement(name);
		if ("".equals(s)) {
			return null;
		}
		return Float.parseFloat(s);
	}
	
	public Integer getMeasurementInt(MeasurementName name) {
		String s = getMeasurement(name);
		if ("".equals(s)) {
			return null;
		}
		return Integer.parseInt(s);
	}*/

	public void propagateData(DiagRecord diag) {
		//weight.changeValue(diag.weight.getValueAsEditing());
		//height.changeValue(diag.height.getValueAsEditing());
		this.age = diag.age.getValueAsEditing();
		this.weight = diag.weight.getValue();
		this.extras = diag.getExtras();
		this.lastSymptoms = diag.symptons.getValue();
		this.lastDiagbrief = diag.diagnosis.getValue();
		this.lastTreatments = diag.treatment.getValue();
	}

	public String getLastSymptoms() {
		return lastSymptoms;
	}

	public String getLastDiagbrief() {
		return lastDiagbrief;
	}

	public String getLastTreatments() {
		return lastTreatments;
	}

	public void setLastSymptoms(String lastSymptoms) {
		this.lastSymptoms = lastSymptoms;
	}

	public void setLastDiagbrief(String lastDiagbrief) {
		this.lastDiagbrief = lastDiagbrief;
	}

	public void setLastTreatments(String lastTreatments) {
		this.lastTreatments = lastTreatments;
	}
}
