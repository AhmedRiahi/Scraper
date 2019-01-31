package com.pp.semantic.controller;

import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.pp.database.model.semantic.schema.IndividualSchema;
import com.pp.semantic.service.SchemaService;

@CrossOrigin
@RestController
@RequestMapping("/schema")
public class SchemaController {

	
	@Autowired
	private SchemaService schemaService;
	
	@RequestMapping(path="/addSchema",method=RequestMethod.POST)
	public void addSchema(@RequestBody IndividualSchema individualSchema) throws ParseException{
		this.schemaService.addSchema(individualSchema);
	}

	@RequestMapping(path="/deleteSchema/{schemaId}",method=RequestMethod.POST)
	public void deleteSchema(@PathVariable String schemaId) throws ParseException{
		this.schemaService.deleteSchema(schemaId);
	}
	
	@RequestMapping("/getAll")
	public Map<String, IndividualSchema> getAllSchemas() {
		return this.schemaService.getAllSchemas();
	}
	
	@RequestMapping("/getAllChilds")
	public Map<String, List<IndividualSchema>> getAllSchemasChilds() {
		return this.schemaService.getAllSchemasChilds();
	}
	
	@RequestMapping("/get/{schemaName}")
	public IndividualSchema getSchema(@PathVariable String schemaName) {
		return this.schemaService.getSchema(schemaName);
	}
	
	@RequestMapping("/getChilds/{schemaName}")
	public List<IndividualSchema> getSchemaChilds(@PathVariable String schemaName) {
		return this.schemaService.getSchemaChilds(schemaName);
	}
	
	
	
	
}
