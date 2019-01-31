package com.pp.database.model.semantic.individual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import lombok.Data;
import org.mongodb.morphia.annotations.Embedded;

import com.pp.database.kernel.PPEntity;

@Data
public class PPIndividual extends PPEntity{

	@NotNull
	private String schemaName;
	@Embedded
	private List<IndividualProperty> properties;
	private String descriptorId;
	private String workflowId;
	private String displayString;


	public PPIndividual() {
		this.properties = new ArrayList<>();
	}
	

	public PPIndividual(String schemaName){
		this();
		this.schemaName = schemaName;
	}
	
	public void addProperty(IndividualProperty propertyValue){
		this.properties.add(propertyValue);
	}
	
	public Optional<IndividualProperty> getProperty(String propertyName) {
		return this.properties.stream().filter(p -> p.getName().equals(propertyName)).findFirst();
	}
	
	public boolean hasProperty(String propertyName) {
		return this.properties.stream().anyMatch(p -> p.getName().equals(propertyName));
	}

}
