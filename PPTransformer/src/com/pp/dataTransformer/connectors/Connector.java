package com.pp.dataTransformer.connectors;

import java.util.List;

import com.pp.dataTransformer.configuration.SourceProvider;
import com.pp.dataTransformer.exception.ConfigurationAttributeException;
import com.pp.dataTransformer.exception.DataLoadingException;
import com.pp.dataTransformer.exception.DataWritingException;
import com.pp.dataTransformer.exception.UnsupportedConnectorException;
import com.pp.dataTransformer.record.RecordValues;
import com.pp.dataTransformer.record.RecordsSet;

public abstract class Connector {

	protected SourceProvider sourceProvider;
	
	public abstract void bindConnectionAttributes() throws ConfigurationAttributeException;
	public abstract Object load() throws DataLoadingException;
	public abstract RecordsSet parse(Object loadedData) throws DataLoadingException;
	public abstract void write(RecordValues entity) throws DataWritingException;
	
	
	public void write(List<RecordValues> records) throws DataWritingException{
		for(RecordValues entity : records){
			this.write(entity);
		}
	}
	
	public RecordsSet read() throws DataLoadingException{
		Object loadedData = this.load();
		return this.parse(loadedData);
	}
	
	
	public static Connector createConnectorFromConfiguration(SourceProvider sourceProvider) throws ConfigurationAttributeException, UnsupportedConnectorException{
		Connector connector = null;
		switch (sourceProvider.getConnectionType()) {
		case SQL:
			connector = new SqlConnector();
			break;
			
		case HTTP_FILE:
			//connector = new HttpConnector();
			break;
			
		case SYSTEM_FILE:
			connector = new FileConnector();
			break;

		default:
			throw new UnsupportedConnectorException();
		}
		connector.setSourceProvider(sourceProvider);
		return connector;
	}
	
	/**************************---- GETTERS/SETTERS ----***********************/
	
	public SourceProvider getSourceProvider() {
		return sourceProvider;
	}
	
	public void setSourceProvider(SourceProvider sourceProvider) throws ConfigurationAttributeException {
		this.sourceProvider = sourceProvider;
		this.bindConnectionAttributes();
	}
}
