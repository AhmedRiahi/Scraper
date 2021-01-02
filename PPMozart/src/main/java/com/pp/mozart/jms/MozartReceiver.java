package com.pp.mozart.jms;

import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.framework.jms.sender.PPSender;
import com.pp.mozart.service.MozartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import static com.pp.framework.jms.JMSTopics.*;

@Service
@Slf4j
public class MozartReceiver {

	@Autowired
	private MozartService mozartService;
	@Autowired
	private PPSender sender;
	@Autowired
	private DescriptorWorkflowDataPackageDAO dwdpDAO;


    @JmsListener(destination = Mozart.PROCESS_DESCRIPTOR)
	public void launchDescriptorWorkflow(String workflowId){
		this.mozartService.launchDescriptorJobWorkflow(workflowId);
	}

    @JmsListener(destination = Mozart.PROCESS_DESCRIPTOR_JOIN)
    public void launchDescriptorJoinWorkflow(String workflowId){
        this.mozartService.launchDescriptorJoinWorkflow(workflowId);
    }

	/* Crawler Listeners */

    @JmsListener(destination = Crawler.DOWNLOAD+ OUT)
	public void crawlerDownloadCompleted(String workflowId) {
		log.info("Mozart received {} after downloding descriptor",workflowId);
		DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
		dwdp.getDebugInformation().setMozartExecutionStep(Crawler.DOWNLOAD+ OUT);
		this.dwdpDAO.save(dwdp);
		this.sender.send(Scraper.MATCH_DESCRIPTOR+ IN, workflowId);
	}

    @JmsListener(destination = Crawler.DOWNLOAD+ ERROR)
	public void crawlerDownloadError(String workflowId) {
		log.error("Error : "+ Crawler.DOWNLOAD+" "+workflowId);
		DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
		dwdp.getDebugInformation().setMozartExecutionStep(Crawler.DOWNLOAD+ ERROR);
		this.dwdpDAO.save(dwdp);
		this.mozartService.handleProcessingError(workflowId);
	}

	/* Scraper Listeners */

    @JmsListener(destination = Scraper.MATCH_DESCRIPTOR+ OUT)
	public void matchDescriptorCompleted(String workflowId) {
		log.info("Mozart received {} after matching descriptor",workflowId);
		DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        dwdp.getDebugInformation().setMozartExecutionStep(Scraper.MATCH_DESCRIPTOR+ OUT);
        this.dwdpDAO.save(dwdp);
		if(dwdp.getIndividuals() != null && !dwdp.getIndividuals().isEmpty()) {
		    if(dwdp.getDescriptorJob().isStandaloneMode()){
                this.sender.send(Analytics.ANALYSE_STANDALONE_DESCRIPTOR_POPULATION + IN, workflowId);
            }else{
		        if(dwdp.getJoinDetails().isJoiner()){
                    this.sender.send(Analytics.ANALYSE_JOINER_DESCRIPTOR_POPULATION + IN, workflowId);
                }else{
                    this.sender.send(Analytics.ANALYSE_JOINED_DESCRIPTOR_POPULATION + IN, workflowId);
                }
            }
		}else {
		    // TODO Error must be thrown here because 0 individuals before cleaning is abnormal behaviour
			log.info("No Individuals found : Nothing to be computed aborting processing for workflow {}",workflowId);
			this.mozartService.finishDescriptorWorkflow(workflowId);
		}
	}

    @JmsListener(destination = Scraper.MATCH_DESCRIPTOR+ ERROR)
	public void matchDescriptorError(String workflowId) {
		log.error("Error : "+ Scraper.MATCH_DESCRIPTOR+" "+workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        dwdp.getDebugInformation().setMozartExecutionStep(Scraper.MATCH_DESCRIPTOR+ ERROR);
        this.dwdpDAO.save(dwdp);
		this.mozartService.handleProcessingError(workflowId);
	}

	/* Analytics Listeners */

    @JmsListener(destination = Analytics.ANALYSE_STANDALONE_DESCRIPTOR_POPULATION + OUT)
	public void analyseStandaloneDescriptorPopulationCompleted(String workflowId) {
		log.info("Mozart received {} after analysing descriptor",workflowId);
		DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        dwdp.getDebugInformation().setMozartExecutionStep(Analytics.ANALYSE_STANDALONE_DESCRIPTOR_POPULATION + OUT);
        this.dwdpDAO.save(dwdp);
		if(dwdp.getDebugInformation().getCleanIndividualsCount() > 0) {
            this.sender.send(Subscription.SCAN_DESCRIPTOR_POPULATION_SUBSCRIPTION+ IN, workflowId);
		}else {
			log.info("No Clean Individuals found : Nothing to be computed aborting processing for workflow {}",workflowId);
			this.mozartService.finishDescriptorWorkflow(workflowId);
		}
	}


    @JmsListener(destination = Analytics.ANALYSE_JOINED_DESCRIPTOR_POPULATION + OUT)
    public void analyseJoinedDescriptorPopulationCompleted(String workflowId) {
        log.info("Mozart received {} after analysing descriptor",workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        dwdp.getDebugInformation().setMozartExecutionStep(Analytics.ANALYSE_JOINED_DESCRIPTOR_POPULATION + OUT);
        this.dwdpDAO.save(dwdp);
        if(dwdp.getDebugInformation().getCleanIndividualsCount() > 0) {
            this.sender.send(Mozart.PROCESS_DESCRIPTOR_JOIN,workflowId);
        }else {
            log.info("No Clean Individuals found : Nothing to be computed aborting processing for workflow {}",workflowId);
            this.mozartService.finishDescriptorWorkflow(workflowId);
        }
    }

    @JmsListener(destination = Analytics.ANALYSE_JOINER_DESCRIPTOR_POPULATION + OUT)
    public void analyseJoinerDescriptorPopulationCompleted(String workflowId) {
        log.info("Mozart received {} after analysing descriptor",workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        dwdp.getDebugInformation().setMozartExecutionStep(Analytics.ANALYSE_JOINER_DESCRIPTOR_POPULATION + OUT);
        this.dwdpDAO.save(dwdp);
        this.sender.send(Subscription.SCAN_DESCRIPTOR_POPULATION_SUBSCRIPTION+IN,dwdp.getJoinDetails().getJoinedDWDP().getStringId());
        this.mozartService.finishDescriptorWorkflow(workflowId);
    }


    @JmsListener(destination = Analytics.ANALYSE_STANDALONE_DESCRIPTOR_POPULATION + ERROR)
    public void analyseStandaloneDescriptorPopulationError(String workflowId) {
        log.error("Error : "+ Analytics.ANALYSE_STANDALONE_DESCRIPTOR_POPULATION +" "+workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        dwdp.getDebugInformation().setMozartExecutionStep(Analytics.ANALYSE_STANDALONE_DESCRIPTOR_POPULATION + ERROR);
        this.dwdpDAO.save(dwdp);
        this.mozartService.handleProcessingError(workflowId);
    }


    @JmsListener(destination = Analytics.ANALYSE_JOINED_DESCRIPTOR_POPULATION + ERROR)
    public void analyseJoinedDescriptorPopulationError(String workflowId) {
        log.error("Error : "+ Analytics.ANALYSE_JOINED_DESCRIPTOR_POPULATION +" "+workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        dwdp.getDebugInformation().setMozartExecutionStep(Analytics.ANALYSE_JOINED_DESCRIPTOR_POPULATION + ERROR);
        this.dwdpDAO.save(dwdp);
        this.mozartService.handleProcessingError(workflowId);
    }

    @JmsListener(destination = Analytics.ANALYSE_JOINER_DESCRIPTOR_POPULATION + ERROR)
    public void analyseJoineRDescriptorPopulationError(String workflowId) {
        log.error("Error : "+ Analytics.ANALYSE_JOINED_DESCRIPTOR_POPULATION +" "+workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        dwdp.getDebugInformation().setMozartExecutionStep(Analytics.ANALYSE_JOINER_DESCRIPTOR_POPULATION + ERROR);
        this.dwdpDAO.save(dwdp);
        this.mozartService.handleProcessingError(workflowId);
    }

	/* Subscription Listeners */

    @JmsListener(destination = Subscription.SCAN_DESCRIPTOR_POPULATION_SUBSCRIPTION+ OUT)
	public void scanDescriptorPopulationSubscriptionCompleted(String workflowId) {
		log.info("Mozart received {} after analysing descriptor subscriptions",workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        dwdp.getDebugInformation().setMozartExecutionStep(Subscription.SCAN_DESCRIPTOR_POPULATION_SUBSCRIPTION+ OUT);
        this.dwdpDAO.save(dwdp);
		this.mozartService.finishDescriptorWorkflow(workflowId);
	}

    @JmsListener(destination = Subscription.SCAN_DESCRIPTOR_POPULATION_SUBSCRIPTION+ ERROR)
	public void scanDescriptorPopulationSubscriptionError(String workflowId) {
		log.error("Error : "+ Subscription.SCAN_DESCRIPTOR_POPULATION_SUBSCRIPTION+" "+workflowId);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(workflowId);
        dwdp.getDebugInformation().setMozartExecutionStep(Subscription.SCAN_DESCRIPTOR_POPULATION_SUBSCRIPTION+ ERROR);
        this.dwdpDAO.save(dwdp);
		this.mozartService.handleProcessingError(workflowId);
	}
}
