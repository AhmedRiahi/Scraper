package com.pp.database.dao.crawler;

import com.pp.database.kernel.PPDAO;
import com.pp.database.model.crawler.Sitemap;

public class SitemapDAO extends PPDAO<Sitemap>{

	public SitemapDAO(){
		super(Sitemap.class);
	}
}
