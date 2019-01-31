package com.pp.database.kernel;

import org.mongodb.morphia.annotations.Entity;


@Entity
public class PPRelation extends PPEntity{
	
	private PPModel source;
	private PPModel target;
	
	// -------------------------------- GETTER / SETTER --------------------------------
	
	public PPModel getSource() {
		return source;
	}
	public void setSource(PPModel source) {
		this.source = source;
	}
	public PPModel getTarget() {
		return target;
	}
	public void setTarget(PPModel target) {
		this.target = target;
	}
	
}
