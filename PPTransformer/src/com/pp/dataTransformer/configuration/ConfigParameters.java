package com.pp.dataTransformer.configuration;

public enum ConfigParameters {

	
	/***************---------- SQL ---------------***********/
	DATABASE_DRIVER("driver"),
	DATABASE_URL("databaseURL"),
	DATABASE_USER("user"),
	DATABASE_PASSWORD("password"),
	DATABASE_QUERY("query"),
	
	/***************---------- FILE ---------------***********/
	FILE_PATH("filePath"),
	FILE_LINE_DELIMINER("lineDeliminer"),
	FILE_ATTRIBUTE_DELIMINER("attributeDeliminer");
	
	
	private String name;
	
	
	private ConfigParameters(String name){
		this.name = name;
	}

	/**************************---- GETTERS/SETTERS ----***********************/
	
	public String getName() {
		return name;
	}
	
}
