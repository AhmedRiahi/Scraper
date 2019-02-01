package com.pp.database.model.crawler;

import lombok.Data;
import org.mongodb.morphia.annotations.Entity;

import java.util.HashSet;

@Entity
@Data
public class CrawlLinksDataSet1 {
	
    private HashSet<Link> externalLinks;
    private HashSet<Link> internaLinks;
    private HashSet<Link> irrelevantLinks;
    private HashSet<Link> newLinks;
	
	
    public CrawlLinksDataSet(){
        this.externalLinks 		= new HashSet<Link>();
        this.internaLinks 		= new HashSet<Link>();
        this.irrelevantLinks 	= new HashSet<Link>();
        this.newLinks 			= new HashSet<Link>();
    }
    
    
    public void append(CrawlLinksDataSet cld){
        this.externalLinks.addAll(cld.externalLinks);
        this.internaLinks.addAll(cld.internaLinks);
        this.irrelevantLinks.addAll(cld.irrelevantLinks);
        this.newLinks.addAll(cld.newLinks);
    }
    
    public void cleanNewLinks(){
    	newLinks.removeAll(this.internaLinks);
    	newLinks.removeAll(this.externalLinks);
    	newLinks.removeAll(this.irrelevantLinks);
    }

    public boolean containsNewLinks(){
    	return this.newLinks.size() != 0;
    }
}
