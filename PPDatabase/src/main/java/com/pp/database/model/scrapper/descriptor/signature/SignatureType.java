package com.pp.database.model.scrapper.descriptor.signature;

import org.mongodb.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlEnum;

@Entity
@XmlEnum
public enum SignatureType {
	DOM_INDEX,
	DOM_REFERENCE_INDEX,
	DOM_ID,
	CSS_REFERENCE_SELECTOR,
	CSS_SELECTOR,
	DOM_CLASS,
	DOM_DISTANCE,
	XPATH_SELECTOR,
	XML_SELECTOR;
}
