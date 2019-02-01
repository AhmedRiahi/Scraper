package com.pp.database.model.crawler;

import lombok.Data;
import org.mongodb.morphia.annotations.Transient;

import java.net.URL;

@Data
public class Link{
	
	@Transient
    private URL url;	
	private String urlString;
	
    public Link(URL url){
        this.url = url;
        this.urlString = this.url.toString();
    }
}
