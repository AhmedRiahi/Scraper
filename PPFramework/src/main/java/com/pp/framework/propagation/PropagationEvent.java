package com.pp.framework.propagation;

public class PropagationEvent {

	
	protected int id;
	
	public PropagationEvent(int id){
		this.id = id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}
	
	@Override
	public boolean equals(Object obj) {
		PropagationEvent tmp = (PropagationEvent)obj;
		return tmp.id == this.id;
	};
	
	// -------------------------------- GETTER / SETTER --------------------------------
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
