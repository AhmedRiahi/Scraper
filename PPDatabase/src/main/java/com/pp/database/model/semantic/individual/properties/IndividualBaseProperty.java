package com.pp.database.model.semantic.individual.properties;

import com.pp.database.kernel.PPEntity;
import lombok.Data;

@Data
public abstract class IndividualBaseProperty extends PPEntity {

    protected String name;
}
