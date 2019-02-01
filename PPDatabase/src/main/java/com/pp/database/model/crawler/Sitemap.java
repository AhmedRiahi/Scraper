package com.pp.database.model.crawler;

import com.pp.database.kernel.PPEntity;

import java.util.Date;

public class Sitemap extends PPEntity{
	
	private String domainName;
	private CrawlLinksDataSet1 cld;
	private Date lastCheckingDate;
	
	public Sitemap(String domainName){
		this.domainName = domainName;
		this.cld = new CrawlLinksDataSet1();
	}
	
	// -------------------------------- GETTER / SETTER --------------------------------

	public String getDomainName() {
		return domainName;
	}
	
	public CrawlLinksDataSet1 getCld() {
		return cld;
	}

	public void setCld(CrawlLinksDataSet1 cld) {
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
