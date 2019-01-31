package com.pp.dataTransformer.record;

public class RecordAttribute implements Comparable<RecordAttribute>{

	protected String name;
	protected int order;
	
	public RecordAttribute(int order,String name) {
		this.order = order;
		this.name = name;
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.name == ((RecordAttribute)obj).name;
	}
	
	@Override
	public int compareTo(RecordAttribute o) {
		if(this.order < o.order){
			return -1;
		}else{
			if(this.order > o.order){
				return 1;
			}
			return 0;
		}
	}

	/**************************---- GETTERS/SETTERS ----***********************/
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
	
}
