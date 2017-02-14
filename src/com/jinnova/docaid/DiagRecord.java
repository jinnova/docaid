package com.jinnova.docaid;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DiagRecord extends FieldSet {
	
	//public QueueTicket ticket;
	
	//public boolean inDay = true;
	
	/*public QueueTicket getTicket() {
		return ticket;
	}

	public void setTicket(QueueTicket ticket) {
		this.ticket = ticket;
	}*/

	public final Patient patient;
	
	public final FieldInt id = newFieldInt();
	
	public final FieldTimestamp date = newFieldTimestamp();
	
	public final FieldString weight = newFieldString();
	
	public final FieldInt height = newFieldInt();
	
	public final FieldString age = newFieldString();
	
	public final FieldFloat bodyTemp = newFieldFloat();
	
	//public final FieldString measurements = newFieldString();

	public final FieldString symptons = newFieldString();

	public final FieldString diagnosis = newFieldString(); //chuan doan
	
	public final FieldString treatment = newFieldString();
	
	public final FieldInt treatmentDays = newFieldInt();
	
	public final FieldInt revisitDays = newFieldInt();
	
	public final FieldBoolean noRevisitIfGood = newFieldBoolean();
	
	//public final FieldBoolean hold = newFieldBoolean();
	
	public final Prescription prescription = new Prescription(this);
	
	public final HashMap<Service, Integer> servicePrices = new HashMap<Service, Integer>();
	
	/*public static DiagRecord load(Patient patient) {
		return new DiagRecord(patient);
	}*/
	
	/*public static DiagRecord newInstance(Patient patient) {
		DiagRecord dr = new DiagRecord(patient);
		
		//we don't want to propagate measurements like bodyTemp
		//loadMeasurements(patient.getMeasurements());
		//JsonObject extrasJson = gson.fromJson(patient.getExtras(), JsonObject.class);
		//JsonObject measJson = getElementObject(extrasJson, "measurements");
		//dr.weight.loadValue(getElementFloat(measJson, MeasurementName.weight.name()));
		//dr.height.loadValue(getElementInt(measJson, MeasurementName.height.name()));
		
		if (Service.serviceDefault != null) {
			dr.servicePrices.put(Service.serviceDefault, Service.serviceDefault.price);
		}
		return dr;
	}*/
	
	DiagRecord(Patient patient) {
		if (patient == null) {
			throw new NullPointerException();
		}
		this.patient = patient;
		//patient.todayDiag = this;
		//this.ticket = ticket;
		//this.queueNumber = queueNumber;
		//addField(patient);
		//addField(prescription);
		
		this.date.loadValue(new Timestamp(System.currentTimeMillis()));
		
		if (patient.dob.getValue() == null) {
			this.age.loadValue(patient.age);
		} else {
			this.age.loadValue(patient.dob.getAgeReading());
		}
		
		//this.weight.maskZero = true;
		this.height.maskZero = true;
		this.treatmentDays.maskZero = true;
		this.revisitDays.maskZero = true;
	}
	
	/*public boolean isPersisted() {
		return id.getValue() != null && id.getValue() != -1;
	}*/
	
	public boolean isInStageBegin() {
		//return inDay;
		return this == patient.todayDiag &&
				patient.ticket != null && patient.ticket.stage == QueueStage.begin;
	}
	
	public boolean isInDay() {
		//return inDay;
		return this == patient.todayDiag && 
				patient.ticket != null /*&& ticket.stage == QueueStage.begin*/;
	}
	
	public boolean isInDayNotEnd() {
		//return inDay;
		return this == patient.todayDiag &&
				patient.ticket != null && patient.ticket.stage != QueueStage.end;
	}
	
	/*public String getStage() {
		return stage;
	}*/

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.id.getValue()).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DiagRecord)) {
			return false;
		}
		DiagRecord p = (DiagRecord) obj;
		return new EqualsBuilder().append(this.id.getValue(), p.id.getValue()).isEquals();
	}
	
	public String getAgeReading() {
		String s = this.age.getValue();
		if (s != null && !"".equals(s)) {
			return s;
		} else {
			return this.patient.getAgeReading();
		}
	}
	
	public String getExtras() {
		JsonObject json = new JsonObject();
		JsonObject measJson = new JsonObject();
		measJson.addProperty(MeasurementName.weight.name(), weight.getValue());
		measJson.addProperty(MeasurementName.height.name(), height.getValue());
		measJson.addProperty(MeasurementName.bodyTemp.name(), bodyTemp.getValue());
		json.add("measurements", measJson);
		
		JsonArray serviceJson = new JsonArray();
		for (Entry<Service, Integer> e : servicePrices.entrySet()) {
			JsonObject o = new JsonObject();
			o.addProperty("id", e.getKey().id);
			o.addProperty("price", e.getValue());
			serviceJson.add(o);
		}
		json.add("services", serviceJson);
		
		return gson.toJson(json);
	}
	
	private static Gson gson = new Gson();
	
	public void loadExtras(String s) {
		JsonObject extrasJson = gson.fromJson(s, JsonObject.class);
		JsonObject measJson = getElementObject(extrasJson, "measurements");
		weight.loadValue(getElementString(measJson, MeasurementName.weight.name()));
		height.loadValue(getElementInt(measJson, MeasurementName.height.name()));
		bodyTemp.loadValue(getElementFloat(measJson, MeasurementName.bodyTemp.name()));
		
		JsonArray serviceJson = getElementArray(extrasJson, "services");
		if (serviceJson != null) {
			for (int i = 0; i < serviceJson.size(); i++) {
				JsonObject o = getElementObject(serviceJson.get(i));
				String id = getElementString(o, "id");
				Integer p = getElementInt(o, "price");
				if (id != null /*&& p != null*/) {
					Service serv = Service.allServices.get(id);
					if (serv != null) {
						servicePrices.put(serv, p);
					}
				}
			}
		}
	}
	
	public int computeServiceCost() {
		int total = 0;
		for (Entry<Service, Integer> e : servicePrices.entrySet()) {
			Integer c = e.getValue();
			if (c != null) {
				total += c;
			}
		}
		return total;
	}
	
	public void propagateData(Patient p) {
		JsonObject extrasJson = gson.fromJson(p.getExtras(), JsonObject.class);
		JsonObject measJson = getElementObject(extrasJson, "measurements");
		weight.loadValue(getElementString(measJson, MeasurementName.weight.name()));
		height.loadValue(getElementInt(measJson, MeasurementName.height.name()));
		//bodyTemp.loadValue(getElementFloat(json, MeasurementName.bodyTemp.name()));

		if (SettingName.isSet(SettingName.diag_populateFromLast.name())) {
			this.symptons.loadValue(p.getLastSymptoms());
			this.diagnosis.loadValue(p.getLastDiagbrief());
			this.treatment.loadValue(p.getLastTreatments());
		}
	}
	
	static JsonObject getElementObject(JsonObject json, String name) {
		if (json == null) {
			return null;
		}
		JsonElement e = json.get(name);
		if (e == null || e.isJsonNull()) {
			return null;
		}
		return e.getAsJsonObject();
	}
	
	static JsonObject getElementObject(JsonElement json) {
		if (json == null || json.isJsonNull()) {
			return null;
		}
		return json.getAsJsonObject();
	}
	
	static JsonArray getElementArray(JsonObject json, String name) {
		if (json == null) {
			return null;
		}
		JsonElement e = json.get(name);
		if (e == null || e.isJsonNull()) {
			return null;
		}
		return e.getAsJsonArray();
	}
	
	static Float getElementFloat(JsonObject json, String name) {
		if (json == null) {
			return null;
		}
		JsonElement e = json.get(name);
		if (e == null || e.isJsonNull()) {
			return null;
		}
		return e.getAsFloat();
	}
	
	static Integer getElementInt(JsonObject json, String name) {
		if (json == null) {
			return null;
		}
		JsonElement e = json.get(name);
		if (e == null || e.isJsonNull()) {
			return null;
		}
		return e.getAsInt();
	}
	
	static String getElementString(JsonObject json, String name) {
		if (json == null) {
			return null;
		}
		JsonElement e = json.get(name);
		if (e == null || e.isJsonNull()) {
			return null;
		}
		return e.getAsString();
	}
	
	//private static final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	
	/*public String getDate() {
		return df.format(date);
	}*/

	/*public void cancelChanges() {
		
	}

	public boolean isModified() {
		return false;
	}

	public String validate() {
		return null;
	}

	public void save() {
		
	}*/
}
