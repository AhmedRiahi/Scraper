package com.pp.database.kernel;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;


@Entity
@XmlRootElement
public abstract class PPEntity{
	
	@Id
	protected ObjectId id;
	private Date creationDate;
	private Date lastModificationDate;


	// -------------------------------- GETTER / SETTER --------------------------------
	
	@XmlElement(
		name="stringId",
		type=String.class
	)
	public String getStringId(){
		if(this.id != null)
			return this.id.toHexString();
		return "";
	}
	
	public void setStringId(String stringId){
		this.id = new ObjectId(stringId);
	}
	
	@XmlTransient
	public ObjectId getId() {
		return id;
	}
	
	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public Date getLastModificationDate() {
		return lastModificationDate;
	}
	
	public void setLastModificationDate(Date lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}
	
}
