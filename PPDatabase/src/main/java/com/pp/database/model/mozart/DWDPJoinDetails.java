package com.pp.database.model.mozart;

import com.pp.database.model.scrapper.descriptor.join.DescriptorJoin;
import com.pp.database.model.semantic.individual.PPIndividual;
import lombok.Data;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

@Data
@Entity
public class DWDPJoinDetails {

    private boolean isJoiner = false;
    @Embedded
    private DescriptorJoin descriptorJoin;
    private String joinedStagingIndividualId;
    private PPIndividual joinedIndividual;
    @Reference
    private DescriptorWorkflowDataPackage joinedDWDP;
    private int launchedJoinersCount;
    private int finishedJoinersCount;
}
