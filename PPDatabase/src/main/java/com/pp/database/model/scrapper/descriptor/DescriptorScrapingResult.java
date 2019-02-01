package com.pp.database.model.scrapper.descriptor;

import com.pp.database.kernel.PPEntity;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import com.pp.database.model.scrapper.descriptor.listeners.ScrapedContent;
import lombok.Data;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Entity
public class DescriptorScrapingResult extends PPEntity{

	@Reference
	private DescriptorModel descriptor;
	private String dsmId;
	private Map<ScrapedContent,List<ScrapedContent>> scrapedContents;
	
	public DescriptorScrapingResult(){
		this.scrapedContents = new HashMap<>();
	}
	
	
	public void addScrapedContent(ScrapedContent scParent,ScrapedContent sc){
		List<ScrapedContent> scChilds = this.scrapedContents.getOrDefault(scParent, new ArrayList<>());
		scChilds.add(sc);
		this.scrapedContents.put(scParent,scChilds);
		this.scrapedContents.put(sc, new ArrayList<>());
	}
	
	public List<ScrapedContent> getScrapedContentByCL(ContentListenerModel cl){
		return this.scrapedContents.keySet()
				.stream()
				.filter(s -> s.getContentListenerName().equals(cl.getName()))
				.collect(Collectors.toList());
	}

	public List<ScrapedContent> getChildsOfWithCL(ScrapedContent scParent,String clName){

		return this.scrapedContents.get(scParent).stream().filter(scChild -> scChild.getContentListenerName().equals(clName)).collect(Collectors.toList());
	}

	public List<ScrapedContent> getAllScrapedContents(){
		List<ScrapedContent> allScrapedContents = new ArrayList<>();
		allScrapedContents.addAll(this.scrapedContents.keySet().stream().collect(Collectors.toList()));
		allScrapedContents.addAll(this.scrapedContents.values().stream().flatMap(List::stream).collect(Collectors.toList()).stream().distinct().collect(Collectors.toList()));
		return allScrapedContents;
	}

}
