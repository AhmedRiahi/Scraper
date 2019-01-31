package com.pp.database.dao.semantic;

import org.springframework.stereotype.Repository;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.semantic.schema.IndividualSchema;

@Repository
public class PPIndividualSchemaDAO extends PPDAO<IndividualSchema>{

	public PPIndividualSchemaDAO(Class<IndividualSchema> entityClass) {
		super(entityClass);
	}

	public PPIndividualSchemaDAO() {
		super(IndividualSchema.class);
	}
	
	public IndividualSchema findByName(String schemaName) {
		return this.createQuery().field("name").equal(schemaName).get();
	}

}
