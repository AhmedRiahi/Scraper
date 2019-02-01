package com.pp.dashboard.service;

import com.pp.database.dao.dashborad.PPUserDAO;
import com.pp.database.model.dashboard.PPUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardUserService implements UserDetailsService{

	@Autowired
	private PPUserDAO userDAO;
	
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		PPUser fmsUser = this.userDAO.findByEmail(email);
		if(fmsUser == null) {
			throw new UsernameNotFoundException(email);
		}
		List<GrantedAuthority> list = new ArrayList<>();
		//TODO handle role dynamilcally
        list.add(new SimpleGrantedAuthority("ACTUATOR"));
		return new User(fmsUser.getUsername(), fmsUser.getPassword(), list);
	}

}
