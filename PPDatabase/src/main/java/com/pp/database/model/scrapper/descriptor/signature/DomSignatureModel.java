package com.pp.database.model.scrapper.descriptor.signature;

import org.mongodb.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity("DomSignature")
public class DomSignatureModel extends SignatureModel{

	
	public DomSignatureModel(){
		super();
	}
	
	public DomSignatureModel(SignatureType type,String value){
		super(type,value);
	}

}
