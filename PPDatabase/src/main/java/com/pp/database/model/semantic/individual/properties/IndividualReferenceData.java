package com.pp.database.model.semantic.individual.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mongodb.morphia.annotations.Embedded;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embedded
public class IndividualReferenceData {

    private String collectionName;
    private String id;
}
