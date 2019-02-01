package com.pp.scrapper.core;


import com.pp.database.model.crawler.CrawledContent;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import com.pp.database.model.scrapper.descriptor.signature.SignatureType;
import com.pp.scrapper.core.signature.HTMLSignatureMatcher;
import com.pp.scrapper.core.signature.SignatureMatcher;
import org.jsoup.nodes.Element;


public class PPHTMLScrapper extends PPScraper{

    private HTMLSignatureMatcher htmlSignatureMatcher = new HTMLSignatureMatcher();

	public PPHTMLScrapper(DescriptorModel descriptor, CrawledContent crawledContent) {
		super(descriptor, crawledContent);
	}

	@Override
	protected Element getRootElement() {
		return this.document.body();
	}

    @Override
    protected SignatureType getSelectionSignature() {
        return SignatureType.CSS_REFERENCE_SELECTOR;
    }

    @Override
    protected SignatureMatcher getSignatureMatcher() {
        return htmlSignatureMatcher;
    }
}
