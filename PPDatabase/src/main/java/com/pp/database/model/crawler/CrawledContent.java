package com.pp.database.model.crawler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pp.database.kernel.PPEntity;
import lombok.Data;
import org.mongodb.morphia.annotations.Entity;

@Data
@Entity
public class CrawledContent extends PPEntity{

	private String url;
	@JsonIgnore
	private String contents;

}
