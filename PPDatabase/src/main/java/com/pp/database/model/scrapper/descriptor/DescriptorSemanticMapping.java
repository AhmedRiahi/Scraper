package com.pp.database.model.scrapper.descriptor;

import com.pp.database.kernel.PPEntity;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.PrePersist;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class DescriptorSemanticMapping extends PPEntity{

    private String name;
    private Map<String,String> clSemanticProperties = new HashMap<>();

    public String getClSemanticName(ContentListenerModel cl){
        return this.clSemanticProperties.get(cl.getName());
    }

    public String getClSemanticName(String clName){
        return this.clSemanticProperties.get(clName);
    }

    public Optional<String> getClNameBySemanticName(String semanticName){
        return this.clSemanticProperties.entrySet().stream().filter(entry -> entry.getValue().equals(semanticName)).map(entry -> entry.getKey()).findFirst();
    }

    @PrePersist
    public void postConstruct(){
        if(this.id == null){
            this.id = new ObjectId();
        }
    }
}
