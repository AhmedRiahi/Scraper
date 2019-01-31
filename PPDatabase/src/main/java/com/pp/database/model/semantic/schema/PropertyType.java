package com.pp.database.model.semantic.schema;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use  =Id.NAME,include= As.PROPERTY, property="class")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PrimitivePropertyType.class, name = "primitive"),
    @JsonSubTypes.Type(value = ReferencePropertyType.class, name = "reference")
})
public class PropertyType {
	
	private String value;

	@Override
	public String toString() {
		return this.value;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
