package com.pp.database.model.crawler;

import java.util.Date;

import com.pp.database.kernel.PPEntity;

public class Sitemap extends PPEntity{
	
	private String domainName;
	private CrawlLinksDataset cld;
	private Date lastCheckingDate;
	
	public Sitemap(String domainName){
		this.domainName = domainName;
		this.cld = new CrawlLinksDataset();
	}
	
	// -------------------------------- GETTER / SETTER --------------------------------

	public String getDomainName() {
		return domainName;
	}
	
	public CrawlLinksDataset getCld() {
		return cld;
	}

	public void setCld(CrawlLinksDataset cld) {
		this.cld = cld;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	
	public Date getLastCheckingDate() {
		return lastCheckingDate;
	}
	
	public void setLastCheckingDate(Date lastCheckingDate) {
		this.lastCheckingDate = lastCheckingDate;
	}
}
