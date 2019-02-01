package com.pp.database.model.dashboard;

import com.pp.database.kernel.PPEntity;

import javax.validation.constraints.NotNull;

public class PPUser extends PPEntity{

	
	@NotNull
	private String email;
	@NotNull
	private String username;
	@NotNull
	private String password;
	
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
}
