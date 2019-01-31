package com.pp.dataTransformer.matcher.algorithm;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.pp.dataTransformer.matcher.pattern.MatchingPattern;
import com.pp.dataTransformer.matcher.pattern.MatchingRule;
import com.pp.dataTransformer.record.RecordsSet;

public abstract class PatternMatcher {

	public abstract RecordsSet match(MatchingPattern pattern,Object loadedData);

	public Object matchRule(MatchingRule rule,Object data){
		
		switch (rule.getType()) {
		case RECORD_ATTRIBUTES_DELIMINER:
		case RECORD_DELIMINER:
		{
			String text = (String) data;
			return text.split((String) rule.getValue());
		}
			
		case TEXT_END_WITH:
		{
			String text = (String) data;
			String[] splittedText = text.split(Pattern.quote((String) rule.getValue()));
			List<String> list = Arrays.asList(splittedText);
			return list.get(0);
		}
		case TEXT_START_WITH:
		{
			String text = (String) data;
			String[] splittedText = text.split(Pattern.quote((String) rule.getValue()));
			List<String> list = Arrays.asList(splittedText);
			return list.get(1);
		}
		default:
			break;
		
		}
		
		return null;
	}
}
