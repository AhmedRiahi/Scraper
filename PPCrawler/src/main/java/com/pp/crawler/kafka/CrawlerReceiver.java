package com.pp.crawler.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pp.crawler.core.PPCrawler;
import com.pp.crawler.payload.RendererPayload;
import com.pp.database.dao.crawler.CrawledContentDAO;
import com.pp.database.dao.mozart.DescriptorWorkflowDataPackageDAO;
import com.pp.database.model.crawler.CrawledContent;
import com.pp.database.model.mozart.DescriptorWorkflowDataPackage;
import com.pp.framework.kafka.KafkaTopics;
import com.pp.framework.kafka.sender.PPSender;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.pp.framework.kafka.KafkaTopics.OUT;

public class CrawlerReceiver {
	
	private static final Logger log = LoggerFactory.getLogger(CrawlerReceiver.class);
	
	@Autowired
	private PPCrawler ppCrawler;
	@Autowired
	private PPSender sender;
	@Autowired
	private DescriptorWorkflowDataPackageDAO dwdpDAO;
	@Autowired
	private CrawledContentDAO crawledContentDAO;

    private static final String RENDERING_SCRIPT_PATH = "/home/pnp_bin/pupitter/";
	
	@KafkaListener(topics = KafkaTopics.Crawler.DOWNLOAD+KafkaTopics.IN)
	public void download(String workflowId) {
		log.info("Crawler received message={}", workflowId);
        DescriptorWorkflowDataPackage dwdp = null;
		try {
		    dwdp = this.dwdpDAO.get(workflowId);
			URL url = new URL(dwdp.getDescriptorJob().getUrl());
			if(dwdp.getDescriptorJob().getDescriptor().isUseJSRendering()){
                ObjectMapper mapper = new ObjectMapper();
                RendererPayload rendererPayload = new RendererPayload();
                rendererPayload.setUrl(dwdp.getDescriptorJob().getUrl());
                rendererPayload.setWorkflowId(dwdp.getStringId());
			    this.sender.send(KafkaTopics.Renderer.DOWNLOAD+KafkaTopics.IN,mapper.writeValueAsString(rendererPayload));
            }else{
                String pageContent = this.ppCrawler.download(url,dwdp.getDescriptorJob().getDescriptor().getCookies());
                this.sendCrawlingResult(dwdp,pageContent);
            }

		} catch (Exception e) {
			log.error(e.toString(),e);
			if(dwdp != null){
                dwdp.getDebugInformation().setException(e.getMessage()+"\n"+Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
                this.dwdpDAO.save(dwdp);
            }else{
			    log.error("Enable to set debug information exception because dwdp is null");
            }

			this.sender.send(KafkaTopics.Crawler.DOWNLOAD+KafkaTopics.ERROR, workflowId);
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
        this.sender.send(KafkaTopics.Crawler.DOWNLOAD+KafkaTopics.OUT,dwdp.getStringId());
    }

    @KafkaListener(topics = KafkaTopics.Renderer.DOWNLOAD+ OUT)
    public void rendererDownloadCompleted(String rendererPayloadJson) throws IOException {
        log.info("Crawler received {} after downloding rendred descriptor");
        ObjectMapper mapper = new ObjectMapper();
        RendererPayload rendererPayload = mapper.readValue(rendererPayloadJson,RendererPayload.class);
        DescriptorWorkflowDataPackage dwdp = this.dwdpDAO.get(rendererPayload.getWorkflowId());
        this.sendCrawlingResult(dwdp,rendererPayload.getContents());
    }
}