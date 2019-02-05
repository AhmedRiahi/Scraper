package com.pp.analytics.service;

import com.mongodb.*;
import com.pp.database.dao.common.DescriptorsPortfolioDAO;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.dao.semantic.PPIndividualDAO;
import com.pp.database.dao.semantic.PPIndividualSchemaDAO;
import com.pp.database.kernel.MongoDatastore;
import com.pp.database.model.engine.DescriptorJob;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.database.model.scrapper.descriptor.join.DescriptorJoin;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import com.pp.database.model.scrapper.descriptor.DescriptorSemanticMapping;
import com.pp.database.model.scrapper.descriptor.join.DescriptorJoinProperties;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import com.pp.database.model.semantic.individual.IndividualProperty;
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
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AnalyticsService {

    @Autowired
    private PPIndividualDAO individualDAO;
    @Autowired
    private DescriptorWorkflowDataPackageDAO dwdpDAO;
    @Autowired
    private PPIndividualSchemaDAO individualSchemaDAO;
    @Autowired
    private DescriptorsPortfolioDAO descriptorsPortfolioDAO;
    private ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");


    public void processStandaloneDescriptorPopulation(String workflowId) {
        log.info("Analysing Standalone descriptor population {}", workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        if (this.processIndividuals(dwdp)) {
            this.copyPopulationToPublishArea(dwdp);
        }
        //Analyse generated links
        this.analyseGeneratedLinks(dwdp);
    }

    public boolean processIndividuals(DescriptorWorkflowDataPackage dwdp) {
        log.info("Got {} individuals", dwdp.getIndividuals().size());
        // Check for duplicated Staging individuals
        List<PPIndividual> individuals = this.getNoDuplicatedPublishedIndividuals(dwdp.getIndividuals());
        log.info("Got {} individuals after cleaning", individuals.size());
        dwdp.getDebugInformation().setCleanIndividualsCount(individuals.size());
        this.dwdpDAO.save(dwdp);
        if (!individuals.isEmpty()) {
            log.info("Generating individuals processing schemas");
            Set<String> schemasNames = this.getDistinctSchemas(dwdp.getIndividuals());
            dwdp.setSchemasNames(schemasNames);
            this.dwdpDAO.updateCollection(dwdp, "schemasNames", schemasNames);
            log.info("Processing Individuals properties", individuals.size());
            this.processIndividualsProperties(dwdp.getDescriptorJob(), individuals);
            // Save population to Staging Area
            log.info("Storing clean individuals into staging area.");
            this.individualDAO.saveAllConvert(individuals);
            // Copy to Publish Area
            log.info("Storing clean individuals into publish area.");
            return true;
        } else {
            log.info("No individuals to be processed : Aborting processing");
            return false;
        }
    }

    public void analyseGeneratedLinks(DescriptorWorkflowDataPackage dwdp) {
        log.info("Analysing Generated Links");
        if (dwdp.getDescriptorJob().isGenerateLinks()) {
            dwdp.getPortfolio().getJobs().stream().forEach(job -> {
                if (job.isDynamicURLJob()) {
                    dwdp.getGeneratedLinks().stream().forEach(link -> {
                        if (Pattern.matches(job.getDynamicUrlPattern(), link)) {
                            if (job.getToBeProcessedLinks() == null) {
                                job.setToBeProcessedLinks(new HashSet<>());
                            }
                            job.getToBeProcessedLinks().add(link);
                        }
                    });
                }
                log.info("Found " + job.getToBeProcessedLinks().size() + " Matched links for job " + job.getName());
            });
        }

        this.descriptorsPortfolioDAO.save(dwdp.getPortfolio());
    }


    public void processJoinedDescriptorPopulation(String workflowId) {
        log.info("Analysing Joined descriptor population {}", workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        this.processIndividuals(dwdp);
    }


    public void processJoinerDescriptorPopulation(String workflowId) {
        log.info("Analysing Joiner descriptor population {}", workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        if(this.processIndividuals(dwdp)){
            this.copyPopulationToPublishArea(dwdp);
        }
        this.updateJoinedIndividuals(dwdp);
    }


    private void updateJoinedIndividuals(DescriptorWorkflowDataPackage dwdp){
        DescriptorJoin join = dwdp.getJoinDetails().getDescriptorJoin();
        this.individualDAO.setDatastore(MongoDatastore.getStagingDatastore());

        DescriptorSemanticMapping sourceDSM = join.getSourceDescriptorModel().getSemanticMappingById(join.getSourceDSMId()).get();
        DescriptorSemanticMapping targetDSM = join.getTargetDescriptorModel().getSemanticMappingById(join.getTargetDSMId()).get();
        // Select Joined Descriptor Individual Schema Name
        //TODO We are selecting only first related source
        ContentListenerModel joinedSourceContentListener = join.getSourceDescriptorModel().getSemanticRelationsAsTarget(join.getJoinProperties().get(0).getSourceContentListenerModel()).get(0).getSource();
        String joinedIndividualSchemaName = sourceDSM.getClSemanticName(joinedSourceContentListener);
        // Query Joined Descriptor Individual
        DBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(dwdp.getJoinDetails().getJoinedIndividualId()));
        DBObject joinedIndividual = MongoDatastore.getAdvancedDatastore().getDB().getCollection(joinedIndividualSchemaName).find(query).next();

        for (DescriptorJoinProperties descriptorJoinProperties : join.getJoinProperties()) {

            //Update Joined Individual new property
            String joinedPropertyName = sourceDSM.getClSemanticName(descriptorJoinProperties.getSourceContentListenerModel());
            String joinerPropertyName = targetDSM.getClSemanticName(descriptorJoinProperties.getTargetContentListenerModel());
            //Get Joiner Individual
            List<PPIndividual> joinerIndividuals = dwdp.getIndividuals();

            if(!joinerIndividuals.isEmpty()){
                if (joinerIndividuals.get(0).getProperty(joinerPropertyName).isPresent()) {
                    List<String> propertiesValues = joinerIndividuals.stream().map(individual -> individual.getProperty(joinerPropertyName)).filter(Optional::isPresent).map(Optional::get).map(IndividualProperty::getValue).collect(Collectors.toList());
                    joinedIndividual.put(joinedPropertyName, propertiesValues.size() == 1 ? propertiesValues.get(0) : propertiesValues);
                }
                // in case of joining individual itself
                if (joinerIndividuals.get(0).getSchemaName().equalsIgnoreCase(joinerPropertyName)) {
                    List<DBRef> dbRefs = joinerIndividuals.stream().map(joinerIndividual -> new DBRef(joinedPropertyName,joinerIndividual.getId())).collect(Collectors.toList());
                    joinedIndividual.put(joinedPropertyName, dbRefs);
                }
            }
        }

        DBCollection publishCollection = MongoDatastore.getPublishDatastore().getDB().getCollection(joinedIndividualSchemaName);
        publishCollection.save(joinedIndividual);

        DBCollection stagingCollection = MongoDatastore.getAdvancedDatastore().getDB().getCollection(joinedIndividualSchemaName);
        stagingCollection.save(joinedIndividual);
    }


    public void processIndividual(String individualId) {
        this.individualDAO.setDatastore(MongoDatastore.getStagingDatastore());
        PPIndividual individual = this.individualDAO.get(individualId);
        this.processManuelIndividualProperties(individual);
        boolean duplicateIndividual = this.isDuplicatedIndividual(individual);
        if (!duplicateIndividual) {
            this.publishIndividual(individual);
        } else {
            log.info("Duplicated individual {}", individual);
        }
    }


    public Set<String> getDistinctSchemas(List<PPIndividual> individuals) {
        Set<String> schemas = new HashSet<>();
        individuals.stream().forEach(individual -> schemas.add(individual.getSchemaName()));
        return schemas;
    }


    private void publishIndividual(PPIndividual individual) {
        DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(individual.getSchemaName());
        collection.save(individualDAO.individualToDBObject(individual));
    }


    private List<PPIndividual> getNoDuplicatedPublishedIndividuals(List<PPIndividual> individuals) {
        List<PPIndividual> cleanIndividuals = new ArrayList<>();
        for (PPIndividual individual : individuals) {
            DBObject duplicateIndividual = this.getDuplicatedIndividual(individual);
            if (duplicateIndividual == null) {
                cleanIndividuals.add(individual);
            }else{
                individual.setId((ObjectId)duplicateIndividual.get("_id"));
            }
        }
        return cleanIndividuals;
    }


    private DBObject getDuplicatedIndividual(PPIndividual individual) {

        IndividualSchema schema = this.individualSchemaDAO.findOne("name", individual.getSchemaName());

        List<PropertyDefinition> uniqueSchemaProperties = schema.getUniqueProperties();
        for (PropertyDefinition property : uniqueSchemaProperties) {
            DBObject query = new BasicDBObject();
            Optional<IndividualProperty> individualProperty = individual.getProperty(property.getName());
            if (individualProperty.isPresent()) {
                query.put(property.getName(), individual.getProperty(property.getName()).get().getValue());
                DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(schema.getName());
                DBObject dbObject = collection.findOne(query);
                return dbObject;
            } else {
                return null;
            }
        }
        return null;
    }


    private boolean isDuplicatedIndividual(PPIndividual individual){
        return this.getDuplicatedIndividual(individual) != null;
    }


    private void processIndividualsProperties(DescriptorJob descriptorJob, List<PPIndividual> individuals) {
        individuals.stream().forEach(individual -> this.processIndividualProperties(descriptorJob.getCrawlingParams().getUrl(), descriptorJob.getDescriptor(), descriptorJob.getDescriptorSemanticMappingId(), individual));
    }

    private void processManuelIndividualProperties(PPIndividual individual) {
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


    private void processIndividualProperties(String url, DescriptorModel descriptor, String dsmId, PPIndividual individual) {

        IndividualSchema schema = this.individualSchemaDAO.findOne("name", individual.getSchemaName());
        // Get all properties including parent ones
        List<PropertyDefinition> properties = schema.getAllProperties();

        properties.stream().forEach(property ->
                individual.getProperty(property.getName()).ifPresent(individualProperty -> {

                    if (property.isDisplayString()) {
                        String displayString = individual.getProperty(property.getName()).get().getValue();
                        individual.setDisplayString(displayString);
                    }

                    if (property.getPropertyType() instanceof PrimitivePropertyType) {
                        //TODO Convert property value to propertyType (to Java type)
                        switch (property.getPropertyType().getValue()) {
                            case "url":
                                try {
                                    if (individualProperty.getValue() == null || individualProperty.getValue().equals("")) {
                                        throw new RuntimeException("Invalid individual property " + individualProperty.getName() + " : " + individualProperty.getValue());
                                    }
                                    String fullURL = URLUtils.generateFullURL(url, individualProperty.getValue());
                                    individualProperty.setValue(fullURL);
                                } catch (MalformedURLException e) {
                                    log.error(e.getMessage(), e);
                                }
                                break;
                        }
                    } else {
                        if (property.getPropertyType() instanceof ReferencePropertyType) {
                            IndividualSchema referenceSchema = this.individualSchemaDAO.findOne("name", property.getPropertyType().getValue());
                            PropertyDefinition uniqueSchemaProperty = referenceSchema.getUniqueProperties().get(0);
                            //Search Reference on database

                            DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(property.getPropertyType().getValue());
                            DBObject query = new BasicDBObject();
                            query.put(uniqueSchemaProperty.getName(), individualProperty.getValue());

                            DBCursor cursor = collection.find(query);
                            if (!cursor.hasNext()) {
                                BasicDBObject dbObject = new BasicDBObject();
                                dbObject.put(uniqueSchemaProperty.getName(), individualProperty.getValue());
                                collection.insert(dbObject);
                            }
                        }
                    }
                })
        );

        properties.stream().forEach(property ->
                individual.getProperty(property.getName()).ifPresent(individualProperty -> {
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
                })
        );
    }


    private void executeIndividualPreProcessScript(String script, IndividualProperty individualProperty, PPIndividual ppIndividual) throws ScriptException {
        ppIndividual.getProperties().stream().forEach(property -> this.engine.put(property.getName(), property.getValue()));
        String result = (String) this.engine.eval(script);
        individualProperty.setValue(result);
    }


    private void copyPopulationToPublishArea(DescriptorWorkflowDataPackage dwdp) {
        dwdp.getSchemasNames().forEach(schemaName -> {
            DBCollection collection = MongoDatastore.getStagingDatastore().getDB().getCollection(schemaName);
            DBObject query = new BasicDBObject();
            query.put("workflowId", dwdp.getStringId());
            DBCursor cursor = collection.find(query);
            cursor.forEach(dbObject -> MongoDatastore.getPublishDatastore().getDB().getCollection(schemaName).insert(dbObject));
        });
    }
}
