package com.pp.engine.service;

import com.pp.database.dao.common.DescriptorsPortfolioDAO;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.model.common.DescriptorsPortfolio;
import com.pp.database.model.engine.DescriptorJob;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.framework.kafka.KafkaTopics;
import com.pp.framework.kafka.sender.PPSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class EngineService {

    private static final Logger log = LoggerFactory.getLogger(EngineService.class);

    @Autowired
    private DescriptorsPortfolioDAO descriptorsPortfolioDAO;
    @Autowired
    private DescriptorWorkflowDataPackageDAO dwdpDao;
    @Autowired
    private PPSender sender;

    public void checkScheduledJobs(){
        List<DescriptorsPortfolio> portfolios = this.descriptorsPortfolioDAO.find().asList();
        log.info("Found {} portfolios to be processed.",portfolios.size());
        portfolios.stream().forEach(portfolio -> {
            portfolio.getJobs().stream().forEach(job -> {
                if(!job.isDisabled()){
                    if(job.getLastCheckingDate() != null){
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(job.getLastCheckingDate());
                        calendar.add(Calendar.MILLISECOND, job.getCheckingInterval() * 60 *1000);
                        Date nextCheckingDate = calendar.getTime();
                        if(!job.isCheckingRequired() && (new Date().after(nextCheckingDate))){
                            if(!job.isDynamicURLJob()){
                                this.launchPortfolioJobWorkflowProcess(portfolio,job);
                            }else{
                                this.launchPortfolioDynamicJobWorkflowProcess(portfolio,job);
                            }

                        }else{
                            log.info("Job is already checked or checking is required : "+job.getDescriptor().getName()+" "+job.getName());
                            log.info("Is checking required :"+job.isCheckingRequired());
                            log.info(nextCheckingDate+" vs "+new Date());
                        }
                    }else{
                        // This job is never launched
                        this.launchPortfolioJobWorkflowProcess(portfolio,job);
                    }
                }else{
                    log.info("Job is disabled :"+job.getName());
                }
            });
        });
    }

    public void launchPortfolioJobWorkflowProcess(DescriptorsPortfolio portfolio, DescriptorJob job){
        log.info("processDescriptorWorkflow porfolio = {}, job = {}",portfolio.getName(),job.getName());
        // Prepare Data package
        DescriptorWorkflowDataPackage dwdp = new DescriptorWorkflowDataPackage();
        dwdp.setPortfolio(portfolio);
        dwdp.setDescriptorJob(job);
        dwdp.getDebugInformation().setMozartExecutionStep("Engine Prepare Package");
        this.dwdpDao.save(dwdp);
        //Trigger Mozart
        this.sender.send(KafkaTopics.Mozart.PROCESS_DESCRIPTOR, dwdp.getId().toHexString());
    }

    public void launchPortfolioDynamicJobWorkflowProcess(DescriptorsPortfolio portfolio, DescriptorJob job){
        log.info("launchPortfolioDynamicJobWorkflowProcess porfolio = {}, job = {}",portfolio.getName(),job.getName());
        job.getToBeProcessedLinks().stream().forEach(link -> {
            // Prepare Data package
            DescriptorWorkflowDataPackage dwdp = new DescriptorWorkflowDataPackage();
            job.setUrl(link);
            dwdp.setPortfolio(portfolio);
            dwdp.setDescriptorJob(job);
            dwdp.getDebugInformation().setMozartExecutionStep("Engine Prepare Package");
            this.dwdpDao.save(dwdp);
            //Trigger Mozart
            this.sender.send(KafkaTopics.Mozart.PROCESS_DESCRIPTOR, dwdp.getId().toHexString());
        });
    }

    public void launchPortfolioJobWorkflowProcess(String portfolioJob){
        String portfolioId = portfolioJob.split("\\.")[0];
        String jobName = portfolioJob.split("\\.")[1];
        DescriptorsPortfolio portfolio = this.descriptorsPortfolioDAO.get(portfolioId);
        DescriptorJob job = portfolio.getJobByName(jobName).get();
        this.launchPortfolioJobWorkflowProcess(portfolio,job);

    }
}
