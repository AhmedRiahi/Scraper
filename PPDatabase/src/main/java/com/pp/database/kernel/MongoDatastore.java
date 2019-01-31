package com.pp.database.kernel;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.DatastoreImpl;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

public class MongoDatastore {

	private static Datastore datastore;
	private static Datastore stagingDatastore;
	private static Datastore publishDatastore;
	private static AdvancedDatastore advancedDatastore;
	private static String ENVIRONMENT = "";
	private static final String databaseName = "PPDatabase"+ENVIRONMENT;
	private static final String stagingDatabase = "PPStagingDatabase"+ENVIRONMENT;
	private static final String publishDatabase = "PPPublishDatabase"+ENVIRONMENT;
	private static String host = "127.0.0.1";
    //private static String host = "151.80.145.65";
	private static final String USERNAME = "";
    private static final String password = "";

	
	public static Datastore getDatastore(){
		if( MongoDatastore.datastore == null){
			Morphia morphia = new Morphia();
            List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
            MongoCredential credential = MongoCredential.createScramSha1Credential(MongoDatastore.USERNAME,MongoDatastore.databaseName,MongoDatastore.password.toCharArray());
            credentialsList.add(credential);
            ServerAddress addr = new ServerAddress(host, 27017);
			MongoDatastore.datastore = morphia.createDatastore(new MongoClient(addr),MongoDatastore.databaseName);
		}
		return MongoDatastore.datastore;
	}
	
	public static AdvancedDatastore getAdvancedDatastore(){
		if( MongoDatastore.advancedDatastore == null){
			Morphia morphia = new Morphia();
            List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
            MongoCredential credential = MongoCredential.createScramSha1Credential(MongoDatastore.USERNAME,MongoDatastore.stagingDatabase,MongoDatastore.password.toCharArray());
            credentialsList.add(credential);
            ServerAddress addr = new ServerAddress(host, 27017);
			MongoDatastore.advancedDatastore = new DatastoreImpl(morphia, new MongoClient(addr), MongoDatastore.stagingDatabase);
		}
		return MongoDatastore.advancedDatastore;
	}
	
	public static Datastore getStagingDatastore() {
		if( MongoDatastore.stagingDatastore == null){
			Morphia morphia = new Morphia();
            List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
            MongoCredential credential = MongoCredential.createScramSha1Credential(MongoDatastore.USERNAME,MongoDatastore.stagingDatabase,MongoDatastore.password.toCharArray());
            credentialsList.add(credential);
            ServerAddress addr = new ServerAddress(host, 27017);
			MongoDatastore.stagingDatastore = morphia.createDatastore(new MongoClient(addr),MongoDatastore.stagingDatabase);
		}
		return MongoDatastore.stagingDatastore;
	}
	
	public static Datastore getPublishDatastore() {
		if( MongoDatastore.publishDatastore == null){
			Morphia morphia = new Morphia();
            List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
            MongoCredential credential = MongoCredential.createScramSha1Credential(MongoDatastore.USERNAME,MongoDatastore.publishDatabase,MongoDatastore.password.toCharArray());
            credentialsList.add(credential);
            ServerAddress addr = new ServerAddress(host, 27017);
			MongoDatastore.publishDatastore = morphia.createDatastore(new MongoClient(addr),MongoDatastore.publishDatabase);
		}
		return MongoDatastore.publishDatastore;
	}
}
