package com.pp.database.model.engine;

import com.pp.database.kernel.PPEntity;
import com.pp.database.model.common.DescriptorsPortfolio;
import lombok.Data;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class DescriptorJobDataSet extends PPEntity {

    @Reference
    private DescriptorsPortfolio descriptorsPortfolio;
    private String jobName;
    private Set<String> toBeProcessedLinks = new HashSet<>();
}
