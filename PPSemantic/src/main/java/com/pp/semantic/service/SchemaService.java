package com.pp.semantic.service;

import com.pp.database.dao.semantic.PPIndividualSchemaDAO;
import com.pp.database.model.semantic.schema.IndividualSchema;
import com.pp.semantic.core.JSONSchemaBuilder;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@Scope("singleton")
public class SchemaService {
	
	private Map<String,IndividualSchema> individualSchemas;
	private Map<String,List<IndividualSchema>> individualSchemasChilds;
	private JSONParser jsonParser;
	
	@Autowired
	private PPIndividualSchemaDAO individualSchemaDAO;
	
	@Autowired
	private JSONSchemaBuilder jsonSchemaBuilder;
	
	
	public SchemaService(){
		this.individualSchemas = new HashMap<>();
		this.jsonParser = new JSONParser();
	}
	
	public IndividualSchema getIndividualSchema(String schemaName){
		return this.individualSchemas.get(schemaName);
	}
	

	public Map<String, IndividualSchema> getAllSchemas(){
		return this.individualSchemas;
	}

	
	public Map<String, List<IndividualSchema>> getAllSchemasChilds(){
		return this.individualSchemasChilds;
	}
	
	public void addSchema(IndividualSchema individualSchema) throws ParseException {
		this.individualSchemaDAO.save(individualSchema);
		this.reloadSchemas();
	}

	public void deleteSchema(String schemaId) throws ParseException {
	    this.individualSchemaDAO.delete(schemaId);
		this.reloadSchemas();
	}
	
	public IndividualSchema getSchema(String name) {
		return this.individualSchemas.get(name);
	}
	
	public List<IndividualSchema> getSchemaChilds(String name) {
		return this.individualSchemasChilds.get(name);
	}
	
	public synchronized void reloadSchemas() {
		this.individualSchemas = new ConcurrentHashMap<>();
		individualSchemasChilds = new ConcurrentHashMap<>();
		List<IndividualSchema> schemas = this.individualSchemaDAO.find().asList();
		for(IndividualSchema schema : schemas) {
			this.individualSchemas.put(schema.getName(), schema);
			
			// Build childs Schemas
			if(schema.getParent() != null) {
				List<IndividualSchema> childs = this.individualSchemasChilds.get(schema.getParent().getName());
				if(childs == null) {
					childs = new ArrayList<>();
					this.individualSchemasChilds.put(schema.getParent().getName(),childs);
				}
				childs.add(schema);
			}
		}
	}
	
	@PostConstruct
	public void init() {
		this.reloadSchemas();
	}
	
	
}
