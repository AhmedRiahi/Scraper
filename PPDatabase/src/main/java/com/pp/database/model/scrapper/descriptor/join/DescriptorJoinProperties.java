package com.pp.database.model.scrapper.descriptor.join;

import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import lombok.Data;
import org.mongodb.morphia.annotations.Entity;


@Data
@Entity
public class DescriptorJoinProperties {

    private ContentListenerModel sourceContentListenerModel;
    private ContentListenerModel targetContentListenerModel;
}
