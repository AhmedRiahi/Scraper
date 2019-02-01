package com.pp.database.model.scrapper.descriptor;

import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import lombok.Data;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import javax.validation.constraints.NotNull;

@Data
@Entity
public class DescriptorJoin{
    @NotNull
    private String name;
    @Reference
    private DescriptorModel sourceDescriptorModel;
    private String sourceDSMId;
    @Reference
    private DescriptorModel targetDescriptorModel;
    private String targetDSMId;
    private ContentListenerModel sourceURLListener;
    private ContentListenerModel sourceContentListenerModel;
    private ContentListenerModel targetContentListenerModel;
}
