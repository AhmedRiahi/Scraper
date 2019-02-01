package com.pp.database.model.scrapper.descriptor.listeners;

import com.pp.database.kernel.PPEntity;
import org.jsoup.nodes.Element;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Transient;

@Entity
public class ScrapedContent extends PPEntity{

	@Transient
	private Element content;

	private String contentString;
	private String contentListenerName;
	
	public ScrapedContent(){}
	
	public ScrapedContent(Element content,String contentListenerName){
		this.content = content;
		this.contentListenerName = contentListenerName;
	}
	
	@PrePersist 
	private void prePersist(){
		if(this.content != null){
            this.contentString = this.content.text();
        }
	}
	
	@Override
	public String toString() {
		return contentListenerName;
	}
	
	// -------------------------------- GETTER / SETTER --------------------------------
	
	public String getContentListenerName() {
		return contentListenerName;
	}

	public void setContentListenerName(String contentListenerName) {
		this.contentListenerName = contentListenerName;
	}

	public Element getContent() {
		return content;
	}
	
	public void setContent(Element content) {
		this.content = content;
	}
	
}
