package com.pp.database.model.semantic.individual;

import com.pp.database.kernel.PPEntity;
import lombok.Data;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

@Data
@Entity
public class IndividualProperty extends PPEntity{

	private String name;
	private String value;
	@Embedded
	private IndividualReferenceData referenceData;
}
