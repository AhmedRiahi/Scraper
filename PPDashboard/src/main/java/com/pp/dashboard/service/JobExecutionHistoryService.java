package com.pp.dashboard.service;


import com.pp.database.dao.mozart.JobExecutionHistoryDAO;
import com.pp.database.model.mozart.JobExecutionHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Service
public class JobExecutionHistoryService {

    @Autowired
    private JobExecutionHistoryDAO jobExecutionHistoryDAO;

    public List<JobExecutionHistory> getPortfolioExecutionHistory(String portfolioId){
        return this.jobExecutionHistoryDAO.getByPortfolioId(portfolioId);
    }

    public JobExecutionHistory get(@PathVariable String jobId){
        return this.jobExecutionHistoryDAO.get(jobId);
    }

    public List<JobExecutionHistory> getInErrorJobs(){
        List<JobExecutionHistory> historyJobs = this.jobExecutionHistoryDAO.getInError();
        historyJobs.stream().forEach(job -> {
            job.setDwdp(null);
            job.setPortfolio(null);
        });
        return historyJobs;
    }

    public List<JobExecutionHistory> getActiveJobs(){
        List<JobExecutionHistory> historyJobs = this.jobExecutionHistoryDAO.getActiveJobs();
        historyJobs.stream().forEach(job -> {
            job.setDwdp(null);
            job.setPortfolio(null);
        });
        return historyJobs;
    }

}
