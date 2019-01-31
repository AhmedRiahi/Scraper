package com.pp.database.model.scrapper.descriptor.relation;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;

@Entity
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
		if(this.source.equals(cl) || this.target.equals(cl)) return true;
		return false;
	}
	
	// -------------------------------- GETTER / SETTER --------------------------------

	public ContentListenerModel getSource() {
		return source;
	}

	public void setSource(ContentListenerModel source) {
		this.source = source;
	}

	public ContentListenerModel getTarget() {
		return target;
	}

	public void setTarget(ContentListenerModel target) {
		this.target = target;
	}

}
