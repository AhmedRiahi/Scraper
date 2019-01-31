package com.pp.database.model.semantic.schema;


import lombok.Data;

@Data
public class PropertyDefinition {

	private String name;
	private PropertyType propertyType;
	private boolean isMandatory;
	private boolean isUnique;
	private boolean isDisplayString;

}
