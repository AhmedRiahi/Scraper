package com.pp.dashboard.transformer;

import com.pp.dashboard.payload.JobExecutionHistoryPayload;
import com.pp.database.model.mozart.JobExecutionHistory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobExecutionHistoryTransformer {


    public static JobExecutionHistoryPayload toPayload(JobExecutionHistory jobExecutionHistory) {
        JobExecutionHistoryPayload jobExecutionHistoryPayload = new JobExecutionHistoryPayload();
        try {
            jobExecutionHistoryPayload.setStringId(jobExecutionHistory.getStringId());
            jobExecutionHistoryPayload.setDescriptorName(jobExecutionHistory.getDescriptorJob().getName());
            jobExecutionHistoryPayload.setStartTime(jobExecutionHistory.getStartTime());
            jobExecutionHistoryPayload.setFinishTime(jobExecutionHistory.getFinishTime());
            jobExecutionHistoryPayload.setError(jobExecutionHistory.isError());
            jobExecutionHistoryPayload.setDwdpStringId(jobExecutionHistory.getDwdp().getStringId());
            jobExecutionHistoryPayload.setLastExecutedStep(jobExecutionHistory.getDwdp().getDebugInformation().getMozartExecutionStep());
            jobExecutionHistoryPayload.setLastExecutedStep(jobExecutionHistory.getDwdp().getDebugInformation().getMozartExecutionStep());
            jobExecutionHistoryPayload.setCleanIndividualsCount(jobExecutionHistory.getDwdp().getDebugInformation().getCleanIndividualsCount());
            jobExecutionHistoryPayload.setExecutionException(jobExecutionHistory.getDwdp().getDebugInformation().getException());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return jobExecutionHistoryPayload;
    }
}
