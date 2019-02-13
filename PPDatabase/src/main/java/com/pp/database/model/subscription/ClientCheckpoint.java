package com.pp.database.model.subscription;

import com.pp.database.kernel.PPEntity;

import lombok.Data;
import org.mongodb.morphia.annotations.Reference;

import java.util.Date;

@Data
public class ClientCheckpoint extends PPEntity {

    private String clientId;
    private Date checkingDate;
    @Reference
    private SchemaSubscription schemaSubscription;
}
