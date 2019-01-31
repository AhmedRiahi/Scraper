package com.pp.crawler.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.pp.crawler.exception.IrrelevantLinkException;
import com.pp.database.model.crawler.CrawlLinksDataset;
import com.pp.database.model.crawler.Link;

public class LinkPoolDownloader implements Callable<CrawlLinksDataset>{

	private URL domainURL;
    private HashSet<Link> inputLinks;
	private URLDownloader urlDownloader;
    
    public LinkPoolDownloader(URL domainURL,HashSet<Link> inputLinks){
    	this.domainURL = domainURL;
    	this.inputLinks = inputLinks;
    	this.urlDownloader = new URLDownloader();
    }
		
    @Override
    public CrawlLinksDataset call(){
        CrawlLinksDataset cld = new CrawlLinksDataset();
        Iterator<Link> iterator = this.inputLinks.iterator();
        while(iterator.hasNext()){
        	Link link = iterator.next();
        	//System.out.println("processing :"+link);
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
					System.out.println("null content"+link);
					break;
				}
				Document document = Jsoup.parse(content);
	            HashSet<Link> links = CrawlerUtils.detectLinks(link.getUrl(),document);
	            cld.getNewLinks().addAll(links);
	            //System.out.println("finished :"+link);
			} catch (MalformedURLException e) {
				System.out.println("MalformedURLException :"+link);
				cld.getIrrelevantLinks().add(link);
				//e.printStackTrace();
			} catch (IOException e) {
				System.out.println("IOException :"+link);
				cld.getIrrelevantLinks().add(link);
				//e.printStackTrace();
			} catch (IrrelevantLinkException e) {
				System.out.println("IrrelevantLinkException :"+link);
				cld.getIrrelevantLinks().add(link);
				//e.printStackTrace();
			}
        }
        return cld;
    }
    
    public String download(URL url) throws MalformedURLException, IOException, IrrelevantLinkException{
		this.urlDownloader.setUrl(url);
		this.urlDownloader.download();
		return urlDownloader.getUrlContent();
    }

}