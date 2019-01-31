package com.pp.database.model.scrapper.descriptor.relation;

import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;

public class CompositionRelation extends SemanticRelation{

	
	public CompositionRelation(){
		super();
	}
	
	public CompositionRelation(ContentListenerModel source,ContentListenerModel target,CardinalityType cardinalityType){
		super(source,target,cardinalityType);
	}

}
