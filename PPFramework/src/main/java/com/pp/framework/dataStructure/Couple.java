package com.pp.framework.dataStructure;

public class Couple<K,V> {

	private K key;
	private V value;
	
	public Couple(K key,V value){
		this.key = key;
		this.value = value;
	}

	// -------------------------------- GETTER / SETTER --------------------------------
	
	public K getKey() {
		return key;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}
}
