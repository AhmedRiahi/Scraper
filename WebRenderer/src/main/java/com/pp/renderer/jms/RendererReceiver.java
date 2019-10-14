package com.pp.renderer.jms;

import com.pp.database.dao.crawler.CrawledContentDAO;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.model.crawler.CrawledContent;
import com.pp.database.model.engine.DescriptorJobCrawlingParams;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.framework.jms.JMSTopics;
import com.pp.framework.jms.sender.PPSender;
import com.pp.renderer.core.WebRendererService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RendererReceiver {

    @Autowired
	private WebRendererService webRendererService;
	@Autowired
	private PPSender sender;
	@Autowired
	private DescriptorWorkflowDataPackageDAO dwdpDAO;
	@Autowired
	private CrawledContentDAO crawledContentDAO;
	
	@JmsListener(destination = JMSTopics.Renderer.DOWNLOAD+ JMSTopics.IN)
	public void download(String workflowId) {
		log.info("Renderer received message={}", workflowId);
        DescriptorWorkflowDataPackage dwdp = null;
		try {
		    dwdp = this.dwdpDAO.get(workflowId);
            DescriptorJobCrawlingParams crawlingParams = dwdp.getDescriptorJob().getCrawlingParams();
            log.info("rendering url : "+crawlingParams.getUrl());
            String pageContent = this.webRendererService.download(crawlingParams);
            this.sendCrawlingResult(dwdp,pageContent);
		} catch (Exception e) {
			log.error(e.toString(),e);
			if(dwdp != null){
                dwdp.getDebugInformation().setException(e.getMessage()+"\n"+Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
                this.dwdpDAO.save(dwdp);
            }else{
			    log.error("Enable to set debug information exception because dwdp is null");
            }
			this.sender.send(JMSTopics.Renderer.DOWNLOAD+ JMSTopics.ERROR, workflowId);
		}
	}


	public void sendCrawlingResult(DescriptorWorkflowDataPackage dwdp,String pageContent){
        CrawledContent crawledContent = new CrawledContent();
        crawledContent.setContents(pageContent);
        dwdp.setCrawledContent(crawledContent);
        this.crawledContentDAO.save(crawledContent);
        UpdateOperations<DescriptorWorkflowDataPackage> updateOperation = this.dwdpDAO.createUpdateOperations().set("crawledContent",crawledContent);
        Query<DescriptorWorkflowDataPackage> query = this.dwdpDAO.createQuery().field("_id").equal(new ObjectId(dwdp.getStringId()));
        this.dwdpDAO.update(query,updateOperation);
        this.sender.send(JMSTopics.Renderer.DOWNLOAD+ JMSTopics.OUT,dwdp.getStringId());
    }
}