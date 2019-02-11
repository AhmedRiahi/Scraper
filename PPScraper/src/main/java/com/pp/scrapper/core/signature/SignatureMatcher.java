package com.pp.scrapper.core.signature;

import com.pp.database.model.scrapper.descriptor.signature.SignatureModel;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public abstract class SignatureMatcher {

    public abstract Elements match(SignatureModel signature, Element rootElement, Element referenceElement);
}
