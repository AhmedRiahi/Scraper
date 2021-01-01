package com.pp.dashboard.service;


import com.pp.dashboard.payload.JobExecutionHistoryPayload;
import com.pp.dashboard.transformer.JobExecutionHistoryTransformer;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.dao.mozart.JobExecutionHistoryDAO;
import com.pp.database.model.mozart.JobExecutionHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobExecutionHistoryService {

    @Autowired
    private JobExecutionHistoryDAO jobExecutionHistoryDAO;
    @Autowired
    private DescriptorWorkflowDataPackageDAO dwdpDAO;

    public List<JobExecutionHistoryPayload> getPortfolioExecutionHistory(String portfolioId){
        List<JobExecutionHistoryPayload> jobExecutionHistoryPayloads = new ArrayList<>();
        List<JobExecutionHistory> jobExecutionHistoryEntities = this.jobExecutionHistoryDAO.getByPortfolioId(portfolioId);
        jobExecutionHistoryEntities.stream().forEach(jobExecutionHistoryEntity -> {
            JobExecutionHistoryPayload jobExecutionHistoryPayload = JobExecutionHistoryTransformer.toPayload(jobExecutionHistoryEntity);
            List<JobExecutionHistory> joinerJobsEntities = this.jobExecutionHistoryDAO.getJoinerJobs(jobExecutionHistoryEntity.getDwdp().getStringId());
            jobExecutionHistoryPayload.setJoinerJobs(joinerJobsEntities.stream().map(JobExecutionHistoryTransformer::toPayload).collect(Collectors.toList()));
            jobExecutionHistoryPayloads.add(jobExecutionHistoryPayload);

        });
        return jobExecutionHistoryPayloads;
    }

    public JobExecutionHistory get(@PathVariable String jobId){
        return this.jobExecutionHistoryDAO.get(jobId);
    }

    public List<JobExecutionHistoryPayload> getInErrorJobs(){
        List<JobExecutionHistory> historyJobs = this.jobExecutionHistoryDAO.getInError();
        historyJobs.stream().forEach(job -> {
            job.setDwdp(null);
            job.setPortfolio(null);
        });
        return historyJobs.stream().map(JobExecutionHistoryTransformer::toPayload).collect(Collectors.toList());
    }

    public List<JobExecutionHistoryPayload> getActiveJobs(){
        List<JobExecutionHistory> historyJobs = this.jobExecutionHistoryDAO.getActiveJobs();
        historyJobs.stream().forEach(job -> {
            job.setDwdp(null);
            job.setPortfolio(null);
        });
        return historyJobs.stream().map(JobExecutionHistoryTransformer::toPayload).collect(Collectors.toList());
    }


}
