package com.pp.analytics.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.pp.database.dao.common.DescriptorsPortfolioDAO;
import com.pp.database.dao.engine.DescriptorJobDataSetDAO;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.dao.semantic.PPIndividualDAO;
import com.pp.database.dao.semantic.PPIndividualSchemaDAO;
import com.pp.database.kernel.MongoDatastore;
import com.pp.database.model.engine.DescriptorJob;
import com.pp.database.model.engine.DescriptorJobDataSet;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.database.model.scrapper.descriptor.DescriptorSemanticMapping;
import com.pp.database.model.scrapper.descriptor.join.DescriptorJoin;
import com.pp.database.model.scrapper.descriptor.join.DescriptorJoinProperties;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import com.pp.database.model.semantic.individual.PPIndividual;
import com.pp.database.model.semantic.individual.properties.IndividualBaseProperty;
import com.pp.database.model.semantic.individual.properties.IndividualSimpleProperty;
import com.pp.database.model.semantic.schema.IndividualSchema;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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
    @Autowired
    private DescriptorJobDataSetDAO descriptorJobDataSetDAO;

    private final List<String> commonIndividualProperties = Collections.unmodifiableList(Arrays.asList("_id", "creationDate", "urlSource", "displayString", "workflowId", "descriptorId", "schemaName", "generatedURL", "previousVersion", "version"));


    public void processStandaloneDescriptorPopulation(String workflowId) {
        log.info("Analysing Standalone descriptor population {}", workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        List<PPIndividual> newIndividuals = this.generateIndividualsPopulation(dwdp);
        if (!newIndividuals.isEmpty()) {
            this.individualsPublisher.copyPopulationToPublishArea(dwdp);
        }
        this.pushGeneratedLinks(dwdp, newIndividuals);
    }

    public void processJoinedDescriptorPopulation(String workflowId) {
        log.info("Analysing Joined descriptor population {}", workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        // Keep Joined individuals in staging area, they will be published into publish area by Joiner workflow
        this.generateIndividualsPopulation(dwdp);
    }

    public void processJoinerDescriptorPopulation(String workflowId) {
        log.info("Analysing Joiner descriptor population {}", workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        this.preProcessJoinerIndividual(dwdp);
        this.updateJoinedIndividuals(dwdp);
        // Process Joiner Individual
        List<PPIndividual> newIndividuals = this.generateJoinedIndividualPopulation(dwdp.getJoinDetails().getJoinedDWDP(), dwdp.getJoinDetails().getJoinedIndividual());
        if (!newIndividuals.isEmpty()) {
            this.individualsPublisher.copyJoinedIndividualToPublishArea(dwdp.getJoinDetails().getJoinedDWDP(), dwdp.getJoinDetails().getJoinedIndividual());
        }
    }

    public List<PPIndividual> generateIndividualsPopulation(DescriptorWorkflowDataPackage dwdp) {
        log.info("Got {} individuals", dwdp.getIndividuals().size());
        // Check for duplicated Staging individuals
        List<PPIndividual> individuals = dwdp.getIndividuals().stream().filter(individual -> !individual.isPureJoinIndividual()).collect(Collectors.toList());
        Map<Boolean, List<PPIndividual>> groupingMap = this.individualsPublisher.getIndividualsGroupedByDuplication(individuals);
        List<PPIndividual> newIndividuals = new ArrayList<>();
        newIndividuals.addAll(groupingMap.get(false));
        log.info("Found {} duplicate individuals", groupingMap.get(true).size());
        if (dwdp.getDescriptorJob().isAllowVersioning()) {
            // Create duplicate individual versions
            List<PPIndividual> updatedIndividuals = this.mergeExistingIndividuals(groupingMap.get(true));
            log.info("Found {} updated duplicate individuals", updatedIndividuals.size());
            newIndividuals.addAll(updatedIndividuals);
        }
        return this.generateNewStagingIndividuals(dwdp, newIndividuals);
    }

    public List<PPIndividual> generateJoinedIndividualPopulation(DescriptorWorkflowDataPackage dwdp, PPIndividual joinedIndividual) {
        log.info("Generate Joined Individual Population", dwdp.getIndividuals().size());
        // Check for duplicated Staging individuals
        Map<Boolean, List<PPIndividual>> groupingMap = this.individualsPublisher.getIndividualsGroupedByDuplication(Arrays.asList(joinedIndividual));
        List<PPIndividual> newIndividuals = new ArrayList<>();
        newIndividuals.addAll(groupingMap.get(false));
        log.info("Found {} duplicate individuals", groupingMap.get(true).size());
        if (dwdp.getDescriptorJob().isAllowVersioning()) {
            // Create duplicate individual versions
            List<PPIndividual> updatedIndividuals = this.mergeExistingIndividuals(groupingMap.get(true));
            log.info("Found {} updated duplicate individuals", updatedIndividuals.size());
            newIndividuals.addAll(updatedIndividuals);
        }
        return this.generateNewStagingIndividuals(dwdp, newIndividuals);
    }


    private List<PPIndividual> mergeExistingIndividuals(List<PPIndividual> individuals) {
        log.info("Got {} duplicate individuals", individuals.size());
        List<PPIndividual> updatedIndividuals = new ArrayList<>();
        individuals.stream().forEach(individual -> {
            DBObject duplicateObject = this.individualsPublisher.getDuplicateIndividualLatestVersion(individual);
            List<String> oldProperties = duplicateObject.keySet().stream()
                    .filter(propertyName -> !commonIndividualProperties.contains(propertyName))
                    .collect(Collectors.toList());

            boolean individualUpdated = oldProperties.stream()
                    .anyMatch(propertyName -> !individual.hasProperty(propertyName) || !Objects.equals(duplicateObject.get(propertyName), individual.getSimpleProperty(propertyName).get().getValue()));

            if (individualUpdated) {
                individual.setPreviousVersion((ObjectId) duplicateObject.get("_id"));
                long versionNumber = (Long) duplicateObject.get("version") + 1;
                individual.setVersion(versionNumber);
                updatedIndividuals.add(individual);
            }
        });
        return updatedIndividuals;
    }

    private List<PPIndividual> generateNewStagingIndividuals(DescriptorWorkflowDataPackage dwdp, List<PPIndividual> individuals) {
        log.info("Got {} individuals after cleaning duplicate", individuals.size());
        if (dwdp.getDescriptorJob().isStandaloneMode()) {
            dwdp.getDebugInformation().setCleanIndividualsCount(individuals.size());
        } else {
            dwdp.getDebugInformation().incrementCleanIndividualsCount();
        }

        this.dwdpDAO.save(dwdp);
        List<PPIndividual> validIndividuals = new ArrayList<>();
        if (!individuals.isEmpty()) {
            log.info("Generating individuals processing schemas");
            Set<String> schemasNames = this.getDistinctSchemas(dwdp.getIndividuals());
            dwdp.setSchemasNames(schemasNames);
            this.dwdpDAO.updateCollection(dwdp, "schemasNames", schemasNames);
            log.info("Processing Individuals properties", individuals.size());
            this.processIndividualsProperties(dwdp.getDescriptorJob(), individuals);
            this.individualsFilter.tagInvalidIndividuals(individuals);
            this.dwdpDAO.updateCollection(dwdp, "individuals", individuals);
            validIndividuals = individuals.stream().filter(PPIndividual::isValid).collect(Collectors.toList());
            log.info("Got {} individuals after validation", validIndividuals.size());
            // Save population to Staging Area
            log.info("Storing clean individuals into staging area.");
            this.individualsStagingPublisher.publishToStagingArea(validIndividuals);
            // persist PPIndividuals ids related to their DBobject in staging areas
            this.dwdpDAO.save(dwdp);
        } else {
            log.info("No individuals to be processed : Aborting processing");
        }
        return validIndividuals;
    }

    public void pushGeneratedLinks(DescriptorWorkflowDataPackage dwdp, List<PPIndividual> newIndividuals) {
        log.info("Analysing Generated Links");
        if (dwdp.getDescriptorJob().getLinkGenerationDetails().isGenerateLinks()) {
            ContentListenerModel sourceUrlCL = dwdp.getDescriptorJob().getLinkGenerationDetails().getSourceURLListener();
            String sourceUrlPropertyName = dwdp.getDescriptorJob().getDescriptor().getSemanticMappingById(dwdp.getDescriptorJob().getDescriptorSemanticMappingId()).get().getClSemanticName(sourceUrlCL);
            Set<String> sourceUrls = newIndividuals.stream().map(individual -> individual.getSimpleProperty(sourceUrlPropertyName).get().getValue()).collect(Collectors.toSet());
            Optional<DescriptorJobDataSet> descriptorJobDataSetOptional = this.descriptorJobDataSetDAO.findByPortfolioAndJobName(dwdp.getPortfolio(), dwdp.getDescriptorJob().getLinkGenerationDetails().getTargetDescriptorJob().getName());
            if (!descriptorJobDataSetOptional.isPresent()) {
                DescriptorJobDataSet descriptorJobDataSet = new DescriptorJobDataSet();
                descriptorJobDataSet.setDescriptorsPortfolio(dwdp.getPortfolio());
                descriptorJobDataSet.setJobName(dwdp.getDescriptorJob().getLinkGenerationDetails().getTargetDescriptorJob().getName());
                descriptorJobDataSet.setToBeProcessedLinks(sourceUrls);
                this.descriptorJobDataSetDAO.save(descriptorJobDataSet);
            } else {
                descriptorJobDataSetOptional.get().getToBeProcessedLinks().addAll(sourceUrls);
                this.descriptorJobDataSetDAO.save(descriptorJobDataSetOptional.get());
            }
            this.descriptorsPortfolioDAO.save(dwdp.getPortfolio());
        }
    }

    private void preProcessJoinerIndividual(DescriptorWorkflowDataPackage dwdp) {
        DescriptorJoin join = dwdp.getJoinDetails().getDescriptorJoin();
        DescriptorSemanticMapping sourceDSM = join.getSourceDescriptorModel().getSemanticMappingById(join.getSourceDSMId()).get();
        if (!join.getJoinProperties().isEmpty()) {
            ContentListenerModel joinedSourceContentListener = join.getSourceDescriptorModel().getSemanticRelationsAsTarget(join.getJoinProperties().get(0).getSourceContentListenerModel()).get(0).getSource();
            String joinedIndividualSchemaName = sourceDSM.getClSemanticName(joinedSourceContentListener);
            IndividualSchema individualSchema = this.individualSchemaDAO.findByName(joinedIndividualSchemaName);
            PPIndividual joinedIndividual = dwdp.getJoinDetails().getJoinedIndividual();
            dwdp.getIndividuals().stream()
                    .filter(individual -> individual.getSchemaName().equals(joinedIndividualSchemaName))
                    .forEach(individual ->
                            individualSchema.getUniqueProperties().stream().filter(property -> joinedIndividual.getProperty(property.getName()) != null).forEach(property -> {
                                IndividualSimpleProperty individualProperty = new IndividualSimpleProperty();
                                individualProperty.setName(property.getName());
                                individualProperty.setValue(joinedIndividual.getProperty(property.getName()).get().toString());
                                individual.getProperties().add(individualProperty);
                                individual.setPureJoinIndividual(true);
                                individual.setValid(true);
                            })
                    );
        }
    }


    private void updateJoinedIndividuals(DescriptorWorkflowDataPackage dwdp) {
        DescriptorJoin join = dwdp.getJoinDetails().getDescriptorJoin();
        if (!join.getJoinProperties().isEmpty()) {
            this.individualDAO.setDatastore(MongoDatastore.getStagingDatastore());

            DescriptorSemanticMapping sourceDSM = join.getSourceDescriptorModel().getSemanticMappingById(join.getSourceDSMId()).get();
            DescriptorSemanticMapping targetDSM = join.getTargetDescriptorModel().getSemanticMappingById(join.getTargetDSMId()).get();
            // Select Joined Descriptor Individual Schema Name
            //TODO We are selecting only first related source
            ContentListenerModel joinedSourceContentListener = join.getSourceDescriptorModel().getSemanticRelationsAsTarget(join.getJoinProperties().get(0).getSourceContentListenerModel()).get(0).getSource();
            String joinedIndividualSchemaName = sourceDSM.getClSemanticName(joinedSourceContentListener);
            // Query Joined Descriptor Individual
            PPIndividual joinedIndividual = dwdp.getJoinDetails().getJoinedIndividual();

            for (DescriptorJoinProperties descriptorJoinProperties : join.getJoinProperties()) {
                //Update Joined Individual new property
                String joinedPropertyName = sourceDSM.getClSemanticName(descriptorJoinProperties.getSourceContentListenerModel());
                String joinerPropertyName = targetDSM.getClSemanticName(descriptorJoinProperties.getTargetContentListenerModel());
                //Get Joiner Individual
                PPIndividual joinerIndividual = dwdp.getValidIndividuals().get(0);
                Optional<IndividualBaseProperty> joinerProperty = joinerIndividual.getProperty(joinerPropertyName);
                if (joinerProperty.isPresent()) {
                    joinedIndividual.addProperty(joinerProperty.get());
                } else {
                    log.error("Undefined Joiner property name : {}", joinerPropertyName);
                }
            }
            DBCollection stagingCollection = MongoDatastore.getAdvancedDatastore().getDB().getCollection(joinedIndividualSchemaName);
            stagingCollection.save(this.individualDAO.individualToDBObject(joinedIndividual));
        }
    }

    private DBObject getJoinedIndividual(DescriptorWorkflowDataPackage dwdp, String joinedIndividualSchemaName) {
        DBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(dwdp.getJoinDetails().getJoinedStagingIndividualId()));
        return MongoDatastore.getStagingDatastore().getDB().getCollection(joinedIndividualSchemaName).find(query).next();
    }


    public void generateIndividualPopulation(String individualId) {
        this.individualDAO.setDatastore(MongoDatastore.getStagingDatastore());
        PPIndividual individual = this.individualDAO.get(individualId);
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
        individuals.stream().forEach(individual -> this.individualPropertiesProcessor.processIndividualProperties(descriptorJob.getCrawlingParams().getUrl(), individual));
    }

}
