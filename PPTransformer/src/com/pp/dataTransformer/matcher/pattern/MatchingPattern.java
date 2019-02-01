package com.pp.dataTransformer.matcher.pattern;

import com.pp.dataTransformer.record.RecordAttribute;
import com.pp.dataTransformer.record.RecordsSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public abstract class MatchingPattern {

	protected Map<RecordAttribute,List<MatchingRule>> attributeRules;
	protected List<MatchingRule> fileStructureRules;
	
	
	public MatchingPattern(){
		this.attributeRules = new TreeMap<>();
		this.fileStructureRules = new ArrayList<>();
	}
	
	public MatchingRule getFileStructreRuleByType(MatchingRuleTypes type){
		for(MatchingRule matchingRule : this.fileStructureRules){
			if(matchingRule.getType() == type){
				return matchingRule;
			}
		}
		return null;
	}
	
	public void addFileStructureMatchingRule(MatchingRule rule){
		this.fileStructureRules.add(rule);
	}
	
	public void addAttributeMatchingRule(RecordAttribute attribute,MatchingRule rule){
		List<MatchingRule> rules = this.attributeRules.get(attribute);
		if(rules == null){
			rules = new ArrayList<>();
		}
		rules.add(rule);
		this.attributeRules.put(attribute,rules);
	}
	
	
	public RecordsSet prepareRecordsSet(){
		RecordsSet records = new RecordsSet();
		for(Entry<RecordAttribute,List<MatchingRule>> entry : this.attributeRules.entrySet()){
			records.addAttribute(entry.getKey());
		}
		
		return records;
	}
	
	/**************************---- GETTERS/SETTERS ----***********************/
	
	public Map<RecordAttribute, List<MatchingRule>> getAttributeRules() {
		return attributeRules;
	}
	
	public void setAttributeRules(Map<RecordAttribute, List<MatchingRule>> attributeRules) {
		this.attributeRules = attributeRules;
	}
	
	public List<MatchingRule> getFileStructureRules() {
		return fileStructureRules;
	}
	
	public void setFileStructureRules(List<MatchingRule> fileStructureRules) {
		this.fileStructureRules = fileStructureRules;
	}
}
