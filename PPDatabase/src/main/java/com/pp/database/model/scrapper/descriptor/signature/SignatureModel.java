package com.pp.database.model.scrapper.descriptor.signature;

import org.mongodb.morphia.annotations.Entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use  =Id.NAME,include= As.PROPERTY, property="type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = DomSignatureModel.class, name = "domSignatureModel")
})
@Entity("Signature")
public abstract class SignatureModel{
	
	protected SignatureType signatureType;
	protected String value;
	
	public SignatureModel(){}
	
	public SignatureModel(SignatureType signatureType,String value){
		this.signatureType = signatureType;
		this.value = value;
	}
	
	// -------------------------------- GETTER / SETTER --------------------------------

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public SignatureType getSignatureType() {
		return signatureType;
	}

	public void setSignatureType(SignatureType signatureType) {
		this.signatureType = signatureType;
	}

}
