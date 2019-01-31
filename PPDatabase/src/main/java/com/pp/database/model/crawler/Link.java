package com.pp.database.model.crawler;

import java.net.URL;

import org.mongodb.morphia.annotations.Transient;

public class Link{
	
	@Transient
    private URL url;	
	private String urlString;
	
    public Link(URL url){
        this.url = url;
        this.urlString = this.url.toString();
    }

    @Override
    public String toString() {
        return "URL : "+this.url.toString();
    }
    
    @Override
    public int hashCode() {
    	return this.url.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
    	return this.hashCode() == obj.hashCode();
    }
	
    
    // -------------------------------- GETTER / SETTER --------------------------------
    
    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }	
}
