package com.pp.database.model.semantic.individual;

import com.pp.database.kernel.PPEntity;
import lombok.Data;

@Data
public class IndividualProperty extends PPEntity{

	private String name;
	private String value;
}
