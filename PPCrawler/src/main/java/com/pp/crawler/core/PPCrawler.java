package com.pp.crawler.core;

import com.pp.crawler.exception.IrrelevantLinkException;
import com.pp.database.model.crawler.Cookie;
import com.pp.database.model.crawler.CrawlLinksDataSet;
import com.pp.database.model.crawler.Link;
import com.pp.database.model.crawler.Sitemap;
import com.pp.database.model.engine.DescriptorJobCrawlingParams;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    	this.executorService = new ExecutorCompletionService<>(this.executor);
    }
	
    public String download(URL url) throws  IOException, IrrelevantLinkException{
		URLDownloader urlDownloader = new URLDownloader(url);
		urlDownloader.download();
		return urlDownloader.getUrlContent();
    }

    public BufferedImage downloadImage(URL url) throws  IOException{
        return ImageIO.read(url);
    }
    
    public String download(DescriptorJobCrawlingParams crawlingParams, List<Cookie> cookies) throws  IOException, IrrelevantLinkException{
		URLDownloader urlDownloader = new URLDownloader(new URL(crawlingParams.getUrl()));
		urlDownloader.setHttpMethod(HttpMethod.resolve(crawlingParams.getHttpMethod()));
		urlDownloader.setBodyParams(crawlingParams.getHttpParams());
		urlDownloader.setCookies(cookies);
		urlDownloader.download();
		return urlDownloader.getUrlContent();
    }
	
    public Sitemap createSitemap(URL url) throws  IOException, IrrelevantLinkException{
    	log.info("Sitemap creation started");
    	this.baseURL = url;
    	this.crawlingLinksDataset = new CrawlLinksDataSet();
    	Set<Link> links = this.crawlFirstPage(url);
        this.launchWaveCrawling(links);
        log.info("Sitemap creation finished");
        Sitemap sitemap = new Sitemap(url.getHost());
        sitemap.setCld(this.crawlingLinksDataset);
        return sitemap;
    }
    
    private Set<Link> crawlFirstPage(URL url) throws IOException, IrrelevantLinkException{
    	String firstPage = this.download(url);
        Document document = Jsoup.parse(firstPage);
        Set<Link> links = CrawlerUtils.detectLinks(url,document);
        this.crawlingLinksDataset.getInternaLinks().add(new Link(url));
        return links;
    }
       
    private void initWaveCrawling(){
        this.futureResults = new ArrayList<>();
    }
    
    private void launchWaveCrawling(Set<Link> links){
    	this.initWaveCrawling();
        log.info("Start wave processing : "+links.size());
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
    
    private void collectCrawlingResults() {
    	int collectedResultsNumber = 0;
    	while(collectedResultsNumber < this.futureResults.size()){
			try {
				Future<CrawlLinksDataSet> future = this.executorService.take();
                log.info(future.isDone()+" "+collectedResultsNumber);
				CrawlLinksDataSet cld = future.get();
				this.crawlingLinksDataset.append(cld);
	            collectedResultsNumber++;
			} catch (InterruptedException e) {
				log.error(e.getMessage(),e);
                Thread.currentThread().interrupt();
			} catch ( ExecutionException e) {
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

}
