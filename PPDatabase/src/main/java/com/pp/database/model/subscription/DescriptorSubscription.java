package com.pp.database.model.subscription;

import com.pp.database.kernel.PPEntity;
import com.pp.database.model.common.DescriptorsPortfolio;
import com.pp.database.model.engine.DescriptorJob;
import lombok.Data;
import org.mongodb.morphia.annotations.Reference;

@Data
public class DescriptorSubscription extends PPEntity {

    @Reference
    private DescriptorsPortfolio descriptorsPortfolio;
    private String descriptorJobName;
}
