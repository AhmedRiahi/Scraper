package com.pp.database.kernel;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.DatastoreImpl;
import org.mongodb.morphia.Morphia;

import java.util.ArrayList;
import java.util.List;

public class MongoDatastore {

	private static Datastore datastore;
	private static Datastore stagingDatastore;
	private static Datastore publishDatastore;
	private static AdvancedDatastore advancedDatastore;
	private static final String ENVIRONMENT = "";
	private static final String DATABASE_NAME = "PPDatabase"+ENVIRONMENT;
	private static final String STAGING_DATABASE_NAME = "PPStagingDatabase"+ENVIRONMENT;
	private static final String PUBLISH_DATABASE_NAME = "PPPublishDatabase"+ENVIRONMENT;
	private static String host = "127.0.0.1";
	private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private MongoDatastore(){
    	//hide public constructor
	}

	
	public static Datastore getDatastore(){
		if( MongoDatastore.datastore == null){
			Morphia morphia = new Morphia();
            List<MongoCredential> credentialsList = new ArrayList<>();
            MongoCredential credential = MongoCredential.createScramSha1Credential(MongoDatastore.USERNAME,MongoDatastore.DATABASE_NAME,MongoDatastore.PASSWORD.toCharArray());
            credentialsList.add(credential);
            ServerAddress addr = new ServerAddress(host, 27017);
			MongoDatastore.datastore = morphia.createDatastore(new MongoClient(addr),MongoDatastore.DATABASE_NAME);
		}
		return MongoDatastore.datastore;
	}
	
	public static AdvancedDatastore getAdvancedDatastore(){
		if( MongoDatastore.advancedDatastore == null){
			Morphia morphia = new Morphia();
            List<MongoCredential> credentialsList = new ArrayList<>();
            MongoCredential credential = MongoCredential.createScramSha1Credential(MongoDatastore.USERNAME,MongoDatastore.STAGING_DATABASE_NAME,MongoDatastore.PASSWORD.toCharArray());
            credentialsList.add(credential);
            ServerAddress addr = new ServerAddress(host, 27017);
			MongoDatastore.advancedDatastore = new DatastoreImpl(morphia, new MongoClient(addr), MongoDatastore.STAGING_DATABASE_NAME);
		}
		return MongoDatastore.advancedDatastore;
	}
	
	public static Datastore getStagingDatastore() {
		if( MongoDatastore.stagingDatastore == null){
			Morphia morphia = new Morphia();
            List<MongoCredential> credentialsList = new ArrayList<>();
            MongoCredential credential = MongoCredential.createScramSha1Credential(MongoDatastore.USERNAME,MongoDatastore.STAGING_DATABASE_NAME,MongoDatastore.PASSWORD.toCharArray());
            credentialsList.add(credential);
            ServerAddress addr = new ServerAddress(host, 27017);
			MongoDatastore.stagingDatastore = morphia.createDatastore(new MongoClient(addr),MongoDatastore.STAGING_DATABASE_NAME);
		}
		return MongoDatastore.stagingDatastore;
	}
	
	public static Datastore getPublishDatastore() {
		if( MongoDatastore.publishDatastore == null){
			Morphia morphia = new Morphia();
            List<MongoCredential> credentialsList = new ArrayList<>();
            MongoCredential credential = MongoCredential.createScramSha1Credential(MongoDatastore.USERNAME,MongoDatastore.PUBLISH_DATABASE_NAME,MongoDatastore.PASSWORD.toCharArray());
            credentialsList.add(credential);
            ServerAddress addr = new ServerAddress(host, 27017);
			MongoDatastore.publishDatastore = morphia.createDatastore(new MongoClient(addr),MongoDatastore.PUBLISH_DATABASE_NAME);
		}
		return MongoDatastore.publishDatastore;
	}
}
