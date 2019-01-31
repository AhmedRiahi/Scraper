package com.pp.dataTransformer.configuration;

import java.util.HashMap;
import java.util.Map;

import com.pp.dataTransformer.exception.ConfigurationAttributeException;

public class SourceProvider {

	public enum Type{
		INPUT,
		OUTPUT
	}
	
	private Type type;
	private SupportedConnections connectionType;
	private Map<ConfigParameters, Object> parameters;
	private Map<Integer,String> mappingAttributes;
	
	public SourceProvider(){
		this.parameters = new HashMap<>();
		this.mappingAttributes = new HashMap<>();
	}
	
	public void addMappingAttribute(Integer id,String name){
		this.mappingAttributes.put(id, name);
	}
	
	
	public void addParameter(ConfigParameters parameterName,Object parameterValue){
		this.parameters.put(parameterName, parameterValue);
	}
	
	public Object getParameter(ConfigParameters parameter) throws ConfigurationAttributeException{
		Object parameterValue = this.parameters.get(parameter);
		if( parameterValue != null){
			return parameterValue;
		}
		throw new ConfigurationAttributeException(parameter.getName());
	}
	
	

	/**************************---- GETTERS/SETTERS ----***********************/
	
	public Map<Integer, String> getMappingAttributes() {
		return mappingAttributes;
	}

	public void setMappingAttributes(Map<Integer, String> mappingAttributes) {
		this.mappingAttributes = mappingAttributes;
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public SupportedConnections getConnectionType() {
		return connectionType;
	}
	
	public void setConnectionType(SupportedConnections connectionType) {
		this.connectionType = connectionType;
	}
	
	public Map<ConfigParameters, Object> getParameters() {
		return parameters;
	}
	
	public void setParameters(Map<ConfigParameters, Object> parameters) {
		this.parameters = parameters;
	}
}
