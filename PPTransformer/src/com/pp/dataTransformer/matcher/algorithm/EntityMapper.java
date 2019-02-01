package com.pp.dataTransformer.matcher.algorithm;

import com.github.drapostolos.typeparser.TypeParser;
import com.pp.dataTransformer.record.RecordAttribute;
import com.pp.dataTransformer.record.RecordValue;
import com.pp.dataTransformer.record.RecordValues;
import com.pp.dataTransformer.record.RecordsSet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EntityMapper<T> {
	
	private Class<T> entityClass;
	private TypeParser typeParser;
	
	public EntityMapper(Class<T> clazz){
		entityClass = clazz;
		this.typeParser = TypeParser.newBuilder().build();
	}
	
	public List<T> map(RecordsSet records) throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException{
		List<T> list = new ArrayList<>();
		for(RecordValues values : records){
			list.add(this.map(values,records.getAttributes()));
		}
		return list;
	}
	
	public T map(RecordValues recordValues,List<RecordAttribute> attributes) throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException{
		Map<String,RecordValue<?>> attributesMapping = new HashMap<>();
		for(RecordAttribute attribute : attributes){
			attributesMapping.put(attribute.getName(),recordValues.get(attribute.getOrder()));
		}
		return this.createEntity(attributesMapping);
	}
	
	public List<T> map(List<Map<String,RecordValue<?>>> attributesList) throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException{
		List<T> entities = new ArrayList<T>();
		for(Map<String,RecordValue<?>> attributes : attributesList){
			entities.add(this.map(attributes));
		}
		return entities;
	}
	
	public T map(Map<String,RecordValue<?>> attributes) throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException{
		return this.createEntity(attributes);
	}

	public T createEntity(Map<String,RecordValue<?>> attributes) throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException{
		T entity = this.entityClass.newInstance();
		for(Entry<String,RecordValue<?>> entry : attributes.entrySet()){
			Field field = entity.getClass().getDeclaredField(entry.getKey());
			field.setAccessible(true);
			field.set(entity,this.typeParser.parse(entry.getValue().toString(),field.getType()));
		}
		return entity;
	}

	/**************************---- GETTERS/SETTERS ----***********************/
	
	public Class<T> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class<T> entityClass) {
		this.entityClass = entityClass;
	}
}
