package com.pp.database.model.scrapper.descriptor.relation;

import org.mongodb.morphia.annotations.Entity;

import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;

//TODO : missing sibling classes : contained in , between, upper to ....
@Entity
public class StructureRelation extends ContentListenersRelation{

	public StructureRelation(){}
	
	public StructureRelation(ContentListenerModel source,ContentListenerModel target){
		super(source,target);
	}
}
