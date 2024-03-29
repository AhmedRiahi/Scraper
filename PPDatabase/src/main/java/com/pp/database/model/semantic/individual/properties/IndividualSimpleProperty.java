package com.pp.database.model.semantic.individual.properties;

import com.pp.database.kernel.PPEntity;
import lombok.Data;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

@Data
@Entity
public class IndividualSimpleProperty extends IndividualBaseProperty{

	private String value;
	@Embedded
	private IndividualReferenceData referenceData;
}
