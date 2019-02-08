package com.pp.database.model.engine;


import com.pp.database.model.engine.trigger.meta.BaseTriggerMeta;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import lombok.Data;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

@Data
@Entity
public class DescriptorJob{
    @NotNull
    private String name;
    @Reference
    private DescriptorModel descriptor;
    private String descriptorSemanticMappingId;
    private BaseTriggerMeta triggerMeta;
    private DescriptorJobCrawlingParams crawlingParams = new DescriptorJobCrawlingParams();
    private Date lastCheckingDate;
    private int executionErrorsCount = 0;
    private boolean checkingRequired = false;
    private boolean isStandaloneMode = true;
    private boolean isGenerateLinks = false;
    private boolean isDynamicURLJob = false;
    private String dynamicUrlPattern;
    private Set<String> toBeProcessedLinks;

    public void incrementExecutionErrorsCount(){
        this.executionErrorsCount ++;
    }
}
