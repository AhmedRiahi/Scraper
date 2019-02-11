package com.pp.scrapper.core.signature;

import com.pp.database.model.scrapper.descriptor.signature.SignatureModel;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class XMLSignatureMatcher extends SignatureMatcher {

    @Override
    public Elements match(SignatureModel signature, Element rootElement, Element referenceElement) {
        switch(signature.getSignatureType()) {
            case XML_SELECTOR:
                String cssSelector = signature.getValue();
                cssSelector = cssSelector.substring(2);
                return referenceElement.select(cssSelector);

            default:
                throw new RuntimeException("Unknown signature " + signature);
        }
    }
}
