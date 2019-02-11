package com.pp.database.model.scrapper.descriptor.join;

import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import lombok.Data;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

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
    private List<DescriptorJoinProperties> joinProperties = new ArrayList<>();
}
