package com.pp.scrapper.core;

import com.pp.database.model.crawler.CrawledContent;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import com.pp.database.model.scrapper.descriptor.signature.SignatureType;
import com.pp.scrapper.core.signature.SignatureMatcher;
import com.pp.scrapper.core.signature.XMLSignatureMatcher;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public class PPXMLScraper extends PPScraper{

    @Autowired
    private XMLSignatureMatcher xmlSignatureMatcher;

    public PPXMLScraper(DescriptorModel descriptor, CrawledContent crawledContent) {
        super(descriptor, crawledContent);
    }

    @Override
    protected Element getRootElement() {
        return this.document.firstElementSibling();
    }

    @Override
    protected SignatureType getSelectionSignature() {
        return SignatureType.XML_SELECTOR;
    }

    @Override
    protected SignatureMatcher getSignatureMatcher() {
        return xmlSignatureMatcher;
    }
}
