package com.pp.database.kernel;

import java.util.List;

import org.mongodb.morphia.annotations.Entity;

@Entity
public class PPModel extends PPEntity{

	
	List<PPRelation> relations;

	// -------------------------------- GETTER / SETTER --------------------------------
	
	public List<PPRelation> getRelations() {
		return relations;
	}

	public void setRelations(List<PPRelation> relations) {
		this.relations = relations;
	}
}
