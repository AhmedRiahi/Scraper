package com.pp.dataTransformer.exception;

public class ConfigurationAttributeException extends Exception{

	public ConfigurationAttributeException(String parameterName){
		super("Invalid parameter : "+parameterName);
	}
}
