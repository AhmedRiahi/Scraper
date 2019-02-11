package com.pp.analytics.service;


import com.pp.database.dao.semantic.PPIndividualSchemaDAO;
import com.pp.database.model.semantic.individual.PPIndividual;
import com.pp.database.model.semantic.schema.PropertyDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IndividualsFilter {

    @Autowired
    private PPIndividualSchemaDAO individualSchemaDAO;

    public void tagInvalidIndividuals(List<PPIndividual> individuals){
        Set<String> individualsSchemasNames = individuals.stream().map(PPIndividual::getSchemaName).collect(Collectors.toSet());
        Map<String,List<PropertyDefinition>> individualsSchemas = individualsSchemasNames.stream().collect(Collectors.toMap(schemaName -> schemaName,schemaName -> this.individualSchemaDAO.findByName(schemaName).getUniqueProperties()));

        individuals.stream().forEach(individual -> {
            List<PropertyDefinition> individualSchemaUniqueProperties = individualsSchemas.get(individual.getSchemaName());
            boolean isInvalidIndividual = individualSchemaUniqueProperties.stream().anyMatch(property -> !individual.getProperty(property.getName()).isPresent());
            individual.setValid(!isInvalidIndividual);
        });
    }
}
