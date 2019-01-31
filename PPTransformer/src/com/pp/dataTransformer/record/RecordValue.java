package com.pp.dataTransformer.record;

import java.io.Serializable;

public class RecordValue<T> implements Serializable{

	protected Object value;
	
	public RecordValue(Object value){
		this.value = value;
	}
	
	/**************************---- GETTERS/SETTERS ----***********************/
	
	public Object getOriginalObject(){
		return this.value;
	}
	
	@Override
	public String toString() {
		return this.value.toString();
	}
	
}
