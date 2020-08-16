package com.pp.dashboard.payload;

import lombok.Data;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
public class JobExecutionHistoryPayload {

    private String stringId;
    private String descriptorName;
    private Date startTime;
    private Date finishTime;
    private boolean inError;
    private String dwdpStringId;
    private String lastExecutedStep;
    private String executionException;
}
