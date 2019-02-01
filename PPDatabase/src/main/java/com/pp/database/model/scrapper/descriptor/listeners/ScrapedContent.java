package com.pp.database.model.scrapper.descriptor.listeners;

import com.pp.database.kernel.PPEntity;
import lombok.*;
import org.jsoup.nodes.Element;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Transient;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ScrapedContent extends PPEntity{

	@Transient
	private Element content;

	private String contentString;
	private String contentListenerName;
	
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
	
}
