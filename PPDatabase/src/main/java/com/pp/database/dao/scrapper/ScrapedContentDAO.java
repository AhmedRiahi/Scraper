package com.pp.database.dao.scrapper;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.scrapper.descriptor.listeners.ScrapedContent;
import org.springframework.stereotype.Repository;

@Repository
public class ScrapedContentDAO extends PPDAO<ScrapedContent>{

	public ScrapedContentDAO() {
		super(ScrapedContent.class);
	}
	
}
