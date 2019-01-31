package com.pp.dashboard.controller;


import com.pp.dashboard.service.JobExecutionHistoryService;
import com.pp.database.model.mozart.JobExecutionHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("jobExecutionHistory")
public class JobExecutionHistoryController {

    @Autowired
    private JobExecutionHistoryService jobExecutionHistoryService;

    @RequestMapping(path = "/portfolio/{portfolioId}",method = RequestMethod.GET)
    public List<JobExecutionHistory> getPortfolioExecutionHistory(@PathVariable String portfolioId){
        return this.jobExecutionHistoryService.getPortfolioExecutionHistory(portfolioId);
    }

    @RequestMapping(path = "/{jobId}",method = RequestMethod.GET)
    public JobExecutionHistory get(@PathVariable String jobId){
        return this.jobExecutionHistoryService.get(jobId);
    }

    @RequestMapping(path = "/inError", method = RequestMethod.GET)
    public List<JobExecutionHistory> getInErrorJobExecutionHistoryList(){
        return this.jobExecutionHistoryService.getInErrorJobs();
    }

    @RequestMapping(path = "/active", method = RequestMethod.GET)
    public List<JobExecutionHistory> getActiveJobExecutionHistoryList(){
        return this.jobExecutionHistoryService.getActiveJobs();
    }
}
