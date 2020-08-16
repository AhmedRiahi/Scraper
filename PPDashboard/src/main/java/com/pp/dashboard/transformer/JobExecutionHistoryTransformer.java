package com.pp.dashboard.transformer;

import com.pp.dashboard.payload.JobExecutionHistoryPayload;
import com.pp.database.model.mozart.JobExecutionHistory;

public class JobExecutionHistoryTransformer {


    public static JobExecutionHistoryPayload toPayload(JobExecutionHistory jobExecutionHistory){
        JobExecutionHistoryPayload jobExecutionHistoryPayload = new JobExecutionHistoryPayload();
        jobExecutionHistoryPayload.setStringId(jobExecutionHistory.getStringId());
        jobExecutionHistoryPayload.setDescriptorName(jobExecutionHistory.getDescriptorJob().getName());
        jobExecutionHistoryPayload.setStartTime(jobExecutionHistory.getStartTime());
        jobExecutionHistoryPayload.setFinishTime(jobExecutionHistory.getFinishTime());
        jobExecutionHistoryPayload.setInError(jobExecutionHistory.isInError());
        jobExecutionHistoryPayload.setDwdpStringId(jobExecutionHistory.getDwdp().getStringId());
        jobExecutionHistoryPayload.setLastExecutedStep(jobExecutionHistory.getDwdp().getDebugInformation().getMozartExecutionStep());
        jobExecutionHistoryPayload.setExecutionException(jobExecutionHistory.getDwdp().getDebugInformation().getException());

        return jobExecutionHistoryPayload;
    }
}
