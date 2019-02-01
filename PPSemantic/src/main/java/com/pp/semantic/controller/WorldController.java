package com.pp.semantic.controller;

import com.mongodb.DBObject;
import com.pp.database.model.semantic.individual.PPIndividual;
import com.pp.semantic.service.PPSemanticWorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/world")
public class WorldController {

	@Autowired
	private PPSemanticWorldService semanticWorldService;

	
	@RequestMapping(path="/addIndividual",method=RequestMethod.POST)
	public void addIndividual(@RequestBody PPIndividual individual) {
		this.semanticWorldService.addIndividual(individual);
	}
	
	
	@RequestMapping(path="/deleteIndividual/{schemaName}/{id}",method=RequestMethod.DELETE)
	public void deleteSchemaIndividual(@PathVariable String schemaName,@PathVariable String id) {
		this.semanticWorldService.deleteIndividual(schemaName,id);
	}
	
	@RequestMapping("/getSchemaIndividuals/{schemaName}")
	public List<DBObject> getSchemaIndividuals(@PathVariable String schemaName){
		return this.semanticWorldService.getSchemaIndividuals(schemaName);
	}
	
	@RequestMapping("/getAllSchemaIndividuals/{schemaName}")
	public List<DBObject> getAllSchemaIndividuals(@PathVariable String schemaName){
		return this.semanticWorldService.getAllSchemaIndividuals(schemaName);
	}

    @RequestMapping("/reloadSchemas")
	public void reloadSchemas(){
        this.semanticWorldService.reloadSchemas();
	}
	
}
