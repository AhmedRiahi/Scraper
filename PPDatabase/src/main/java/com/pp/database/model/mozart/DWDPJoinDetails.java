package com.pp.database.model.mozart;

import com.pp.database.model.scrapper.descriptor.DescriptorJoin;
import lombok.Data;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

@Data
@Entity
public class DWDPJoinDetails {

    private boolean isJoin = false;
    @Embedded
    private DescriptorJoin descriptorJoin;
    private String joinedIndividualId;
    @Reference
    private DescriptorWorkflowDataPackage joinedDWDP;
    private int launchedJoinersCount;
    private int finishedJoinersCount;


    public void incrementFinsihedJoinersCount(){
        this.finishedJoinersCount++;
    }
}
