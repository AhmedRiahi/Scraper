package com.pp.database.dao.crawler;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.crawler.CrawledContent;
import org.springframework.stereotype.Repository;

@Repository
public class CrawledContentDAO extends PPDAO<CrawledContent>{

	public CrawledContentDAO(){
		super(CrawledContent.class);
	}
}
