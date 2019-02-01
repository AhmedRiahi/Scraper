package com.pp.crawler.core;

import com.pp.crawler.exception.IrrelevantLinkException;
import com.pp.database.model.crawler.Link;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

public final class CrawlerUtils {
	
    public static HashSet<Link> detectLinks(URL referingURL,Document document){
		HashSet<Link> links = new HashSet<>();
		Elements elements = document.getElementsByTag("a");
        for(Element element : elements){
            String href = element.attr("href");
            if(href != null){
                try {
                    URL url = CrawlerUtils.resolveURL(referingURL, href);
                    Link link = new Link(url);
                    links.add(link);
                }catch(MalformedURLException ex) {
                	System.out.println("MalformedURLException :"+href);
                	ex.printStackTrace();
                	continue;
                } catch (IrrelevantLinkException e) {
                	
				}
            }
        }	
        return links;
    }
    
    public static URL resolveURL(URL referingURL, String href) throws IrrelevantLinkException, MalformedURLException{
    	
        String result = "";
        if(href.startsWith("http")){
        	return new URL(href);
        }
        if(href.startsWith("www.")){
        	return new URL("http://"+href);
        }
        if(href.startsWith("mailto:")){
        	throw new IrrelevantLinkException();
        }
        if(href.startsWith("whatsapp:")){
        	throw new IrrelevantLinkException();
        }
        if(href.startsWith("#")){
        	throw new IrrelevantLinkException();
        }
        if(href.startsWith("javascript")){
        	throw new IrrelevantLinkException();
        }
        if(href.startsWith("?")){
        	result = referingURL.getProtocol()+"://"+referingURL.getHost()+href;
        	return new URL(result);
        }
        if(href.startsWith("/") && href.length() > 1){
        	result = referingURL.getProtocol()+"://"+referingURL.getHost()+"/"+href;
        	return new URL(result);
        }
        if(href.startsWith("/") && href.length() == 1){
        	result = referingURL.getProtocol()+"://"+referingURL.getHost();
        	return new URL(result);
        }
        result = referingURL.getProtocol()+"://"+referingURL.getHost()+"/"+href;
        return new URL(result);
    }
    
    
    public static boolean isExternalLink(URL domain,URL url){
    	return !domain.getHost().equals(url.getHost());
    }

}
