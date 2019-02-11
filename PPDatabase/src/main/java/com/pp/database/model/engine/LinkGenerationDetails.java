package com.pp.database.model.engine;

import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import lombok.Data;
import org.mongodb.morphia.annotations.Reference;

@Data
public class LinkGenerationDetails {

    private boolean isGenerateLinks = false;
    private ContentListenerModel sourceURLListener;
    private DescriptorJob targetDescriptorJob;

}
