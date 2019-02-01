package com.pp.structureDetector.abstractStructure;

import java.util.Comparator;

public class ClassRatesComparator implements Comparator<ClassRate>{

	@Override
	public int compare(ClassRate cr1, ClassRate cr2) {
		if(cr1.getRate() > cr2.getRate()){
			return -1;
		}else{
			if(cr1.getRate() < cr2.getRate()){
				return 1;
			}else{
				return cr1.getClassName().compareTo(cr2.getClassName());
			}
		}
	}
}
