package com.pp.database.model.scrapper.descriptor.listeners;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;

import com.pp.database.model.scrapper.descriptor.signature.SignatureModel;

@Data
@Embedded("ContentListener")
public class ContentListenerModel{
	
	// Name must be unique per descriptor
	@Id
	private String name;
	private List<SignatureModel> signatures;
	private boolean isIndividual = false;
	private String staticValue;
	private String preProcessScript;
	private boolean isJoinable = false;
	
	public ContentListenerModel(){}
	
	public ContentListenerModel(String name){
		this.name = name;
		this.signatures = new ArrayList<>();
	}
	

	@Override
	public boolean equals(Object obj) {
		return ((ContentListenerModel)obj).getName().equals(this.name);
	}

	public boolean isStatic() {
		return this.staticValue != null && !this.staticValue.isEmpty();
	}
}
