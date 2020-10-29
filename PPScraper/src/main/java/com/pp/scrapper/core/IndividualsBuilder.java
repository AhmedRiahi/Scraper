package com.pp.scrapper.core;

import com.pp.database.dao.semantic.PPIndividualSchemaDAO;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import com.pp.database.model.scrapper.descriptor.DescriptorScrapingResult;
import com.pp.database.model.scrapper.descriptor.DescriptorSemanticMapping;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import com.pp.database.model.scrapper.descriptor.listeners.ScrapedContent;
import com.pp.database.model.scrapper.descriptor.relation.AggregationRelation;
import com.pp.database.model.scrapper.descriptor.relation.CompositionRelation;
import com.pp.database.model.scrapper.descriptor.relation.SemanticRelation;
import com.pp.database.model.semantic.individual.properties.IndividualBaseProperty;
import com.pp.database.model.semantic.individual.properties.IndividualEmbeddedProperty;
import com.pp.database.model.semantic.individual.properties.IndividualListProperty;
import com.pp.database.model.semantic.individual.properties.IndividualSimpleProperty;
import com.pp.database.model.semantic.individual.PPIndividual;
import com.pp.database.model.semantic.schema.IndividualSchema;
import com.pp.database.model.semantic.schema.PropertyDefinition;
import com.pp.scrapper.core.requester.PPCrawlerRequester;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class IndividualsBuilder {

    @Autowired
    private PPCrawlerRequester ppCrawlerRequester;

    @Autowired
    private PPIndividualSchemaDAO individualSchemaDAO;

    private ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    public List<PPIndividual> buildScrapedIndividuals(DescriptorScrapingResult scrapingResult) {
        List<PPIndividual> individuals = new ArrayList<>();
        List<ContentListenerModel> orderedContentListener = ScrapingUtils.generateOrderedContentListeners(scrapingResult.getDescriptor(), DescriptorModel.SEMANTIC_LISTENER);
        DescriptorSemanticMapping dsm = scrapingResult.getDescriptor().getSemanticMappingById(scrapingResult.getDsmId()).get();
        //Get content listeners as individuals
        List<ContentListenerModel> individualContentListeners = orderedContentListener.stream().filter(ContentListenerModel::isIndividual).collect(Collectors.toList());

        Map<PPIndividual, ScrapedContent> individualsScrapedContentMapping = new HashMap<>();
        Map<ContentListenerModel, List<PPIndividual>> individualsContentListenerMapping = new HashMap<>();

        individualContentListeners.stream().forEach(cl -> {
            List<ScrapedContent> scrapedContents = scrapingResult.getScrapedContentByCL(cl);
            List<SemanticRelation> compositionSemanticRelations = scrapingResult.getDescriptor().getSemanticRelationsAsSource(cl).stream().filter(sr -> sr instanceof CompositionRelation).collect(Collectors.toList());
            //Process composition semantic relations
            for (ScrapedContent sc : scrapedContents) {
                String clSemanticName = dsm.getClSemanticName(cl);
                PPIndividual individual = new PPIndividual(clSemanticName);
                for (SemanticRelation compositionSemanticRelation : compositionSemanticRelations) {
                    String targetValue = this.getCompositionSemanticRelationTargetValue(scrapingResult, sc, compositionSemanticRelation);
                    if (targetValue != null) {
                        String propertyName = dsm.getClSemanticName(compositionSemanticRelation.getTarget());
                        IndividualSimpleProperty propertyValue = new IndividualSimpleProperty();
                        propertyValue.setName(propertyName);
                        propertyValue.setValue(targetValue);
                        individual.addProperty(propertyValue);
                    } else {
                        log.error("Invalid composition target Value " + compositionSemanticRelation.getTarget().getName());
                    }
                }
                individuals.add(individual);
                individualsScrapedContentMapping.put(individual, sc);
                individualsContentListenerMapping.putIfAbsent(cl, new ArrayList<>());
                individualsContentListenerMapping.get(cl).add(individual);
            }
        });

        log.info("Completed composition relations processing");

        individualContentListeners.stream().forEach(cl -> {
            List<SemanticRelation> aggregationSemanticRelations = scrapingResult.getDescriptor().getSemanticRelationsAsSource(cl).stream().filter(sr -> sr instanceof AggregationRelation).collect(Collectors.toList());

            //Process aggregation semantic relations
            for (SemanticRelation aggregationSemanticRelation : aggregationSemanticRelations) {

                //Cls Source/Target individuals
                List<PPIndividual> clSourceIndividuals = individualsContentListenerMapping.get(aggregationSemanticRelation.getSource());
                List<PPIndividual> clTargetIndividuals = individualsContentListenerMapping.get(aggregationSemanticRelation.getTarget());

                for (PPIndividual sourceIndividual : clSourceIndividuals) {
                    ScrapedContent sourceScrapedContent = individualsScrapedContentMapping.get(sourceIndividual);
                    List<ScrapedContent> targetScrapedContents = scrapingResult.getScrapedContentByParent(sourceScrapedContent, aggregationSemanticRelation.getTarget().getName());
                    for (PPIndividual targetIndividual : clTargetIndividuals) {
                        boolean found = targetScrapedContents.stream().anyMatch(targetScrapedContent -> targetScrapedContent.equals(individualsScrapedContentMapping.get(targetIndividual)));
                        if (found) {
                            individuals.remove(targetIndividual);
                            IndividualBaseProperty individualBaseProperty = null;
                            if (aggregationSemanticRelation.getCardinalityType().equals(SemanticRelation.CardinalityType.ONE_TO_MANY)) {
                                individualBaseProperty = new IndividualListProperty();
                                if (sourceIndividual.getProperty(aggregationSemanticRelation.getTarget().getName()).isPresent()) {
                                    individualBaseProperty = sourceIndividual.getProperty(aggregationSemanticRelation.getTarget().getName()).get();
                                    ((IndividualListProperty) individualBaseProperty).getValue().add(targetIndividual);
                                } else {
                                    individualBaseProperty.setName(aggregationSemanticRelation.getTarget().getName());
                                    ((IndividualListProperty) individualBaseProperty).setValue(new ArrayList<>());
                                    ((IndividualListProperty) individualBaseProperty).getValue().add(targetIndividual);
                                }
                            } else {
                                if (aggregationSemanticRelation.getCardinalityType().equals(SemanticRelation.CardinalityType.ONE_TO_ONE)) {
                                    individualBaseProperty = new IndividualEmbeddedProperty();
                                    individualBaseProperty.setName(aggregationSemanticRelation.getTarget().getName());
                                    ((IndividualEmbeddedProperty) individualBaseProperty).setValue(targetIndividual);
                                }
                            }
                            sourceIndividual.addProperty(individualBaseProperty);
                        }
                    }
                }

            }
        });
        this.processPropertiesScripts(scrapingResult, individuals);
        this.handleIndividualImages(individuals);
        return individuals;
    }


    private void processPropertiesScripts(DescriptorScrapingResult scrapingResult, List<PPIndividual> individuals) {

        individuals.stream().forEach(individual -> {
            IndividualSchema schema = this.individualSchemaDAO.findOne("name", individual.getSchemaName());
            // Get all properties including parent ones that exists on individual properties
            List<PropertyDefinition> schemaProperties = schema.getAllProperties().stream().filter(property -> individual.getProperty(property.getName()).isPresent()).collect(Collectors.toList());

            //generate content listeners values having pre-process script
            schemaProperties.stream().forEach(property -> {
                IndividualSimpleProperty individualProperty = (IndividualSimpleProperty) individual.getProperty(property.getName()).get();
                Optional<ContentListenerModel> clOpt = scrapingResult.getDescriptor().getDSMContentListenerBySemanticName(scrapingResult.getDsmId(), individualProperty.getName());
                clOpt.ifPresent(cl -> {
                    if (cl.getPreProcessScript() != null) {
                        try {
                            this.executeIndividualPreProcessScript(cl.getPreProcessScript(), individualProperty, individual);
                        } catch (ScriptException e) {
                            log.error(e.toString(), e);
                        }
                    }
                });
            });
        });
    }

    private void executeIndividualPreProcessScript(String script, IndividualSimpleProperty individualProperty, PPIndividual ppIndividual) throws ScriptException {
        ppIndividual.getProperties().stream().forEach(property -> {
            if (property instanceof IndividualSimpleProperty) {
                this.engine.put(property.getName(), ((IndividualSimpleProperty) property).getValue());
            }
        });
        String result = (String) this.engine.eval(script);
        individualProperty.setValue(result);
    }

    private String getCompositionSemanticRelationTargetValue(DescriptorScrapingResult scrapingResult, ScrapedContent sc, SemanticRelation sr) {
        String targetValue = null;
        if (sr.getTarget().isStatic()) {
            //Handle static content listener
            targetValue = sr.getTarget().getStaticValue();
        } else {
            ScrapedContent itemScrapedContent = null;
            List<ScrapedContent> scTargets = scrapingResult.getScrapedContentByParent(sc, sr.getTarget().getName());
            if (scTargets != null && !scTargets.isEmpty()) {
                switch (sr.getCardinalityType()) {
                    case ONE_TO_MANY:
                        break;
                    case MANY_TO_MANY:
                        break;
                    case MANY_TO_ONE:
                        itemScrapedContent = scrapingResult.getScrapedContentByCL(sr.getTarget()).get(0);
                        break;
                    case ONE_TO_ONE:
                        itemScrapedContent = scTargets.get(0);
                        break;
                }
                // TODO Bad technique to check basing on Content Listener name
                if (sr.getTarget().getName().toLowerCase().contains("link")) {
                    targetValue = itemScrapedContent.getContent().attr("href");
                } else {
                    // TODO Bad technique to check basing on element tag name
                    if (itemScrapedContent.getContent().tagName().equalsIgnoreCase("img")) {
                        targetValue = itemScrapedContent.getContent().attr("src");
                    } else {
                        targetValue = itemScrapedContent.getContent().text();
                    }

                }
            }
        }
        return targetValue;
    }

    private void handleIndividualImages(List<PPIndividual> individuals){
        individuals.stream().forEach(individual -> {
            IndividualSchema schema = this.individualSchemaDAO.findOne("name", individual.getSchemaName());
            // Get all properties including parent ones that exists on individual properties
            List<PropertyDefinition> schemaProperties = schema.getAllProperties().stream().filter(property -> individual.getProperty(property.getName()).isPresent()).collect(Collectors.toList());

            schemaProperties.stream()
                    .filter(property -> property.getPropertyType().getValue().equals("image"))
                    .forEach(property -> {
                        IndividualSimpleProperty individualProperty = (IndividualSimpleProperty) individual.getProperty(property.getName()).get();
                        String imageContent = this.ppCrawlerRequester.downloadImage(individualProperty.getValue());
                        IndividualSimpleProperty propertyValue = new IndividualSimpleProperty();
                        propertyValue.setName("imageContent");
                        propertyValue.setValue(imageContent);
                        individual.addProperty(propertyValue);
                    });
        });
    }
}
