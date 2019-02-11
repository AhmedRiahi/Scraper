package com.pp.database.model.semantic.schema;

import com.pp.database.kernel.PPEntity;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@XmlRootElement
public class IndividualSchema extends PPEntity{

	private String name;
	private List<PropertyDefinition> properties;
	@Transient
	private List<PropertyDefinition> allProperties;
	@Reference
	private IndividualSchema parent;
	

	public IndividualSchema(){
		this.properties = new ArrayList<>();
		this.allProperties = new ArrayList<>();
	}
	
	public void addPropertyDefinition(PropertyDefinition propertyDefinition){
		this.properties.add(propertyDefinition);
	}

	@Override
	public String toString() {
		return this.name+" : "+Arrays.toString(this.properties.toArray());
	}
	
	public List<PropertyDefinition> getUniqueProperties(){
		return this.allProperties.stream().filter(p -> p.isUnique()).collect(Collectors.toList());
	}
	
	public List<PropertyDefinition> getReferenceProperties(){
		return this.allProperties
				.stream()
				.filter(p -> p.getPropertyType() instanceof ReferencePropertyType)
                .collect(Collectors.toList());
	}
	
	@PostLoad
	public void posLoad() {
		this.allProperties.addAll(this.properties);
		IndividualSchema tmpParent = this.getParent();
		while(tmpParent != null) {
			this.allProperties.addAll(tmpParent.getProperties());
			tmpParent = tmpParent.getParent();
			
		}
	}

	@java.beans.Transient
	public IndividualSchema getRootParent(){
		IndividualSchema currentSchema = this;
		while(!currentSchema.getParent().getName().equals("Thing")){
			currentSchema = currentSchema.getParent();
		}
		return currentSchema;
	}
	
	//---------------------- GETTERS / SETTERS ----------------------
	
	public List<PropertyDefinition> getProperties() {
		return properties;
	}

	public void setProperties(List<PropertyDefinition> properties) {
		this.properties = properties;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public IndividualSchema getParent() {
		return parent;
	}

	public void setParent(IndividualSchema parent) {
		this.parent = parent;
	}
	
	public List<PropertyDefinition> getAllProperties(){
		return this.allProperties;
	}
}
