package com.pp.dataTransformer.connectors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import com.pp.dataTransformer.configuration.ConfigParameters;
import com.pp.dataTransformer.exception.ConfigurationAttributeException;
import com.pp.dataTransformer.exception.DataLoadingException;
import com.pp.dataTransformer.exception.DataWritingException;
import com.pp.dataTransformer.record.RecordValue;
import com.pp.dataTransformer.record.RecordValues;
import com.pp.dataTransformer.record.RecordsSet;

public class FileConnector extends Connector{

	private String filePath;
	private String lineDeliminer;
	private String attributeDeliminer;
	
	
	@Override
	public String load() throws DataLoadingException {
		try {
			FileReader fileReader = new FileReader(new File(this.filePath));
			BufferedReader br = new BufferedReader(fileReader);
			String line = "";
			String allLines = "";
			while((line = br.readLine())!=null){
				allLines += line+"\n";
			}
			return allLines;
		} catch (IOException e) {
			throw new DataLoadingException(e);
		}
	}

	@Override
	public RecordsSet parse(Object loadedData) throws DataLoadingException {
		RecordsSet records = new RecordsSet();
		String allLines = (String) loadedData;
		String[] splittedLines = allLines.split(this.lineDeliminer);
		for(String line : splittedLines){
			String[] values = line.split(this.attributeDeliminer);
			RecordValues record = new RecordValues();
			for(String value : values){
				record.add(new RecordValue<>(value));
			}
			records.add(record);
		}
		return records;
	}
	
	@Override
	public void write(RecordValues entity) throws DataWritingException {
		try {
			FileWriter fileWriter = new FileWriter(this.filePath,true);
			BufferedWriter bw = new BufferedWriter(fileWriter);
			Iterator<RecordValue<?>> iter = entity.iterator();
			while(iter.hasNext()){
				Object value = iter.next();
				bw.write(value.toString());
				// If not last attribute value add <AttributeDeliminer>
				if(iter.hasNext()){
					bw.write(this.attributeDeliminer);
				}
			}
			bw.write(this.lineDeliminer);
			bw.flush();
		} catch (IOException e) {
			throw new DataWritingException(e);
		}
	}
	

	@Override
	public void bindConnectionAttributes() throws ConfigurationAttributeException {
		this.filePath 			= (String) this.sourceProvider.getParameter(ConfigParameters.FILE_PATH);
		this.lineDeliminer 		= (String) this.sourceProvider.getParameter(ConfigParameters.FILE_LINE_DELIMINER);
		this.attributeDeliminer = (String) this.sourceProvider.getParameter(ConfigParameters.FILE_ATTRIBUTE_DELIMINER);
	}
	
	/**************************---- GETTERS/SETTERS ----***********************/

	public String getLineDeliminer() {
		return lineDeliminer;
	}

	public void setLineDeliminer(String lineDeliminer) {
		this.lineDeliminer = lineDeliminer;
	}

	public String getAttributeDeliminer() {
		return attributeDeliminer;
	}

	public void setAttributeDeliminer(String attributeDeliminer) {
		this.attributeDeliminer = attributeDeliminer;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
}
