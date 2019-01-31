package com.pp.dataTransformer.connectors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import com.pp.dataTransformer.configuration.ConfigParameters;
import com.pp.dataTransformer.exception.ConfigurationAttributeException;
import com.pp.dataTransformer.exception.DataLoadingException;
import com.pp.dataTransformer.exception.DataWritingException;
import com.pp.dataTransformer.record.RecordValue;
import com.pp.dataTransformer.record.RecordValues;
import com.pp.dataTransformer.record.RecordsSet;

public class SqlConnector extends Connector{

	protected String driver;
	protected String databaseURL;
	protected String user;
	protected String password;
	protected String query;
	
	
	
	@Override
	public ResultSet load() throws DataLoadingException {
		try {
			Class.forName(this.driver).newInstance();
			Connection connection = DriverManager.getConnection(this.databaseURL,this.user,this.password);
			Statement stmt = connection.createStatement();
			stmt.execute(this.query);
			return stmt.getResultSet();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			throw new DataLoadingException(e);
		}
	}
	
	@Override
	public RecordsSet parse(Object loadedData) throws DataLoadingException {
		RecordsSet records = new RecordsSet();
		try{
			ResultSet result = (ResultSet) loadedData;
			ResultSetMetaData metaData = result.getMetaData();
			while(result.next()){
				RecordValues record = new RecordValues();
				for(int i=1; i <= metaData.getColumnCount(); i++){
					Object obj = result.getObject(i);
					record.add(new RecordValue<>(obj));
				}
				records.add(record);
			}
		}catch(SQLException e){
			throw new DataLoadingException(e);
		}
		return records;
	}

	@Override
	public void write(RecordValues record) throws DataWritingException {
		try {
			Class.forName(this.driver).newInstance();
			Connection connection = DriverManager.getConnection(this.databaseURL,this.user,this.password);
			PreparedStatement stmt = connection.prepareStatement(this.query);
			Iterator<RecordValue<?>> iter = record.iterator();
			int i=0;
			while(iter.hasNext()){
				RecordValue<?> value = iter.next();
				stmt.setObject(++i,value.getOriginalObject());
			}
			stmt.executeUpdate();
			
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			throw new DataWritingException(e);
		}
		
	}
	
	@Override
	public void bindConnectionAttributes() throws ConfigurationAttributeException{
		this.driver 		= (String) this.sourceProvider.getParameter(ConfigParameters.DATABASE_DRIVER);
		this.databaseURL	= (String) this.sourceProvider.getParameter(ConfigParameters.DATABASE_URL);
		this.user 			= (String) this.sourceProvider.getParameter(ConfigParameters.DATABASE_USER);
		this.password 		= (String) this.sourceProvider.getParameter(ConfigParameters.DATABASE_PASSWORD);
		this.query 			= (String) this.sourceProvider.getParameter(ConfigParameters.DATABASE_QUERY);
	}
	

	/**************************---- GETTERS/SETTERS ----***********************/

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getDatabaseURL() {
		return databaseURL;
	}

	public void setDatabaseURL(String databaseURL) {
		this.databaseURL = databaseURL;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
