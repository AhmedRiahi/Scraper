package com.pp.mozart.service;

import com.mongodb.DBObject;
import com.pp.database.dao.common.DescriptorsPortfolioDAO;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.dao.mozart.JobExecutionHistoryDAO;
import com.pp.database.dao.semantic.PPIndividualDAO;
import com.pp.database.model.engine.DescriptorJob;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.database.model.mozart.JobExecutionHistory;
import com.pp.framework.kafka.KafkaTopics;
import com.pp.framework.kafka.sender.PPSender;
import com.pp.mozart.kafka.MozartReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MozartService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MozartReceiver.class);
	
	@Autowired
	private DescriptorsPortfolioDAO descriptorsPortfolioDAO;
	@Autowired
	private DescriptorWorkflowDataPackageDAO dwdpDAO;
	@Autowired
	private JobExecutionHistoryDAO dehDAO;
	@Autowired
	private PPSender sender;
    @Autowired
    private PPIndividualDAO individualDAO;

	
	public void launchDescriptorJobWorkflow(String workflowId) {
		LOGGER.info("processDescriptorWorkflow workflowID = {}",workflowId);
		// Prepare Data package
		DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
		dwdp.getDebugInformation().setMozartExecutionStep("Mozart Init");
		this.dwdpDAO.save(dwdp);
		//Create execution history
		this.createDescriptorJobExecutionHistory(dwdp);
		//Launch crawling process
		this.sender.send(KafkaTopics.Crawler.DOWNLOAD+KafkaTopics.IN,dwdp.getId().toHexString());
	}


	public void launchDescriptorJoinWorkflow(String workflowId) {
        LOGGER.info("processDescriptorJoinWorkflow workflowId = {}",workflowId);
	    DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        List<DBObject> individuals = this.individualDAO.getStagingWokflowIndividuals(workflowId);
        dwdp.getJoinDetails().setLaunchedJoinersCount(individuals.size());

        dwdp.getPortfolio().getDescriptorJoins(dwdp.getDescriptorJob().getDescriptor()).stream().forEach(join -> {
            individuals.stream().forEach(individual -> {
                DescriptorWorkflowDataPackage joinDwdp = new DescriptorWorkflowDataPackage();
                joinDwdp.getJoinDetails().setJoiner(true);
                joinDwdp.getJoinDetails().setJoinedIndividualId(individual.get("_id").toString());
                joinDwdp.getJoinDetails().setJoinedDWDP(dwdp);
                join.setSourceDSMId(dwdp.getDescriptorJob().getDescriptorSemanticMappingId());
                DescriptorJob joinJob = new DescriptorJob();
                joinJob.setDescriptor(join.getTargetDescriptorModel());
                joinJob.getCrawlingParams().setHttpMethod(HttpMethod.GET.name());
                joinJob.getCrawlingParams().setUrl(individual.get(join.getSourceURLListener().getName()).toString());
                joinJob.setStandaloneMode(false);
                joinJob.setDescriptorSemanticMappingId(join.getTargetDSMId());
                joinDwdp.setDescriptorJob(joinJob);
                joinDwdp.getJoinDetails().setDescriptorJoin(join);
                joinDwdp.getDebugInformation().setMozartExecutionStep("Mozart Init");
                joinDwdp.setPortfolio(dwdp.getPortfolio());
                this.dwdpDAO.save(joinDwdp);
                this.sender.send(KafkaTopics.Crawler.DOWNLOAD+KafkaTopics.IN,joinDwdp.getId().toHexString());
            });
        });
	}
	
	
	public void finishDescriptorWorkflow(String workflowId) {
		LOGGER.info("Finishing workflow processing {}",workflowId);
		DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        if(!dwdp.getJoinDetails().isJoiner()){
            DescriptorJob descriptorJob = dwdp.getPortfolio().getJobByName(dwdp.getDescriptorJob().getName()).get();
            descriptorJob.setLastCheckingDate(new Date());
            descriptorJob.setExecutionErrorsCount(0);
            this.descriptorsPortfolioDAO.save(dwdp.getPortfolio());
            this.closeDescriptorJobExecutionHistory(dwdp,false);
        }else{
            // Todo handle joiner workflow
        }

	}

	
	public void handleProcessingError(String workflowId) {
		LOGGER.info("handling workflow error {}",workflowId);
		DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        if(!dwdp.getJoinDetails().isJoiner()) {
            DescriptorJob descriptorJob = dwdp.getPortfolio().getJobByName(dwdp.getDescriptorJob().getName()).get();
            descriptorJob.setLastCheckingDate(new Date());
            descriptorJob.setExecutionErrorsCount(0);
            descriptorJob.incrementExecutionErrorsCount();
            if (descriptorJob.getExecutionErrorsCount() > 3) {
                descriptorJob.setCheckingRequired(true);
            }
            this.descriptorsPortfolioDAO.save(dwdp.getPortfolio());
            this.closeDescriptorJobExecutionHistory(dwdp, true);
        }else{
            // Todo handle joiner workflow
        }
		
	}
	
	private void createDescriptorJobExecutionHistory(DescriptorWorkflowDataPackage dwdp) {
		JobExecutionHistory deh = new JobExecutionHistory();
		deh.setStartTime(new Date());
		deh.setPortfolio(dwdp.getPortfolio());
		deh.setDescriptorJob(dwdp.getDescriptorJob());
		deh.setDwdp(dwdp);
		this.dehDAO.save(deh);
	}
	
	private void closeDescriptorJobExecutionHistory(DescriptorWorkflowDataPackage dwdp,boolean error) {
		JobExecutionHistory deh = this.dehDAO.getByDWDPId(dwdp.getStringId());
		deh.setFinishTime(new Date());
		if(error) {
			deh.setInError(true);
		}
		this.dehDAO.save(deh);
	}

}
