package com.pp.subscription.core;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.util.JSON;
import com.pp.database.kernel.MongoDatastore;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class IndividualToBeanConverter {


    public DBObject convert(DBObject dbObject){
        dbObject.removeField("_id");
        dbObject.keySet().stream().forEach(key -> {
            if(dbObject.get(key) instanceof DBRef){
                DBRef dbRef = (DBRef) dbObject.get(key);
                DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(dbRef.getCollectionName());
                DBObject query = new BasicDBObject();
                query.put("_id", dbRef.getId());
                if(collection.find(query).hasNext()){
                    DBObject refIndividual = collection.find(query).next();
                    refIndividual.removeField("_id");
                    dbObject.put(key,refIndividual);
                }
            }
        });
        return dbObject;
    }

}
