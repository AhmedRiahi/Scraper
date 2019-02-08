package com.pp.analytics.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.pp.database.dao.common.DescriptorsPortfolioDAO;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.dao.semantic.PPIndividualDAO;
import com.pp.database.dao.semantic.PPIndividualSchemaDAO;
import com.pp.database.kernel.MongoDatastore;
import com.pp.database.model.engine.DescriptorJob;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.database.model.scrapper.descriptor.DescriptorSemanticMapping;
import com.pp.database.model.scrapper.descriptor.join.DescriptorJoin;
import com.pp.database.model.scrapper.descriptor.join.DescriptorJoinProperties;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import com.pp.database.model.semantic.individual.IndividualProperty;
import com.pp.database.model.semantic.individual.PPIndividual;
import com.pp.database.model.semantic.schema.IndividualSchema;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

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
    @Autowired
    private IndividualsFilter individualsFilter;
    @Autowired
    private PPIndividualSchemaDAO individualSchemaDAO;


    public void processStandaloneDescriptorPopulation(String workflowId) {
        log.info("Analysing Standalone descriptor population {}", workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        if (this.generateIndividualsPopulation(dwdp)) {
            this.individualsPublisher.copyPopulationToPublishArea(dwdp);
        }
        this.analyseGeneratedLinks(dwdp);
    }


    public boolean generateIndividualsPopulation(DescriptorWorkflowDataPackage dwdp) {
        log.info("Got {} individuals", dwdp.getIndividuals().size());
        // Check for duplicated Staging individuals
        List<PPIndividual> individuals = dwdp.getIndividuals().stream().filter(individual -> !individual.isPureJoinIndividual()).collect(Collectors.toList());
        individuals = this.individualsPublisher.getNoDuplicatedPublishedIndividuals(individuals);
        log.info("Got {} individuals after cleaning duplicate", individuals.size());
        dwdp.getDebugInformation().setCleanIndividualsCount(individuals.size());
        this.dwdpDAO.save(dwdp);
        if (!individuals.isEmpty()) {
            log.info("Generating individuals processing schemas");
            Set<String> schemasNames = this.getDistinctSchemas(dwdp.getIndividuals());
            dwdp.setSchemasNames(schemasNames);
            this.dwdpDAO.updateCollection(dwdp, "schemasNames", schemasNames);
            log.info("Processing Individuals properties", individuals.size());
            this.processIndividualsProperties(dwdp.getDescriptorJob(), individuals);
            this.individualsFilter.tagInvalidIndividuals(individuals);
            this.dwdpDAO.updateCollection(dwdp,"individuals",individuals);
            List<PPIndividual> validIndividuals = individuals.stream().filter(PPIndividual::isValid).collect(Collectors.toList());
            log.info("Got {} individuals after validation", validIndividuals.size());
            // Save population to Staging Area
            log.info("Storing clean individuals into staging area.");
            this.individualsStagingPublisher.publishToStagingArea(validIndividuals);
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
        if(this.generateIndividualsPopulation(dwdp)){
            this.individualsPublisher.copyPopulationToPublishArea(dwdp);
        }
    }


    public void processJoinerDescriptorPopulation(String workflowId) {
        log.info("Analysing Joiner descriptor population {}", workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        List<PPIndividual> pureJoinIndividuals = this.preProcessJoinerIndividuals(dwdp);
        if (this.generateIndividualsPopulation(dwdp)) {
            this.individualsPublisher.copyPopulationToPublishArea(dwdp);
        }
        this.updateJoinedIndividuals(dwdp);
    }


    private List<PPIndividual> preProcessJoinerIndividuals(DescriptorWorkflowDataPackage dwdp){

        DescriptorJoin join = dwdp.getJoinDetails().getDescriptorJoin();
        DescriptorSemanticMapping sourceDSM = join.getSourceDescriptorModel().getSemanticMappingById(join.getSourceDSMId()).get();
        ContentListenerModel joinedSourceContentListener = join.getSourceDescriptorModel().getSemanticRelationsAsTarget(join.getJoinProperties().get(0).getSourceContentListenerModel()).get(0).getSource();
        String joinedIndividualSchemaName = sourceDSM.getClSemanticName(joinedSourceContentListener);
        IndividualSchema individualSchema = this.individualSchemaDAO.findByName(joinedIndividualSchemaName);
        DBObject joinedIndividual = this.getJoinedIndividual(dwdp,joinedIndividualSchemaName);
        return dwdp.getIndividuals().stream()
                .filter(individual -> individual.getSchemaName().equals(joinedIndividualSchemaName))
                .map(individual -> {
                    individualSchema.getUniqueProperties().stream().filter(property -> joinedIndividual.get(property.getName()) != null).forEach(property -> {
                            IndividualProperty individualProperty = new IndividualProperty();
                            individualProperty.setName(property.getName());
                            individualProperty.setValue(joinedIndividual.get(property.getName()).toString());
                            individual.getProperties().add(individualProperty);
                            individual.setPureJoinIndividual(true);
                            individual.setValid(true);
                    });
                    return individual;
        }).collect(Collectors.toList());
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
        DBObject joinedIndividual = this.getJoinedIndividual(dwdp, joinedIndividualSchemaName);

        for (DescriptorJoinProperties descriptorJoinProperties : join.getJoinProperties()) {
            //Update Joined Individual new property
            String joinedPropertyName = sourceDSM.getClSemanticName(descriptorJoinProperties.getSourceContentListenerModel());
            String joinerPropertyName = targetDSM.getClSemanticName(descriptorJoinProperties.getTargetContentListenerModel());
            //Get Joiner Individual
            List<PPIndividual> joinerIndividuals = dwdp.getValidIndividuals();

            Map<String,List<PPIndividual>> groupedIndividuals = joinerIndividuals.stream().collect(groupingBy(PPIndividual::getSchemaName));
            groupedIndividuals.entrySet().stream().forEach(entry -> {
                if (!entry.getValue().isEmpty()) {
                    // in case of joining individual itself
                    if (entry.getKey().equalsIgnoreCase(joinerPropertyName)) {
                        List<DBRef> dbRefs = entry.getValue().stream().map(joinerIndividual -> new DBRef(joinedPropertyName, joinerIndividual.getId())).collect(Collectors.toList());
                        joinedIndividual.put(joinedPropertyName, dbRefs);
                    } else {
                        if (entry.getValue().get(0).getProperty(joinerPropertyName).isPresent()) {
                            List<String> propertiesValues = entry.getValue().stream().map(individual -> individual.getProperty(joinerPropertyName)).filter(Optional::isPresent).map(Optional::get).map(IndividualProperty::getValue).collect(Collectors.toList());
                            joinedIndividual.put(joinedPropertyName, propertiesValues.size() == 1 ? propertiesValues.get(0) : propertiesValues);
                        }
                    }
                }
            });
        }

        DBCollection publishCollection = MongoDatastore.getPublishDatastore().getDB().getCollection(joinedIndividualSchemaName);
        publishCollection.save(joinedIndividual);

        DBCollection stagingCollection = MongoDatastore.getAdvancedDatastore().getDB().getCollection(joinedIndividualSchemaName);
        stagingCollection.save(joinedIndividual);
    }

    private DBObject getJoinedIndividual(DescriptorWorkflowDataPackage dwdp, String joinedIndividualSchemaName) {
        DBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(dwdp.getJoinDetails().getJoinedIndividualId()));
        return MongoDatastore.getPublishDatastore().getDB().getCollection(joinedIndividualSchemaName).find(query).next();
    }


    public void generateIndividualPopulation(String individualId) {
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
