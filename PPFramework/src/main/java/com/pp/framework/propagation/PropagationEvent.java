package com.pp.framework.propagation;

import lombok.Data;

@Data
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
		if(obj != null && obj instanceof PropagationEvent){
			PropagationEvent tmp = (PropagationEvent)obj;
			return tmp.id == this.id;
		}
		return false;
	}
}
