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
    private DescriptorsPortfolioDAO descriptorsPortfolioDAO;
    @Autowired
    private IndividualPropertiesProcessor individualPropertiesProcessor;
    @Autowired
    private IndividualsPublisher individualsPublisher;
    @Autowired
    private IndividualsStagingPublisher individualsStagingPublisher;


    public void processStandaloneDescriptorPopulation(String workflowId) {
        log.info("Analysing Standalone descriptor population {}", workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        if (this.processIndividuals(dwdp)) {
            this.individualsPublisher.copyPopulationToPublishArea(dwdp);
        }
        //Analyse generated links
        this.analyseGeneratedLinks(dwdp);
    }


    public boolean processIndividuals(DescriptorWorkflowDataPackage dwdp) {
        log.info("Got {} individuals", dwdp.getIndividuals().size());
        // Check for duplicated Staging individuals
        List<PPIndividual> individuals = this.individualsPublisher.getNoDuplicatedPublishedIndividuals(dwdp.getIndividuals());
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
            this.individualsStagingPublisher.publishToStagingArea(individuals);
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
        if(this.processIndividuals(dwdp)){
            this.individualsPublisher.copyPopulationToPublishArea(dwdp);
        }
    }


    public void processJoinerDescriptorPopulation(String workflowId) {
        log.info("Analysing Joiner descriptor population {}", workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        if (this.processIndividuals(dwdp)) {
            this.individualsPublisher.copyPopulationToPublishArea(dwdp);
        }
        this.updateJoinedIndividuals(dwdp);
    }


    private void updateJoinedIndividuals(DescriptorWorkflowDataPackage dwdp) {
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

            if (!joinerIndividuals.isEmpty()) {
                // in case of joining individual itself
                if (joinerIndividuals.get(0).getSchemaName().equalsIgnoreCase(joinerPropertyName)) {
                    List<DBRef> dbRefs = joinerIndividuals.stream().map(joinerIndividual -> new DBRef(joinedPropertyName, joinerIndividual.getId())).collect(Collectors.toList());
                    joinedIndividual.put(joinedPropertyName, dbRefs);
                } else {
                    if (joinerIndividuals.get(0).getProperty(joinerPropertyName).isPresent()) {
                        List<String> propertiesValues = joinerIndividuals.stream().map(individual -> individual.getProperty(joinerPropertyName)).filter(Optional::isPresent).map(Optional::get).map(IndividualProperty::getValue).collect(Collectors.toList());
                        joinedIndividual.put(joinedPropertyName, propertiesValues.size() == 1 ? propertiesValues.get(0) : propertiesValues);
                    }
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
        this.individualPropertiesProcessor.processManuelIndividualProperties(individual);
        boolean duplicateIndividual = this.individualsPublisher.isDuplicatedIndividual(individual);
        if (!duplicateIndividual) {
            this.individualsPublisher.publishIndividual(individual);
        } else {
            log.info("Duplicated individual {}", individual);
        }
    }


    public Set<String> getDistinctSchemas(List<PPIndividual> individuals) {
        Set<String> schemas = new HashSet<>();
        individuals.stream().forEach(individual -> schemas.add(individual.getSchemaName()));
        return schemas;
    }

    private void processIndividualsProperties(DescriptorJob descriptorJob, List<PPIndividual> individuals) {
        individuals.stream().forEach(individual -> this.individualPropertiesProcessor.processIndividualProperties(descriptorJob.getCrawlingParams().getUrl(), descriptorJob.getDescriptor(), descriptorJob.getDescriptorSemanticMappingId(), individual));
    }



}
