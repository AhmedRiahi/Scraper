package com.pp.database.model.semantic.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlRootElement;

import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;

import com.pp.database.kernel.PPEntity;

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
	
	public Optional<PropertyDefinition> getPropertyByName(String propertyName) {
		return this.allProperties.stream().filter(property -> property.getName().equals(propertyName)).findFirst();
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
