package main;

import java.util.List;

import com.pp.dataTransformer.Transformer;
import com.pp.dataTransformer.exception.DataLoadingException;
import com.pp.dataTransformer.matcher.algorithm.TextPatternMatcher;
import com.pp.dataTransformer.matcher.pattern.MatchingRule;
import com.pp.dataTransformer.matcher.pattern.MatchingRuleTypes;
import com.pp.dataTransformer.matcher.pattern.TextPattern;
import com.pp.dataTransformer.record.RecordAttribute;
import com.pp.dataTransformer.record.RecordsSet;

public class Main {

	public static void main(String[] args){
		TextPattern pattern = new TextPattern();
		pattern.addFileStructureMatchingRule(new MatchingRule(MatchingRuleTypes.RECORD_DELIMINER,"\n"));
		
		RecordAttribute attr1 = new RecordAttribute(0,"id");
		RecordAttribute attr2 = new RecordAttribute(1,"name");
		
		pattern.addAttributeMatchingRule(attr1,new MatchingRule(MatchingRuleTypes.TEXT_START_WITH,"["));
		pattern.addAttributeMatchingRule(attr1,new MatchingRule(MatchingRuleTypes.TEXT_END_WITH,"]"));
		
		pattern.addAttributeMatchingRule(attr2,new MatchingRule(MatchingRuleTypes.TEXT_START_WITH,"{"));
		pattern.addAttributeMatchingRule(attr2,new MatchingRule(MatchingRuleTypes.TEXT_END_WITH,"}"));
		
		TextPatternMatcher matcher = new TextPatternMatcher();
		RecordsSet records = matcher.match(pattern, "[4] kjrge{me}");
		
		try {
			List<Student> list = Transformer.<Student>transforToEntities(records, Student.class);
			for(Student st : list){
				System.out.println(st);
			}
		} catch (DataLoadingException e) {
			e.printStackTrace();
		}
		
		
		
		
		/*SourceProvider inputConfig = new SourceProvider();
		inputConfig.setConnectionType(SupportedConnections.SQL);
		inputConfig.addParameter(ConfigParameters.DATABASE_DRIVER		, "com.mysql.jdbc.Driver");
		inputConfig.addParameter(ConfigParameters.DATABASE_URL			, "jdbc:mysql://localhost:3306/test");
		inputConfig.addParameter(ConfigParameters.DATABASE_USER			, "");
		inputConfig.addParameter(ConfigParameters.DATABASE_PASSWORD		, "");
		inputConfig.addParameter(ConfigParameters.DATABASE_QUERY		, "select id,name from test");
		inputConfig.addMappingAttribute(0,"id");
		inputConfig.addMappingAttribute(1,"name");
		
		SourceProvider outputConfig = new SourceProvider();
		outputConfig.setConnectionType(SupportedConnections.SYSTEM_FILE);
		outputConfig.addParameter(ConfigParameters.FILE_PATH					, "C:\\Users\\Camirra\\Desktop\\test.txt");
		outputConfig.addParameter(ConfigParameters.FILE_ATTRIBUTE_DELIMINER		, ";");
		outputConfig.addParameter(ConfigParameters.FILE_LINE_DELIMINER			, "\n");
	
		
		SourceProvider outputConfig2 = new SourceProvider();
		outputConfig2.setConnectionType(SupportedConnections.SQL);
		outputConfig2.addParameter(ConfigParameters.DATABASE_DRIVER			, "com.mysql.jdbc.Driver");
		outputConfig2.addParameter(ConfigParameters.DATABASE_URL			, "jdbc:mysql://localhost:3306/test");
		outputConfig2.addParameter(ConfigParameters.DATABASE_USER			, "");
		outputConfig2.addParameter(ConfigParameters.DATABASE_PASSWORD		, "");
		outputConfig2.addParameter(ConfigParameters.DATABASE_QUERY			, "INSERT INTO TEST(id,name) VALUES(?,?)");
		
		
		SourceProvider inputConfig2 = new SourceProvider();
		inputConfig2.setConnectionType(SupportedConnections.SYSTEM_FILE);
		inputConfig2.addParameter(ConfigParameters.FILE_PATH					, "C:\\Users\\Camirra\\Desktop\\test.txt");
		inputConfig2.addParameter(ConfigParameters.FILE_ATTRIBUTE_DELIMINER		, ";");
		inputConfig2.addParameter(ConfigParameters.FILE_LINE_DELIMINER			, "\n");
		inputConfig2.addMappingAttribute(0,"id");
		inputConfig2.addMappingAttribute(1,"name");
		
		try {
			Transformer transformer = new Transformer();
			transformer.setInputConfigurationModel(inputConfig);
			transformer.setOutputConfigurationModel(outputConfig);
			
			List<Student> list = transformer.<Student>transformToEntities(Student.class);
			for(Student st : list){
				System.out.println(st);
			}
			
			transformer.transform();
			
		} catch (DataLoadingException e) {
			e.printStackTrace();
		} catch (ConfigurationAttributeException e) {
			e.printStackTrace();
		} catch (DataWritingException e) {
			e.printStackTrace();
		}*/
		
	}
}
