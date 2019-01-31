package com.pp.dataTransformer;

import java.util.List;
import java.util.Map.Entry;

import com.pp.dataTransformer.configuration.SourceProvider;
import com.pp.dataTransformer.connectors.Connector;
import com.pp.dataTransformer.exception.ConfigurationAttributeException;
import com.pp.dataTransformer.exception.DataLoadingException;
import com.pp.dataTransformer.exception.DataWritingException;
import com.pp.dataTransformer.exception.NoDataFoundException;
import com.pp.dataTransformer.exception.UnsupportedConnectorException;
import com.pp.dataTransformer.matcher.algorithm.EntityMapper;
import com.pp.dataTransformer.record.RecordAttribute;
import com.pp.dataTransformer.record.RecordsSet;

public class Transformer {

	private SourceProvider inputConfigurationModel;
	private SourceProvider outputConfigurationModel;
	private Connector inputConnector;
	private Connector outputConnector;
	
	
	public void transform() throws DataLoadingException, DataWritingException{
		// Loading entities from connector
		RecordsSet records = this.inputConnector.read();
		if(records.size() == 0){
			Exception e = new NoDataFoundException();
			throw new DataLoadingException(e);
		}
		// Writing entities to connector
		this.outputConnector.write(records);
	}
	
	public <T> List<T> transformToEntities(Class<T> clazz) throws DataLoadingException{
		// Loading entities from connector
		RecordsSet records = this.inputConnector.read();
		for(Entry<Integer,String> entry : this.inputConfigurationModel.getMappingAttributes().entrySet()){
			records.addAttribute(new RecordAttribute(entry.getKey(),entry.getValue()));
		}
		return Transformer.transforToEntities(records, clazz);
	}
	
	public static <T> List<T> transforToEntities(RecordsSet records,Class<T> clazz) throws DataLoadingException{
		EntityMapper<T> mapper = new EntityMapper<T>(clazz);
		try {
			List<T> entities = mapper.map(records);
			return entities;
		} catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new DataLoadingException(e);
		}
	}
	
	/**************************---- GETTERS/SETTERS ----***********************/

	public SourceProvider getInputConfigurationModel() {
		return inputConfigurationModel;
	}

	public void setInputConfigurationModel(SourceProvider inputConfigurationModel) throws ConfigurationAttributeException {
		this.inputConfigurationModel 	= inputConfigurationModel;
		try {
			this.inputConnector = Connector.createConnectorFromConfiguration(this.inputConfigurationModel);
		} catch (UnsupportedConnectorException e) {
			throw new ConfigurationAttributeException("Connection");
		}
	}

	public SourceProvider getOutputConfigurationModel() {
		return outputConfigurationModel;
	}

	public void setOutputConfigurationModel(SourceProvider outputConfigurationModel) throws ConfigurationAttributeException {
		this.outputConfigurationModel 	= outputConfigurationModel;
		try {
			this.outputConnector = Connector.createConnectorFromConfiguration(this.outputConfigurationModel);
		} catch (UnsupportedConnectorException e) {
			throw new ConfigurationAttributeException("Connection");
		}
	}

	public Connector getInputConnector() {
		return inputConnector;
	}

	public void setInputConnector(Connector inputConnector) {
		this.inputConnector = inputConnector;
	}

	public Connector getOutputConnector() {
		return outputConnector;
	}

	public void setOutputConnector(Connector outputConnector) {
		this.outputConnector = outputConnector;
	}
}
