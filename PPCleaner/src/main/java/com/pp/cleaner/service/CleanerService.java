package com.pp.cleaner.service;

import com.pp.cleaner.jms.CleanerReceiver;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.dao.mozart.JobExecutionHistoryDAO;
import com.pp.database.model.mozart.JobExecutionHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CleanerService {
	
	private static final Logger log = LoggerFactory.getLogger(CleanerReceiver.class);

	private static final long CLEAN_DELAY = 2 * 24 * 60 * 60 * 1000l ; // 2 day
	@Autowired
	private DescriptorWorkflowDataPackageDAO dwdpDAO;

	@Autowired
    private JobExecutionHistoryDAO jobExecutionHistoryDAO;
	
	public void clean() {
		//Clean DescriptorWorkflowDataPackage
		List<JobExecutionHistory> jobExecutionHistories = this.jobExecutionHistoryDAO.getBeforeDate(CleanerService.CLEAN_DELAY);
		if(!jobExecutionHistories.isEmpty()) {
			log.info("Preparing cleaning process for {} job execution history.",jobExecutionHistories.size());
            jobExecutionHistories.stream().forEach(jobExecutionHistory -> {
                this.dwdpDAO.delete(jobExecutionHistory.getDwdp());
                this.jobExecutionHistoryDAO.delete(jobExecutionHistory);
			});
		}else {
			log.info("No packages found for cleaning.");
		}
	}
}
