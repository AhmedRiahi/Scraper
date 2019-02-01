package com.pp.database.model.scrapper.descriptor.relation;

import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import org.mongodb.morphia.annotations.Entity;

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
