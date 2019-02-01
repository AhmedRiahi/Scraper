package com.pp.crawler.core;

import com.pp.crawler.exception.IrrelevantLinkException;
import com.pp.database.model.crawler.Cookie;
import com.pp.database.model.crawler.CrawlLinksDataSet;
import com.pp.database.model.crawler.Link;
import com.pp.database.model.crawler.Sitemap;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PPCrawler {

	private URL baseURL;
	private final ExecutorService executor;
    private final ExecutorCompletionService<CrawlLinksDataSet> executorService;
    private int threadsNumber = 40;
    private CrawlLinksDataSet crawlingLinksDataset;
    private List<Future<CrawlLinksDataSet>> futureResults;
    
    public PPCrawler(){
    	this.executor = Executors.newFixedThreadPool(this.threadsNumber);
    	this.executorService = new ExecutorCompletionService<CrawlLinksDataSet>(this.executor);
    }
	
    public String download(URL url) throws MalformedURLException, IOException, IrrelevantLinkException{
		URLDownloader urlDownloader = new URLDownloader(url);
		urlDownloader.download();
		return urlDownloader.getUrlContent();
    }
    
    public String download(URL url,List<Cookie> cookies) throws MalformedURLException, IOException, IrrelevantLinkException{
		URLDownloader urlDownloader = new URLDownloader(url);
		urlDownloader.setCookies(cookies);
		urlDownloader.download();
		return urlDownloader.getUrlContent();
    }
	
    public Sitemap createSitemap(URL url) throws MalformedURLException, IOException, IrrelevantLinkException{
    	System.out.println("Sitemap creation started");
    	this.baseURL = url;
    	this.crawlingLinksDataset = new CrawlLinksDataSet();
    	HashSet<Link> links = this.crawlFirstPage(url);
        this.launchWaveCrawling(links);
        System.out.println("Sitemap creation finished");
        Sitemap sitemap = new Sitemap(url.getHost());
        sitemap.setCld(this.crawlingLinksDataset);
        return sitemap;
    }
    
    private HashSet<Link> crawlFirstPage(URL url) throws MalformedURLException, IOException, IrrelevantLinkException{
    	String firstPage = this.download(url);
        Document document = Jsoup.parse(firstPage);
        HashSet<Link> links = CrawlerUtils.detectLinks(url,document);
        this.crawlingLinksDataset.getInternaLinks().add(new Link(url));
        return links;
    }
       
    private void initWaveCrawling(){
        this.futureResults = new ArrayList<>();
    }
    
    private void launchWaveCrawling(HashSet<Link> links){
    	this.initWaveCrawling();
        System.out.println("Start wave processing : "+links.size());
        int wavePortionSize = links.size()/this.threadsNumber;
        for(int i=0 ; i<this.threadsNumber ;i++){
        	int portionFrom = i*wavePortionSize;
        	int portionTo = (i+1)*wavePortionSize;
            HashSet<Link> linksPortion =  (HashSet<Link>) links.stream().skip(portionFrom).limit(portionTo).collect(Collectors.toSet());
            Future<CrawlLinksDataSet> futureResult = this.crawlLinks(linksPortion);
            this.futureResults.add(futureResult);
        }
        
        // for the rest of links
        long rest = links.size()%this.threadsNumber;
        HashSet<Link> linksPortion =  (HashSet<Link>) links.stream().skip(links.size()-rest).limit(links.size()).collect(Collectors.toSet());
        Future<CrawlLinksDataSet> futureResult = this.crawlLinks(linksPortion);
        this.futureResults.add(futureResult);
        
        this.collectCrawlingResults();
        
        if( this.crawlingLinksDataset.containsNewLinks()){
        	this.launchWaveCrawling(this.crawlingLinksDataset.getNewLinks());
        }
        
    }
    
    private void collectCrawlingResults(){
    	int collectedResultsNumber = 0;
    	while(collectedResultsNumber < this.futureResults.size()){
			try {
				Future<CrawlLinksDataSet> future = this.executorService.take();
				System.out.println(future.isDone()+" "+collectedResultsNumber);
				CrawlLinksDataSet cld = future.get();
				this.crawlingLinksDataset.append(cld);
	            collectedResultsNumber++;
			} catch (InterruptedException e) {
				log.error(e.getMessage(),e);
			} catch (ExecutionException e) {
                log.error(e.getMessage(),e);
			}
    	}
        this.crawlingLinksDataset.cleanNewLinks();
    }
        
    private Future<CrawlLinksDataSet> crawlLinks(HashSet<Link> links){
        LinkPoolDownloader linkPoolDownloader = new LinkPoolDownloader(this.baseURL,links);
        return this.executorService.submit(linkPoolDownloader);
    }
	
    public void shutdown(){
        this.executor.shutdown();
    }

	
    // -------------------------------- GETTER / SETTER --------------------------------

    public int getThreadsNumber() {
        return threadsNumber;
    }
	
    public void setThreadsNumber(int threadsNumber) {
	this.threadsNumber = threadsNumber;
    }

}
