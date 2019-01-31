package com.pp.analytics.service;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.pp.database.dao.common.DescriptorsPortfolioDAO;
import com.pp.database.model.engine.DescriptorJob;
import com.pp.database.model.scrapper.descriptor.DescriptorJoin;
import com.pp.database.model.scrapper.descriptor.DescriptorSemanticMapping;
import com.pp.database.model.semantic.schema.ReferencePropertyType;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.dao.semantic.PPIndividualDAO;
import com.pp.database.dao.semantic.PPIndividualSchemaDAO;
import com.pp.database.kernel.MongoDatastore;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import com.pp.database.model.semantic.individual.IndividualProperty;
import com.pp.database.model.semantic.individual.PPIndividual;
import com.pp.database.model.semantic.individual.ReferenceProperty;
import com.pp.database.model.semantic.schema.IndividualSchema;
import com.pp.database.model.semantic.schema.PrimitivePropertyType;
import com.pp.database.model.semantic.schema.PropertyDefinition;
import com.pp.framework.urlUtils.URLUtils;

@Service
public class AnalyticsService {
	
	private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

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
		log.info("Analysing Standalone descriptor population {}",workflowId);
		DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
		// Check for duplicated Staging individuals
		log.info("Got {} individuals",dwdp.getIndividuals().size());
		List<PPIndividual> individuals = this.getNoDuplicatedPublishedIndividuals(dwdp.getIndividuals());
		log.info("Got {} individuals after cleaning",individuals.size());
		dwdp.getDebugInformation().setCleanIndividualsCount(individuals.size());
		this.dwdpDAO.save(dwdp);
		if(!individuals.isEmpty()) {
			log.info("Generating individuals processing schemas");
			Set<String> schemasNames = this.getDistinctSchemas(individuals);
			dwdp.setSchemasNames(schemasNames);
			this.dwdpDAO.updateCollection(dwdp,"schemasNames",schemasNames);
			log.info("Processing Individuals properties",individuals.size());
			this.processIndividualsProperties(dwdp.getDescriptorJob(),individuals);
			// Save population to Staging Area
			log.info("Storing clean individuals into staging area.");
			this.individualDAO.saveAllConvert(individuals);
			// Copy to Publish Area
			log.info("Storing clean individuals into publish area.");
			this.copyPopulationToPublishArea(dwdp);
		}else {
			log.info("No individuals to be processed : Aborting processing");
		}
		//Analyse generated links
		this.analyseGeneratedLinks(dwdp);
	}

	public void analyseGeneratedLinks(DescriptorWorkflowDataPackage dwdp){
        log.info("Analysing Generated Links");
		if(dwdp.getDescriptorJob().isGenerateLinks()){
		    dwdp.getPortfolio().getJobs().stream().forEach(job ->{
		        if(job.isDynamicURLJob()){
		            dwdp.getGeneratedLinks().stream().forEach(link -> {
		                if(Pattern.matches(job.getDynamicUrlPattern(),link)){
		                   if(job.getToBeProcessedLinks() == null){
                               job.setToBeProcessedLinks(new HashSet<>());
                           }
                           job.getToBeProcessedLinks().add(link);
                        }
                    });
                }
                log.info("Found "+job.getToBeProcessedLinks().size()+" Matched links for job "+job.getName());
            });
        }

        this.descriptorsPortfolioDAO.save(dwdp.getPortfolio());
	}


    public void processJoinedDescriptorPopulation(String workflowId) {
        log.info("Analysing Joined descriptor population {}",workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        // Check for duplicated Staging individuals
        log.info("Got {} individuals",dwdp.getIndividuals().size());
        List<PPIndividual> individuals = this.getNoDuplicatedPublishedIndividuals(dwdp.getIndividuals());
        log.info("Got {} individuals after cleaning",individuals.size());
        dwdp.getDebugInformation().setCleanIndividualsCount(individuals.size());
        this.dwdpDAO.save(dwdp);
        if(!individuals.isEmpty()) {
            log.info("Generating individuals processing schemas");
            Set<String> schemasNames = this.getDistinctSchemas(individuals);
            dwdp.setSchemasNames(schemasNames);
            this.dwdpDAO.updateCollection(dwdp,"schemasNames",schemasNames);
            log.info("Processing Individuals properties",individuals.size());
            this.processIndividualsProperties(dwdp.getDescriptorJob(),individuals);
            // Save population to Staging Area
            log.info("Storing clean individuals into staging area.");
            this.individualDAO.saveAllConvert(individuals);
        }else {
            log.info("No individuals to be processed : Aborting processing");
        }
    }


    public void processJoinerDescriptorPopulation(String workflowId) {
        log.info("Analysing Joiner descriptor population {}",workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        DescriptorJoin join = dwdp.getJoinDetails().getDescriptorJoin();
        this.individualDAO.setDatastore(MongoDatastore.getStagingDatastore());
        DBObject query = new BasicDBObject();

        DescriptorSemanticMapping sourceDSM = join.getSourceDescriptorModel().getSemanticMappingById(join.getSourceDSMId()).get();
        DescriptorSemanticMapping targetDSM = join.getTargetDescriptorModel().getSemanticMappingById(join.getTargetDSMId()).get();
        // Select Joined Descriptor Individual Schema Name
        //TODO We are selecting only first related source
        ContentListenerModel joinedSourceContentListener = join.getSourceDescriptorModel().getSemanticRelationsAsTarget(join.getSourceContentListenerModel()).get(0).getSource();
        String joinedIndividualSchemaName  = sourceDSM.getClSemanticName(joinedSourceContentListener);
        // Query Joined Descriptor Individual
        query.put("_id",new ObjectId(dwdp.getJoinDetails().getJoinedIndividualId()));
        DBObject joinedIndividual = MongoDatastore.getAdvancedDatastore().getDB().getCollection(joinedIndividualSchemaName).find(query).next();

        //Get Joiner Individual
        PPIndividual joinerIndividual = dwdp.getIndividuals().get(0);
         //Update Joined Individual new property
        String joinedPropertyName = sourceDSM.getClSemanticName(join.getSourceContentListenerModel());
        String joinerPropertyName = targetDSM.getClSemanticName(join.getTargetContentListenerModel());
        String joinerIndividualPropertyValue = joinerIndividual.getProperty(joinerPropertyName).get().getValue();
        joinedIndividual.put(joinedPropertyName,joinerIndividualPropertyValue);
        DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(joinedIndividualSchemaName);
        collection.save(joinedIndividual);
    }


	public void processIndividual(String individualId) {
		this.individualDAO.setDatastore(MongoDatastore.getStagingDatastore());
		PPIndividual individual = this.individualDAO.get(individualId);
        this.processManuelIndividualProperties(individual);
		boolean duplicateIndividual = this.isDuplicatedIndividual(individual);
		if(!duplicateIndividual) {
			//this.processIndividualProperties(null,individual);
			this.publishIndividual(individual);
		}else {
			log.info("Duplicated individual {}",individual);
		}
	}


	public Set<String> getDistinctSchemas(List<PPIndividual> individuals){
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
		for(PPIndividual individual: individuals) {
			boolean duplicateIndividual = this.isDuplicatedIndividual(individual);
			if(!duplicateIndividual) {
				cleanIndividuals.add(individual);
			}
		}
		return cleanIndividuals;
	}
	
	
	private boolean isDuplicatedIndividual(PPIndividual individual) {
		
		IndividualSchema schema = this.individualSchemaDAO.findOne("name",individual.getSchemaName());
				
		List<PropertyDefinition> uniqueSchemaProperties = schema.getUniqueProperties();
		boolean individualExists = false;
		for(PropertyDefinition property : uniqueSchemaProperties) {
			DBObject query = new BasicDBObject();
			Optional<IndividualProperty> individualProperty = individual.getProperty(property.getName());
			if(individualProperty.isPresent()) {
				query.put(property.getName(), individual.getProperty(property.getName()).get().getValue());
				DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(schema.getName());
				DBObject dbObject = collection.findOne(query);
				if(dbObject != null) {
					individualExists = true;
					break;
				}
			}else {
				individualExists = true;
				break;
			}
			
		}
		return individualExists;
	}
	
	
	
	private void processIndividualsProperties(DescriptorJob descriptorJob, List<PPIndividual> individuals) {
		individuals.stream().forEach(individual -> {
			this.processIndividualProperties(descriptorJob.getUrl(),descriptorJob.getDescriptor(),descriptorJob.getDescriptorSemanticMappingId(),individual);
		});
	}

    private void processManuelIndividualProperties(PPIndividual individual) {
        IndividualSchema schema = this.individualSchemaDAO.findOne("name",individual.getSchemaName());
        // TODO Also handle parent properties
        List<PropertyDefinition> properties = schema.getAllProperties();
        properties.stream().forEach(property -> {
            individual.getProperty(property.getName()).ifPresent(individualProperty -> {
                if (property.isDisplayString()) {
                    String displayString = individual.getProperty(property.getName()).get().getValue();
                    individual.setDisplayString(displayString);
                }
            });
        });
    }
	
	
	private void processIndividualProperties(String url,DescriptorModel descriptor,String dsmId,PPIndividual individual) {
		
		IndividualSchema schema = this.individualSchemaDAO.findOne("name",individual.getSchemaName());
		// Get all properties including parent ones
		List<PropertyDefinition> properties = schema.getAllProperties();
		properties.stream().forEach(property -> {
			individual.getProperty(property.getName()).ifPresent(individualProperty -> {
				
				if(property.isDisplayString()) {
					String displayString = individual.getProperty(property.getName()).get().getValue();
					individual.setDisplayString(displayString);
				}

				Optional<ContentListenerModel> clOpt = descriptor.getDSMContentListenerBySemanticName(dsmId,individualProperty.getName());
                // Apply user expression on property value if exists
				clOpt.ifPresent(cl -> {
					if(cl.getPreProcessScript() !=  null) {
                        try {
                            this.executeIndividualPreProcessScript(cl.getPreProcessScript(), individualProperty);
                        } catch (ScriptException e) {
                            log.error(e.toString());
                        }
                    }
				});

				if(property.getPropertyType() instanceof PrimitivePropertyType) {
					//TODO Convert property value to propertyType (to Java type)
					switch(property.getPropertyType().getValue()) {
						case "url":
						try {
							if(individualProperty.getValue() == null || individualProperty.getValue().equals("")){
								throw new RuntimeException("Invalid individual property "+individualProperty.getName()+" : "+individualProperty.getValue());
							}
							String fullURL = URLUtils.generateFullURL(url,individualProperty.getValue());
							individualProperty.setValue(fullURL);
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
						break;
					}
				}else {
					if(property.getPropertyType() instanceof ReferencePropertyType) {
					    // TODO
                        IndividualSchema referenceSchema = this.individualSchemaDAO.findOne("name",property.getPropertyType().getValue());
                        PropertyDefinition uniqueSchemaProperty = referenceSchema.getUniqueProperties().get(0);
						//Search Reference on database

						DBCollection collection = MongoDatastore.getPublishDatastore().getDB().getCollection(property.getPropertyType().getValue());
						DBObject query = new BasicDBObject();
						query.put(uniqueSchemaProperty.getName(),individualProperty.getValue());

						DBCursor cursor = collection.find(query);
						if(!cursor.hasNext()){
                            BasicDBObject dbObject = new BasicDBObject();
						    dbObject.put(uniqueSchemaProperty.getName(),individualProperty.getValue());
                            collection.insert(dbObject);
                        }
					}
				}
				
			});
		});
	}
	
	
	private void executeIndividualPreProcessScript(String script,IndividualProperty property) throws ScriptException {
		this.engine.put("clContent", property.getValue());
		String result = (String) this.engine.eval(script);
		property.setValue(result);
	}
	
	
	private void copyPopulationToPublishArea(DescriptorWorkflowDataPackage dwdp) {
		dwdp.getSchemasNames().forEach(schemaName -> {
			DBCollection collection =  MongoDatastore.getStagingDatastore().getDB().getCollection(schemaName);
			DBObject query = new BasicDBObject();
			query.put("workflowId",dwdp.getStringId());
			DBCursor cursor = collection.find(query);
			cursor.forEach(dbObject ->{
				MongoDatastore.getPublishDatastore().getDB().getCollection(schemaName).insert(dbObject);
			});
		});
	}
}
