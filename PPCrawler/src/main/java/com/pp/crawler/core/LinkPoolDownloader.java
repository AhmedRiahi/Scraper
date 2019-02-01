package com.pp.crawler.core;

import com.pp.crawler.exception.IrrelevantLinkException;
import com.pp.database.model.crawler.CrawlLinksDataSet;
import com.pp.database.model.crawler.Link;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;

@Slf4j
public class LinkPoolDownloader implements Callable<CrawlLinksDataSet>{

	private URL domainURL;
    private Set<Link> inputLinks;
	private URLDownloader urlDownloader;
    
    public LinkPoolDownloader(URL domainURL, Set<Link> inputLinks){
    	this.domainURL = domainURL;
    	this.inputLinks = inputLinks;
    	this.urlDownloader = new URLDownloader();
    }
		
    @Override
    public CrawlLinksDataSet call(){
        CrawlLinksDataSet cld = new CrawlLinksDataSet();
        Iterator<Link> iterator = this.inputLinks.iterator();
        while(iterator.hasNext()){
        	Link link = iterator.next();
			try {
				if(CrawlerUtils.isExternalLink(this.domainURL,link.getUrl())){
					// if external link dont process it
					cld.getExternalLinks().add(link);
					continue;
				}else{
					cld.getInternaLinks().add(link);
				}
				String content = this.download(link.getUrl());
				if(content == null){
					log.info("null content "+link);
					break;
				}
				Document document = Jsoup.parse(content);
	            Set<Link> links = CrawlerUtils.detectLinks(link.getUrl(),document);
	            cld.getNewLinks().addAll(links);
			} catch (MalformedURLException e) {
				log.info("MalformedURLException :"+link);
				cld.getIrrelevantLinks().add(link);
			} catch (IOException e) {
				log.info("IOException :"+link);
				cld.getIrrelevantLinks().add(link);
			} catch (IrrelevantLinkException e) {
				log.info("IrrelevantLinkException :"+link);
				cld.getIrrelevantLinks().add(link);
			}
        }
        return cld;
    }
    
    public String download(URL url) throws IOException, IrrelevantLinkException{
		this.urlDownloader.setUrl(url);
		this.urlDownloader.download();
		return urlDownloader.getUrlContent();
    }

}