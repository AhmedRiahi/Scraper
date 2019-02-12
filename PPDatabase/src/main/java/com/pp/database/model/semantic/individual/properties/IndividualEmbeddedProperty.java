package com.pp.database.model.semantic.individual.properties;

import com.pp.database.model.semantic.individual.PPIndividual;
import lombok.Data;

@Data
public class IndividualEmbeddedProperty extends IndividualBaseProperty {

    private PPIndividual value;
}
