package com.jinnova.docaid;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PatientFamilyCandidate {

	public Patient patient;
	
	public PatientFamilyCandidate(Patient p) {
		this.patient = p;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.patient.id.getValue()).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PatientFamilyCandidate)) {
			return false;
		}
		PatientFamilyCandidate p = (PatientFamilyCandidate) obj;
		return new EqualsBuilder().append(this.patient.id.getValue(), p.patient.id.getValue()).isEquals();
	}

}
