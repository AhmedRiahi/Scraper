package com.pp.analytics.jms;

import com.pp.analytics.service.AnalyticsService;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.framework.jms.JMSTopics;
import com.pp.framework.jms.sender.PPSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnalyticsReceiver {

    public static final String ANALYTICS_RECEIVED_MESSAGE = "Analytics:{} received message = {}";

    @Autowired
    private DescriptorWorkflowDataPackageDAO dwdpDAO;
    @Autowired
    private AnalyticsService analyticsService;
    @Autowired
    private PPSender sender;


    @JmsListener(destination = JMSTopics.Analytics.ANALYSE_STANDALONE_DESCRIPTOR_POPULATION + JMSTopics.IN)
    public void analyseStandaloneDescriptorPopulation(String workflowId) {
        try {
            log.info(ANALYTICS_RECEIVED_MESSAGE, JMSTopics.Analytics.ANALYSE_STANDALONE_DESCRIPTOR_POPULATION, workflowId);
            this.analyticsService.processStandaloneDescriptorPopulation(workflowId);
            this.sender.send(JMSTopics.Analytics.ANALYSE_STANDALONE_DESCRIPTOR_POPULATION + JMSTopics.OUT, workflowId);
        } catch (Exception e) {
            log.error(e.toString(), e);
            this.sender.send(JMSTopics.Analytics.ANALYSE_STANDALONE_DESCRIPTOR_POPULATION + JMSTopics.ERROR, workflowId);
        }
    }

    @JmsListener(destination = JMSTopics.Analytics.ANALYSE_JOINED_DESCRIPTOR_POPULATION + JMSTopics.IN)
    public void analyseJoinedDescriptorPopulation(String workflowId) {
        DescriptorWorkflowDataPackage dwdp = null;
        try {
            dwdp = this.dwdpDAO.get(workflowId);
            log.info(ANALYTICS_RECEIVED_MESSAGE, JMSTopics.Analytics.ANALYSE_JOINED_DESCRIPTOR_POPULATION, workflowId);
            this.analyticsService.processJoinedDescriptorPopulation(workflowId);
            this.sender.send(JMSTopics.Analytics.ANALYSE_JOINED_DESCRIPTOR_POPULATION + JMSTopics.OUT, workflowId);
        } catch (Exception e) {
            this.handleGlobalError(dwdp, e);
            this.sender.send(JMSTopics.Analytics.ANALYSE_JOINED_DESCRIPTOR_POPULATION + JMSTopics.ERROR, workflowId);
        }
    }


    @JmsListener(destination = JMSTopics.Analytics.ANALYSE_JOINER_DESCRIPTOR_POPULATION + JMSTopics.IN)
    public void analyseJoinerDescriptorPopulation(String workflowId) {
        DescriptorWorkflowDataPackage dwdp = null;
        try {
            dwdp = this.dwdpDAO.get(workflowId);
            log.info(ANALYTICS_RECEIVED_MESSAGE, JMSTopics.Analytics.ANALYSE_JOINER_DESCRIPTOR_POPULATION, workflowId);
            this.analyticsService.processJoinerDescriptorPopulation(workflowId);
            this.sender.send(JMSTopics.Analytics.ANALYSE_JOINER_DESCRIPTOR_POPULATION + JMSTopics.OUT, workflowId);
        } catch (Exception e) {
            this.handleGlobalError(dwdp, e);
            this.sender.send(JMSTopics.Analytics.ANALYSE_JOINER_DESCRIPTOR_POPULATION + JMSTopics.ERROR, workflowId);
        }
    }

    private void handleGlobalError(DescriptorWorkflowDataPackage dwdp, Exception e) {
        log.error(e.toString(), e);
        if (dwdp != null) {
            dwdp.getDebugInformation().setException(e.getMessage() + "\n" + Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
            this.dwdpDAO.save(dwdp);
        } else {
            log.error("Enable to set debug information exception because dwdp is null");
        }
    }


    @JmsListener(destination = JMSTopics.Analytics.ANALYSE_INDIVIDUAL + JMSTopics.IN)
    public void analyseIndividual(String individualId) {
        try {
            log.info(ANALYTICS_RECEIVED_MESSAGE, JMSTopics.Analytics.ANALYSE_INDIVIDUAL, individualId);
            this.analyticsService.generateIndividualPopulation(individualId);
            this.sender.send(JMSTopics.Analytics.ANALYSE_INDIVIDUAL + JMSTopics.OUT, individualId);
        } catch (Exception e) {
            log.error(e.toString(), e);
            this.sender.send(JMSTopics.Analytics.ANALYSE_INDIVIDUAL + JMSTopics.ERROR, individualId);
        }
    }

}
