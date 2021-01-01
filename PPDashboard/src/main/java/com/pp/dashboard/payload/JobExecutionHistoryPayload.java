package com.pp.dashboard.payload;

import lombok.Data;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class JobExecutionHistoryPayload {

    private String stringId;
    private String descriptorName;
    private Date startTime;
    private Date finishTime;
    private boolean isError;
    private String dwdpStringId;
    private String lastExecutedStep;
    private String executionException;
    private int cleanIndividualsCount;
    private List<JobExecutionHistoryPayload> joinerJobs = new ArrayList<>();
}
