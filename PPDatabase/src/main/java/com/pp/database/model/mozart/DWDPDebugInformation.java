package com.pp.database.model.mozart;


import lombok.Data;
import org.mongodb.morphia.annotations.Entity;

@Data
@Entity
public class DWDPDebugInformation{

    private String mozartExecutionStep;
    private int cleanIndividualsCount;
    private String exception;
}
