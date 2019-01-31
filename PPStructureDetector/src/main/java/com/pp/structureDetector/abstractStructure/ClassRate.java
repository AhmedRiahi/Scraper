package com.pp.structureDetector.abstractStructure;

import java.util.Observable;

public class ClassRate extends Observable implements Comparable<ClassRate>{

	protected String className;
	protected int rate;
	protected boolean isNoise = false;
	
	public ClassRate(String name,int rate){
		this.className = name;
		this.rate = rate;
	}
	
	
	public void incrementRate(int increment){
		this.setRate(this.getRate()+increment);
	}
	
	@Override
	public int hashCode() {
		return (this.rate+this.className).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		ClassRate tmp = (ClassRate) obj;
		if(this.rate != tmp.rate) return false;
		return this.className.equalsIgnoreCase(tmp.getClassName());
	}
	
	@Override
	public String toString() {
		return this.className+"="+this.rate;
	}
	
	@Override
	public int compareTo(ClassRate o) {
		if(this.rate != o.rate){
			return this.rate-o.rate;
		}else{
			return this.className.compareTo(o.className);
		}
	}
	
	
	// -------------------------------- GETTER / SETTER --------------------------------

	public boolean isNoise() {
		return isNoise;
	}

	public void setNoise(boolean isNoise) {
		this.isNoise = isNoise;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		if(this.rate != rate){
			this.setChanged();
			this.notifyObservers(new ClassRate(this.className, rate));
			this.rate = rate;
			
		}
		
	}
}
