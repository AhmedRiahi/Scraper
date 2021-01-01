package com.pp.dashboard.config;

import com.pp.dashboard.service.DashboardUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private DashboardUserService dashboardUserService;

    @Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(this.dashboardUserService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic()
                .and()
                .authorizeRequests()
                .antMatchers("/", "/login", "/views/login/index.html", "/static/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and().logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .and().csrf().disable();

        http.logout().permitAll();
        http.logout().logoutSuccessHandler((new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)));
        http.sessionManagement().disable();
        http.headers().frameOptions().sameOrigin();
    }
}
