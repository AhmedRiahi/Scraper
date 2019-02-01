package com.pp.database.model.common;

import com.pp.database.kernel.PPEntity;
import com.pp.database.model.engine.DescriptorJob;
import com.pp.database.model.scrapper.descriptor.DescriptorJoin;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import lombok.Data;
import lombok.NonNull;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Entity
public class DescriptorsPortfolio extends PPEntity{

    @NonNull
    private String name;
    @Reference
    private List<DescriptorModel> descriptors;
    @Embedded
    private List<DescriptorJoin> joins;
    @Embedded
    private List<DescriptorJob> jobs;

    public DescriptorsPortfolio(){
        this.descriptors = new ArrayList<>();
        this.joins = new ArrayList<>();
        this.jobs = new ArrayList<>();
    }

    public Optional<DescriptorJob> getJobByName(String jobName){
       return this.jobs.stream().filter(job -> job.getName().equals(jobName)).findFirst();
    }

    public List<DescriptorJoin> getDescriptorJoins(DescriptorModel descriptor){
        return this.joins.stream().filter(join -> join.getSourceDescriptorModel().getStringId().equals(descriptor.getStringId())).collect(Collectors.toList());
    }

}
