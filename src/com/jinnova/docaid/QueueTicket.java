package com.jinnova.docaid;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class QueueTicket extends FieldSet {

	public final FieldInt queueNumber = newFieldInt();
	
	//public final DiagRecord diag;
	public final Patient patient;
	
	public final FieldDate previousVisit = newFieldDate();
	
	public final FieldDate previousAppointment = newFieldDate();
	
	public String lastSymptoms;
	
	public String lastDiagbrief;
	
	public String lastTreatments;
	
	public QueueStage stage = QueueStage.begin;
	
	//public String holdReason;
	
	//public final Patient patient;
	
	//public final DiagRecord currentDiag;
	
	//public final FieldFloat weight = newFieldFloat();
	
	//public final FieldInt height = newFieldInt();
	
	//public final FieldString symptons = newFieldString();
	
	public QueueTicket(Integer nbr, Patient p) {
		this.queueNumber.loadValue(nbr);
		this.patient = p;
		if (patient != null) {
			patient.ticket = this;
		}
		//this.currentDiag = new DiagRecord(patient/*, nbr*/);
		//this.currentDiag.date.loadValue(new Date(System.currentTimeMillis()));
		
		//addField(diag);
		addField(patient);
		
		//previousVisit.loadValue(diag.patient.lastVisit.getValue());
		//previousAppointment.loadValue(diag.patient.previousAppointment.getValue());
	}
	
	/*public DiagRecord createDiag() {
		DiagRecord dr = new DiagRecord(patient);
		dr.date.loadValue(new Date(System.currentTimeMillis()));
		dr.weight.loadValue(weight.getValue());
		dr.height.loadValue(height.getValue());
		dr.symptons.loadValue(symptons.getValueAsString());
		return dr;
	}*/

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.patient.id.getValue()).toHashCode();
	}
	
	public String getHoldReason() {
		if (stage != QueueStage.beginHold) {
			return null;
		}
		for (Service serv : patient.todayDiag.servicePrices.keySet()) {
			if (SettingName.isSet(SettingName.services_id + "_" + serv.id + "_holdrequired", true)) {
				return serv.name;
			}
		}
		return "Tạm dừng";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof QueueTicket)) {
			return false;
		}
		QueueTicket p = (QueueTicket) obj;
		return new EqualsBuilder().
				append(this.patient.id.getValue(), p.patient.id.getValue()).isEquals();
	}

	public void propagateData(/*Patient p*/) {
		/*if (p.dob.getValue() == null) {
			diag.age.loadValue(p.age);
		} else {
			diag.age.loadValue(p.dob.getAgeReading());
		}*/
		//diag.weight.loadValue(p.weight);
		//diag.height.loadValue(p.height);
		//diag.propagateData(p);
		this.patient.todayDiag.propagateData(this.patient);
		
		previousVisit.loadValue(patient.lastVisit.getValue());
		previousAppointment.loadValue(patient.previousAppointment.getValue());
	}
}
