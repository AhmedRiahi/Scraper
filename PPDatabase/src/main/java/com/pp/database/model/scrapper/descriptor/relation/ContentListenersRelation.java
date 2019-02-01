package com.pp.database.model.scrapper.descriptor.relation;

import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import lombok.Data;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

@Entity
@Data
public abstract class ContentListenersRelation {

	@Reference
	private ContentListenerModel source;
	@Reference
	private ContentListenerModel target;
	
	public ContentListenersRelation(){}
	
	public ContentListenersRelation(ContentListenerModel source,ContentListenerModel target){
		this();
		this.source = source;
		this.target = target;
	}
	
	
	public boolean doConcerns(ContentListenerModel cl){
		return this.source.equals(cl) || this.target.equals(cl);
	}

}
