package com.jinnova.docaid;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PatientFamilyMember {

	public Patient patient;
	
	public int id;
	
	public String name;
	
	public PatientFamilyMember(Patient p) {
		this.patient = p;
		this.id = p.id.getValue();
		this.name = p.name.getValue();
	}
	
	public PatientFamilyMember(int id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PatientFamilyMember)) {
			return false;
		}
		PatientFamilyMember p = (PatientFamilyMember) obj;
		return new EqualsBuilder().append(this.id, p.id).isEquals();
	}
}
