package com.pp.database.model.scrapper.descriptor.relation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;

@JsonTypeInfo(use  =Id.NAME,include= As.PROPERTY, property="type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AggregationRelation.class, name = "aggregationRelation"),
    @JsonSubTypes.Type(value = CompositionRelation.class, name = "compositionRelation"),
})
public abstract class SemanticRelation extends ContentListenersRelation{

	protected CardinalityType cardinalityType;
	
	public SemanticRelation(){}
	
	public SemanticRelation(ContentListenerModel source,ContentListenerModel target,CardinalityType cardinalityType){
		super(source,target);
		this.cardinalityType = cardinalityType;
	}
	
	
	public enum CardinalityType{
		ONE_TO_MANY,
		MANY_TO_MANY,
		MANY_TO_ONE,
		ONE_TO_ONE,
	}


	public CardinalityType getCardinalityType() {
		return cardinalityType;
	}

	public void setCardinalityType(CardinalityType cardinalityType) {
		this.cardinalityType = cardinalityType;
	}
}
