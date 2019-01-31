package com.pp.structureDetector.exception;

public class NoCapacityToDetectException extends Exception{
	
	public NoCapacityToDetectException(String algorithm,String method){
		super(algorithm+" : "+method);
	}
}
