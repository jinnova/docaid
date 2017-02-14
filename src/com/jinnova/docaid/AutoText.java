package com.jinnova.docaid;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class AutoText {

	public String fieldId;
	
	public String content;
	
	public String keywords;

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.fieldId).append(this.content).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AutoText)) {
			return false;
		}
		AutoText t = (AutoText) obj;
		return new EqualsBuilder().append(this.fieldId, t.fieldId).
				append(this.content, t.content).isEquals();
	}
}
