package com.pp.database.model.subscription;

import com.mongodb.BasicDBObject;
import com.pp.database.kernel.PPEntity;
import com.pp.database.model.semantic.schema.IndividualSchema;
import lombok.Data;
import org.mongodb.morphia.annotations.Reference;

import java.util.ArrayList;
import java.util.List;


@Data
public class SchemaSubscription extends PPEntity{

	@Reference
	private IndividualSchema schema;
	private BasicDBObject subscriptionFilter;

}
