package com.pp.database.model.scrapper.descriptor.signature;

import javax.xml.bind.annotation.XmlRootElement;

import org.mongodb.morphia.annotations.Entity;

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
