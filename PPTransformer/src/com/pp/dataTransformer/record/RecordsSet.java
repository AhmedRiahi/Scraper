package com.pp.dataTransformer.record;

import java.util.ArrayList;
import java.util.List;

public class RecordsSet extends ArrayList<RecordValues>{
	
	protected List<RecordAttribute> attributes;

	
	
	public RecordsSet() {
		this.attributes = new ArrayList<>();
	}
	
	public void addAttribute(RecordAttribute attribute){
		this.attributes.add(attribute);
	}
	
	/**************************---- GETTERS/SETTERS ----***********************/

	public List<RecordAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<RecordAttribute> attributes) {
		this.attributes = attributes;
	}

}
