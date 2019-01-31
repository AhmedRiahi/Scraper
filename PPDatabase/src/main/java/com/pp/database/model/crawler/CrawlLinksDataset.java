package com.pp.database.model.crawler;

import java.util.HashSet;

import org.mongodb.morphia.annotations.Entity;

@Entity
public class CrawlLinksDataset {
	
    private HashSet<Link> externalLinks;
    private HashSet<Link> internaLinks;
    private HashSet<Link> irrelevantLinks;
    private HashSet<Link> newLinks;
	
	
    public CrawlLinksDataset(){
        this.externalLinks 		= new HashSet<Link>();
        this.internaLinks 		= new HashSet<Link>();
        this.irrelevantLinks 	= new HashSet<Link>();
        this.newLinks 			= new HashSet<Link>();
    }
    
    
    public void append(CrawlLinksDataset cld){
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
    

    @Override
    public String toString() {
        String result = " External links :\n";
        result += this.externalLinks.stream().map(link -> link.toString());
        result = " Internal links :\n";
        result += this.internaLinks.stream().map(link -> link.toString());
        result = " Irrelevant links :\n";
        result += this.irrelevantLinks.stream().map(link -> link.toString());
        result = " New links links :\n";
        result += this.newLinks.stream().map(link -> link.toString());
        return result;
    }

	
    // -------------------------------- GETTER / SETTER --------------------------------

    public HashSet<Link> getExternalLinks() {
        return externalLinks;
    }
    
    public void setExternalLinks(HashSet<Link> externalLinks) {
        this.externalLinks = externalLinks;
    }

    public HashSet<Link> getInternaLinks() {
        return internaLinks;
    }

    public void setInternaLinks(HashSet<Link> internaLinks) {
        this.internaLinks = internaLinks;
    }

    public HashSet<Link> getIrrelevantLinks() {
        return irrelevantLinks;
    }

    public void setIrrelevantLinks(HashSet<Link> irrelevantLinks) {
        this.irrelevantLinks = irrelevantLinks;
    }

    public HashSet<Link> getNewLinks() {
        return newLinks;
    }

    public void setNewLinks(HashSet<Link> newLinks) {
        this.newLinks = newLinks;
    }
}
