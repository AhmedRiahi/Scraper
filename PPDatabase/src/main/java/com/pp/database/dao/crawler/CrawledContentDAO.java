package com.pp.database.dao.crawler;

import org.springframework.stereotype.Repository;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.crawler.CrawledContent;

@Repository
public class CrawledContentDAO extends PPDAO<CrawledContent>{

	public CrawledContentDAO(){
		super(CrawledContent.class);
	}
}
