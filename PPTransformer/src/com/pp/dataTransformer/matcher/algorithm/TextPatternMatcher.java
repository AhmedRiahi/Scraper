package com.pp.dataTransformer.matcher.algorithm;

import com.pp.dataTransformer.matcher.pattern.MatchingPattern;
import com.pp.dataTransformer.matcher.pattern.MatchingRule;
import com.pp.dataTransformer.matcher.pattern.MatchingRuleTypes;
import com.pp.dataTransformer.matcher.pattern.TextPattern;
import com.pp.dataTransformer.record.RecordAttribute;
import com.pp.dataTransformer.record.RecordValue;
import com.pp.dataTransformer.record.RecordValues;
import com.pp.dataTransformer.record.RecordsSet;

import java.util.List;
import java.util.Map.Entry;

public class TextPatternMatcher extends PatternMatcher{


	@Override
	public RecordsSet match(MatchingPattern pattern, Object loadedData) {
		RecordsSet records = pattern.prepareRecordsSet();
		pattern = (TextPattern) pattern;
		MatchingRule recordsDeliminerRule = pattern.getFileStructreRuleByType(MatchingRuleTypes.RECORD_DELIMINER);
		if( recordsDeliminerRule == null ){
			// throw exception here
		}
		// we could have many line split rules ... ;)
		
		String[] splittedStringRecords = (String[]) this.matchRule(recordsDeliminerRule, loadedData);
		for(String stringRecord : splittedStringRecords){
			// Process attribute rules one by one
			
			RecordValues recordsValues = new RecordValues();
			for(Entry<RecordAttribute,List<MatchingRule>> entry : pattern.getAttributeRules().entrySet()){
				String stringAttribute = stringRecord;
				for(MatchingRule rule : entry.getValue()){
					stringAttribute = (String) this.matchRule(rule, stringAttribute);
				}
				recordsValues.add(new RecordValue<>(stringAttribute));
			}
			records.add(recordsValues);
		}
		
		return records;
	}

}
