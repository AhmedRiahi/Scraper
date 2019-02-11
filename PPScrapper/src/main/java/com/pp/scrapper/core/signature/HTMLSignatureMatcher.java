package com.pp.scrapper.core.signature;

import com.pp.database.model.scrapper.descriptor.signature.SignatureModel;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class HTMLSignatureMatcher extends SignatureMatcher{

	
	public Elements match(SignatureModel signature, Element bodyElement, Element referenceElement){
		switch(signature.getSignatureType()){
		case CSS_REFERENCE_SELECTOR:
			String cssSelector = signature.getValue();
			cssSelector = cssSelector.substring(2);
			if(cssSelector.trim().isEmpty()){
				return new Elements(referenceElement);
			}
			return referenceElement.select(cssSelector);

		case CSS_SELECTOR:
			cssSelector = signature.getValue();
			return bodyElement.select(cssSelector);
			
		case DOM_CLASS:
			String domClass = signature.getValue();
			return bodyElement.getElementsByClass(domClass);
			
		case DOM_DISTANCE:
			break;
			
		case DOM_ID:
			String domId = signature.getValue();
			return new Elements(bodyElement.getElementById(domId));
			
		case DOM_INDEX:
			break;
			
		case DOM_REFERENCE_INDEX:
			break;
			
		case XPATH_SELECTOR:
			String xpathSelector = signature.getValue();
			xpathSelector = xpathSelector.substring(2);
			return bodyElement.select(xpathSelector);
			
		default:
			break;
		
		}
		return null;
	}
	
}
