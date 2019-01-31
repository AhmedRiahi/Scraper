package com.pp.database.model.scrapper.descriptor.relation;

import org.mongodb.morphia.annotations.Entity;

import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;

@Entity
public class AggregationRelation extends SemanticRelation{

	private String relationName;
	
	public AggregationRelation(){
		super();
	}
	
	public AggregationRelation(String relationName,ContentListenerModel source,ContentListenerModel target,CardinalityType cardinalityType){
		super(source,target,cardinalityType);
		this.relationName = relationName;
	}
	
	// -------------------------------- GETTER / SETTER --------------------------------

	public String getRelationName() {
		return relationName;
	}

	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}
	
}
