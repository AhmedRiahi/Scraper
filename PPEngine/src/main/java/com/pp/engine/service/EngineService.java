package com.pp.engine.service;

import com.pp.database.dao.common.DescriptorsPortfolioDAO;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.model.common.DescriptorsPortfolio;
import com.pp.database.model.engine.DescriptorJob;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.framework.utils.URLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EngineService {

    private static final Logger log = LoggerFactory.getLogger(EngineService.class);

    @Autowired
    private DescriptorsPortfolioDAO descriptorsPortfolioDAO;
    @Autowired
    private DescriptorWorkflowDataPackageDAO dwdpDao;
    @Autowired
    private EngineJobScheduler engineJobScheduler;
    @Autowired
    private DescriptorJobUrlResolver descriptorJobUrlResolver;

    public synchronized void  checkScheduledJobs(){
        List<DescriptorsPortfolio> portfolios = this.descriptorsPortfolioDAO.find().asList();
        log.info("Found {} portfolios to be processed.",portfolios.size());
        portfolios.stream().forEach(portfolio ->
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
                        if(!job.isDynamicURLJob()){
                            this.launchPortfolioJobWorkflowProcess(portfolio,job);
                        }else{
                            this.launchPortfolioDynamicJobWorkflowProcess(portfolio,job);
                        }
                    }
                }else{
                    log.info("Job is disabled :"+job.getName());
                }
            })
        );
    }

    public void launchPortfolioJobWorkflowProcess(DescriptorsPortfolio portfolio, DescriptorJob job){
        log.info("processDescriptorWorkflow porfolio = {}, job = {}",portfolio.getName(),job.getName());
        List<String> jobURLs = this.descriptorJobUrlResolver.resolveJobURLs(job.getCrawlingParams());
        log.info("generated urls :");
        log.info(Arrays.toString(jobURLs.toArray()));
        long globalSleepTime = 100;
        for(String url : jobURLs){
            // Prepare Data package
            DescriptorWorkflowDataPackage dwdp = new DescriptorWorkflowDataPackage();
            dwdp.setPortfolio(portfolio);
            job.getCrawlingParams().setUrl(url);
            dwdp.setDescriptorJob(job);
            dwdp.getDebugInformation().setMozartExecutionStep("Engine Prepare Package");
            this.dwdpDao.save(dwdp);
            //Trigger Mozart
            this.engineJobScheduler.scheduleJob(dwdp,globalSleepTime);
            globalSleepTime+=job.getCrawlingParams().getSleepTime();
        }
    }

    public void launchPortfolioDynamicJobWorkflowProcess(DescriptorsPortfolio portfolio, DescriptorJob job){
        log.info("launchPortfolioDynamicJobWorkflowProcess porfolio = {}, job = {}",portfolio.getName(),job.getName());
        Iterator<String> iterator = job.getToBeProcessedLinks().iterator();
        if(iterator.hasNext()){
            String url = iterator.next();
            if(!URLUtils.isValidUrl(url)){
                url = job.getCrawlingParams().getBaseUrl()+url;
            }
            // Prepare Data package
            DescriptorWorkflowDataPackage dwdp = new DescriptorWorkflowDataPackage();
            job.getCrawlingParams().setUrl(url);
            dwdp.setPortfolio(portfolio);
            dwdp.setDescriptorJob(job);
            dwdp.getDebugInformation().setMozartExecutionStep("Engine Prepare Package");
            //Trigger MozartlaunchPortfolioDynamicJobWorkflowProcess
            iterator.remove();
            this.descriptorsPortfolioDAO.save(portfolio);
            this.dwdpDao.save(dwdp);
            this.engineJobScheduler.scheduleJob(dwdp,100);
        }
    }

    public void launchPortfolioJobWorkflowProcess(String portfolioJob){
        String portfolioId = portfolioJob.split("\\.")[0];
        String jobName = portfolioJob.split("\\.")[1];
        DescriptorsPortfolio portfolio = this.descriptorsPortfolioDAO.get(portfolioId);
        DescriptorJob job = portfolio.getJobByName(jobName).get();
        this.launchPortfolioJobWorkflowProcess(portfolio,job);

    }
}
