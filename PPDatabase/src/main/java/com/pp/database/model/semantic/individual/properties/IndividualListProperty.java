package com.pp.database.model.semantic.individual.properties;

import com.pp.database.model.semantic.individual.PPIndividual;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class IndividualListProperty extends IndividualBaseProperty {

    private List<PPIndividual> value = new ArrayList<>();
}
