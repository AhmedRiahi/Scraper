package com.pp.mozart.service;

import com.mongodb.DBObject;
import com.pp.database.dao.common.DescriptorsPortfolioDAO;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.dao.mozart.JobExecutionHistoryDAO;
import com.pp.database.dao.semantic.PPIndividualDAO;
import com.pp.database.model.engine.DescriptorJob;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.database.model.mozart.JobExecutionHistory;
import com.pp.framework.jms.JMSTopics;
import com.pp.framework.jms.sender.PPSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class MozartService {

    @Autowired
    private DescriptorsPortfolioDAO descriptorsPortfolioDAO;
    @Autowired
    private DescriptorWorkflowDataPackageDAO dwdpDAO;
    @Autowired
    private JobExecutionHistoryDAO jobExecutionHistoryDAO;
    @Autowired
    private PPSender sender;
    @Autowired
    private PPIndividualDAO individualDAO;


    public void launchDescriptorJobWorkflow(String workflowId) {
        log.info("processDescriptorWorkflow workflowID = {}", workflowId);
        // Prepare Data package
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        dwdp.getDebugInformation().setMozartExecutionStep("Mozart Init");
        this.dwdpDAO.save(dwdp);
        //Create execution history
        this.createDescriptorJobExecutionHistory(dwdp);
        //Launch crawling process
        this.sender.send(JMSTopics.Crawler.DOWNLOAD + JMSTopics.IN, dwdp.getId().toHexString());
    }

    public void launchDescriptorJoinWorkflow(String workflowId) {
        log.info("processDescriptorJoinWorkflow workflowId = {}", workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        List<DBObject> individuals = this.individualDAO.getStagingWokflowIndividuals(workflowId);
        dwdp.getJoinDetails().setLaunchedJoinersCount(individuals.size());

        dwdp.getPortfolio().getDescriptorJoins(dwdp.getDescriptorJob().getDescriptor()).stream().forEach(join -> {
            individuals.stream().forEach(stagingIndividual -> {
                DescriptorWorkflowDataPackage joinDwdp = new DescriptorWorkflowDataPackage();
                joinDwdp.getJoinDetails().setJoiner(true);
                joinDwdp.getJoinDetails().setJoinedStagingIndividualId(stagingIndividual.get("_id").toString());
                joinDwdp.getJoinDetails().setJoinedDWDP(dwdp);
                joinDwdp.getJoinDetails().setJoinedIndividual(dwdp.getIndividuals().stream().filter(individual -> individual.getId().toString().equals(stagingIndividual.get("_id").toString())).findFirst().get());
                join.setSourceDSMId(dwdp.getDescriptorJob().getDescriptorSemanticMappingId());
                DescriptorJob joinJob = new DescriptorJob();
                joinJob.setName(dwdp.getDescriptorJob().getName() + " - Joiner");
                joinJob.setDescriptor(join.getTargetDescriptorModel());
                joinJob.getCrawlingParams().setHttpMethod(HttpMethod.GET.name());
                joinJob.getCrawlingParams().setUrl(stagingIndividual.get(join.getSourceURLListener().getName()).toString());
                joinJob.setStandaloneMode(false);
                joinJob.setDescriptorSemanticMappingId(join.getTargetDSMId());
                joinDwdp.setDescriptorJob(joinJob);
                joinDwdp.getJoinDetails().setDescriptorJoin(join);
                joinDwdp.getDebugInformation().setMozartExecutionStep("Mozart Init");
                joinDwdp.setPortfolio(dwdp.getPortfolio());
                this.dwdpDAO.save(joinDwdp);
                //Create execution history
                this.createDescriptorJobExecutionHistory(joinDwdp);
                this.sender.send(JMSTopics.Crawler.DOWNLOAD + JMSTopics.IN, joinDwdp.getId().toHexString());
            });
        });
    }

    public void finishDescriptorWorkflow(String workflowId) {
        log.info("Finishing workflow processing {}", workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        if (!dwdp.getJoinDetails().isJoiner()) {
            DescriptorJob descriptorJob = dwdp.getPortfolio().getJobByName(dwdp.getDescriptorJob().getName()).get();
            descriptorJob.setLastCheckingDate(new Date());
            descriptorJob.setExecutionErrorsCount(0);
            this.descriptorsPortfolioDAO.save(dwdp.getPortfolio());
        } else {
            // Todo handle joiner workflow
        }
        this.closeDescriptorJobExecutionHistory(dwdp, false);

    }

    public void handleProcessingError(String workflowId) {
        log.info("handling workflow error {}", workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        if (!dwdp.getJoinDetails().isJoiner()) {
            DescriptorJob descriptorJob = dwdp.getPortfolio().getJobByName(dwdp.getDescriptorJob().getName()).get();
            descriptorJob.setLastCheckingDate(new Date());
            descriptorJob.setExecutionErrorsCount(0);
            descriptorJob.incrementExecutionErrorsCount();
            if (descriptorJob.getExecutionErrorsCount() > 3) {
                descriptorJob.setCheckingRequired(true);
            }
            this.descriptorsPortfolioDAO.save(dwdp.getPortfolio());
        } else {
            // Todo handle joiner workflow
        }
        this.closeDescriptorJobExecutionHistory(dwdp, true);
    }

    private void createDescriptorJobExecutionHistory(DescriptorWorkflowDataPackage dwdp) {
        JobExecutionHistory jobExecutionHistory = new JobExecutionHistory();
        jobExecutionHistory.setStartTime(new Date());
        jobExecutionHistory.setPortfolio(dwdp.getPortfolio());
        jobExecutionHistory.setDescriptorJob(dwdp.getDescriptorJob());
        jobExecutionHistory.setDwdp(dwdp);
        jobExecutionHistory.setJoinDetails(dwdp.getJoinDetails());
        this.jobExecutionHistoryDAO.save(jobExecutionHistory);
    }

    private void closeDescriptorJobExecutionHistory(DescriptorWorkflowDataPackage dwdp, boolean error) {
        JobExecutionHistory jobExecutionHistory = this.jobExecutionHistoryDAO.getByDWDPId(dwdp.getStringId());
        jobExecutionHistory.setFinishTime(new Date());
        if (error) {
            jobExecutionHistory.setError(true);
        }
        this.jobExecutionHistoryDAO.save(jobExecutionHistory);
    }

}
