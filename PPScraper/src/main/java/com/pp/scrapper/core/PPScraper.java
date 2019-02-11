package com.pp.scrapper.core;

import com.pp.database.model.crawler.CrawledContent;
import com.pp.database.model.scrapper.descriptor.DescriptorModel;
import com.pp.database.model.scrapper.descriptor.DescriptorScrapingResult;
import com.pp.database.model.scrapper.descriptor.listeners.ContentListenerModel;
import com.pp.database.model.scrapper.descriptor.listeners.ScrapedContent;
import com.pp.database.model.scrapper.descriptor.signature.SignatureModel;
import com.pp.database.model.scrapper.descriptor.signature.SignatureType;
import com.pp.scrapper.core.signature.SignatureMatcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class PPScraper {

    protected DescriptorModel descriptor;
    protected CrawledContent crawledContent;
    protected Document document;


    public PPScraper(DescriptorModel descriptor,CrawledContent crawledContent){
        this.descriptor = descriptor;
        this.crawledContent = crawledContent;
        this.document = Jsoup.parse(crawledContent.getContents());
    }

    protected abstract Element getRootElement();
    protected abstract SignatureType getSelectionSignature();
    protected abstract SignatureMatcher getSignatureMatcher();

    public DescriptorScrapingResult scrapDescriptor() throws IOException {
        DescriptorScrapingResult scrapingResult  = new DescriptorScrapingResult();
        scrapingResult.setDescriptor(descriptor);
        // Ordering ContentListeners : structural Order
        List<ContentListenerModel> orderedContentListeners = ScrapingUtils.generateOrderedContentListeners(descriptor,DescriptorModel.STRUCTURED_LISTENER);

        for(ContentListenerModel cl : orderedContentListeners){
            // Do not process static Listener
            if(cl.isStatic()){
                continue;
            }
            List<ScrapedContent> referenceScrapedContents = new ArrayList<>();
            List<ContentListenerModel> sourceContentListeners = descriptor.getStructureRelatedSources(cl);

            // search if reference element do exist : by default the reference element is the <body> one
            if(sourceContentListeners.size() > 0){
                // TODO : for the moment we are processing only one source which is the parent. The source could be the element upper to or at right ...
                referenceScrapedContents = scrapingResult.getScrapedContentByCL(sourceContentListeners.get(0));
            }else{
                referenceScrapedContents.add(new ScrapedContent(this.getRootElement(), "root"));
            }

            for(ScrapedContent referenceSC : referenceScrapedContents){
                Elements scrapedElements = this.scrapContentListener(cl, referenceSC.getContent());
                //Elements cleanScrapedElements = this.cleanScrapedElements(scrapedElements);
                for(Element element : scrapedElements){
                    ScrapedContent scrapedContent = new ScrapedContent(element,cl.getName());
                    scrapingResult.addScrapedContent(referenceSC,scrapedContent);
                }
            }
        }
        return scrapingResult;
    }

    private Elements scrapContentListener(ContentListenerModel contentListener,Element referenceElement){
        List<SignatureModel> signatures = contentListener.getSignatures();
        Elements elements = new Elements();
        for(SignatureModel signature : signatures){
            if(signature.getSignatureType().equals(this.getSelectionSignature())) {
                Elements tmpElements = this.getSignatureMatcher().match(signature, this.getRootElement(), referenceElement);
                elements.addAll(tmpElements);
            }
        }
        // TODO We must implement algorithm to validate that all element refers to only one, if not so there is an ambiguous information
        return elements;
    }

    public List<String> detectedAllLinks(){
        return this.document.select("a").stream().map(elem -> elem.attr("href")).collect(Collectors.toList());
    }

}
