package com.pp.database.model.semantic.individual;

import com.mongodb.DBObject;
import com.pp.database.kernel.PPEntity;
import com.pp.database.model.semantic.individual.properties.IndividualBaseProperty;
import com.pp.database.model.semantic.individual.properties.IndividualSimpleProperty;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class PPIndividual extends PPEntity {

    @NotNull
    private String schemaName;
    @Embedded
    private List<IndividualBaseProperty> properties;
    private String descriptorId;
    private String workflowId;
    private String displayString;
    private boolean isValid = false;
    private boolean pureJoinIndividual = false;
    private ObjectId previousVersion;
    private long version = 1;


    public PPIndividual() {
        this.properties = new ArrayList<>();
    }


    public PPIndividual(String schemaName) {
        this();
        this.schemaName = schemaName;
    }

    public void addProperty(IndividualBaseProperty propertyValue) {
        this.properties.add(propertyValue);
    }

    public Optional<IndividualBaseProperty> getProperty(String propertyName) {
        return this.properties.stream().filter(p -> p.getName().equals(propertyName)).findFirst();
    }

    public Optional<IndividualSimpleProperty> getSimpleProperty(String propertyName) {
        return this.properties.stream().filter(p -> p instanceof IndividualSimpleProperty && p.getName().equals(propertyName)).map(p -> (IndividualSimpleProperty) p).findFirst();
    }

    public boolean hasProperty(String propertyName) {
        return this.properties.stream().anyMatch(p -> p.getName().equals(propertyName));
    }

    private void deleteProperty(String propertyName) {
        this.properties.remove(this.getProperty(propertyName));
    }

    public void resetProperty(String name, IndividualBaseProperty property) {
        if (this.hasProperty(property.getName())) {
            this.deleteProperty(property.getName());
        }
        this.addProperty(property);
    }

}
