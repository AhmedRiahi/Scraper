package com.pp.semantic.core;

import com.pp.database.model.semantic.schema.*;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Iterator;

@Service
public class JSONSchemaBuilder {

	private static final Logger log = LoggerFactory.getLogger(JSONSchemaBuilder.class);
	
	public boolean validateSchema(JSONObject jsonObject){
		return true;
	}
	
	public IndividualSchema buildIndividualSchema(JSONObject jsonObject){
		IndividualSchema individualSchema= new IndividualSchema();
		String schemaName = jsonObject.get("id").toString();
		log.info(schemaName);
		individualSchema.setName(schemaName);
		JSONObject properties = (JSONObject) jsonObject.get("properties");
		Iterator<String> iterator = properties.keySet().iterator();
		while(iterator.hasNext()){
			String key = iterator.next();
			PropertyDefinition propertyDefinition = new PropertyDefinition();
			propertyDefinition.setName(key);
			JSONObject jsonPropertyValue = (JSONObject) properties.get(key);
			Object jsonPropertyType = jsonPropertyValue.get("type");
			PropertyType propertyType= null;
			// Primitive Property
			if(jsonPropertyType != null){
				propertyType = new PrimitivePropertyType();
				propertyType.setValue(jsonPropertyType.toString());
			}else{
				jsonPropertyType = jsonPropertyValue.get("$ref");
				// Reference Property
				if(jsonPropertyType != null){
					propertyType = new ReferencePropertyType();
					// TODO Check if reference exists
					propertyType.setValue(jsonPropertyType.toString());
				}else{
					log.info("Uknown property type of : "+jsonPropertyValue);
				}
			}
			propertyDefinition.setPropertyType(propertyType);
			individualSchema.addPropertyDefinition(propertyDefinition);
		}
		return individualSchema;
	}
	
}
