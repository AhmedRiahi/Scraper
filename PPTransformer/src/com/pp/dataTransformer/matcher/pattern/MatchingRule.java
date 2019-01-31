package com.pp.dataTransformer.matcher.pattern;

public class MatchingRule {

	private MatchingRuleTypes type;
	private Object value;
	
	
	public MatchingRule(MatchingRuleTypes type,Object value) {
		this.type = type;
		this.value = value;
	}
	
	/**************************---- GETTERS/SETTERS ----***********************/
	
	public MatchingRuleTypes getType() {
		return type;
	}
	
	public void setType(MatchingRuleTypes type) {
		this.type = type;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}

}
