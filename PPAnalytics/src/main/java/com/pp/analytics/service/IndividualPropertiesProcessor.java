package com.pp.analytics.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.pp.database.dao.semantic.PPIndividualDAO;
import com.pp.database.dao.semantic.PPIndividualSchemaDAO;
import com.pp.database.kernel.MongoDatastore;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import com.pp.database.model.semantic.individual.IndividualProperty;
import com.pp.database.model.semantic.individual.IndividualReferenceData;
import com.pp.database.model.semantic.individual.PPIndividual;
import com.pp.database.model.semantic.schema.IndividualSchema;
import com.pp.database.model.semantic.schema.PrimitivePropertyType;
import com.pp.database.model.semantic.schema.PropertyDefinition;
import com.pp.database.model.semantic.schema.ReferencePropertyType;
import com.pp.framework.urlUtils.URLUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IndividualPropertiesProcessor {

    @Autowired
    private PPIndividualSchemaDAO individualSchemaDAO;

    @Autowired
    private PPIndividualDAO individualDAO;

    private ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");


    public void processIndividualProperties(String url, DescriptorModel descriptor, String dsmId, PPIndividual individual) {
        IndividualSchema schema = this.individualSchemaDAO.findOne("name", individual.getSchemaName());
        // Get all properties including parent ones that exists on individual properties
        List<PropertyDefinition> schemaProperties = schema.getAllProperties().stream().filter(property -> individual.getProperty(property.getName()).isPresent()).collect(Collectors.toList());

        schemaProperties.stream().forEach(property -> {
            IndividualProperty individualProperty = individual.getProperty(property.getName()).get();
            if (property.isDisplayString()) {
                String displayString = individual.getProperty(property.getName()).get().getValue();
                individual.setDisplayString(displayString);
            }

            if (property.getPropertyType() instanceof PrimitivePropertyType && property.getPropertyType().getValue().equals("url")) {
                //TODO Convert property value to propertyType (to Java type)
                this.processUrlProperty(url, individualProperty);
            }

            if (property.getPropertyType() instanceof ReferencePropertyType) {
                Map<String,DBObject> generatedReferencesIndividuals = this.processReferenceProperty(property, individualProperty);
                generatedReferencesIndividuals.values().stream().forEach(dbObject -> {
                    dbObject.put("schemaName", property.getPropertyType().getValue());
                    dbObject.put("descriptorId", individual.getDescriptorId());
                    dbObject.put("workflowId", individual.getWorkflowId());
                    dbObject.put("displayString", individualProperty.getValue());
                    DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(property.getPropertyType().getValue());
                    collection.save(dbObject);
                });
            }
        });
        this.processPropertiesScripts(schemaProperties, descriptor, dsmId, individual);
    }

    private void processUrlProperty(String url, IndividualProperty individualProperty) {
        try {
            if (individualProperty.getValue() == null || individualProperty.getValue().equals("")) {
                throw new RuntimeException("Invalid individual property " + individualProperty.getName() + " : " + individualProperty.getValue());
            }
            String fullURL = URLUtils.generateFullURL(url, individualProperty.getValue());
            individualProperty.setValue(fullURL);
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private Map<String,DBObject> processReferenceProperty(PropertyDefinition property, IndividualProperty individualProperty) {
        IndividualSchema referenceSchema = this.individualSchemaDAO.findOne("name", property.getPropertyType().getValue());
        PropertyDefinition uniqueSchemaProperty = referenceSchema.getUniqueProperties().get(0);
        //Search Reference on database

        DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(property.getPropertyType().getValue());
        DBObject query = new BasicDBObject();
        query.put(uniqueSchemaProperty.getName(), individualProperty.getValue());
        Map<String,DBObject> generatedReferencesIndividuals = new HashMap<>();
        DBCursor cursor = collection.find(query);
        if (!cursor.hasNext()) {
            BasicDBObject dbObject = new BasicDBObject();
            dbObject.put(uniqueSchemaProperty.getName(), individualProperty.getValue());
            generatedReferencesIndividuals.put(property.getPropertyType().getValue(),dbObject);
            collection.save(dbObject);
            individualProperty.setReferenceData(new IndividualReferenceData(property.getPropertyType().getValue(),(((ObjectId)dbObject.get("_id")).toHexString())));
        }else{
            individualProperty.setReferenceData(new IndividualReferenceData(property.getPropertyType().getValue(),(((ObjectId)cursor.next().get("_id")).toHexString())));
        }
        return generatedReferencesIndividuals;
    }

    private void processPropertiesScripts(List<PropertyDefinition> schemaProperties, DescriptorModel descriptor, String dsmId, PPIndividual individual) {
        //generate content listeners values having pre-process script
        schemaProperties.stream().forEach(property -> {
            IndividualProperty individualProperty = individual.getProperty(property.getName()).get();
            Optional<ContentListenerModel> clOpt = descriptor.getDSMContentListenerBySemanticName(dsmId, individualProperty.getName());
            clOpt.ifPresent(cl -> {
                if (cl.getPreProcessScript() != null) {
                    try {
                        this.executeIndividualPreProcessScript(cl.getPreProcessScript(), individualProperty, individual);
                    } catch (ScriptException e) {
                        log.error(e.toString());
                    }
                }
            });
        });
    }


    private void executeIndividualPreProcessScript(String script, IndividualProperty individualProperty, PPIndividual ppIndividual) throws ScriptException {
        ppIndividual.getProperties().stream().forEach(property -> this.engine.put(property.getName(), property.getValue()));
        String result = (String) this.engine.eval(script);
        individualProperty.setValue(result);
    }

    public void processManuelIndividualProperties(PPIndividual individual) {
        IndividualSchema schema = this.individualSchemaDAO.findOne("name", individual.getSchemaName());
        // TODO Also handle parent properties
        List<PropertyDefinition> properties = schema.getAllProperties();
        properties.stream().forEach(property ->
                individual.getProperty(property.getName()).ifPresent(individualProperty -> {
                    if (property.isDisplayString()) {
                        String displayString = individual.getProperty(property.getName()).get().getValue();
                        individual.setDisplayString(displayString);
                    }
                })
        );
    }
}
