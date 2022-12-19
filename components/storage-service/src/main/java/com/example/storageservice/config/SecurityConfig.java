package com.example.storageservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableResourceServer
public class SecurityConfig extends WebSecurityConfigurerAdapter {

//   @Bean
//   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//      http.authorizeRequests()
//            .antMatchers(HttpMethod.GET, "/storages").hasAnyRole("USER", "ADMIN")
//            .and().authorizeRequests()
//            .antMatchers(HttpMethod.DELETE, "/storages").hasRole("ADMIN")
//            .and().authorizeRequests()
//            .antMatchers(HttpMethod.POST, "/storages").hasRole("ADMIN");
//      return http.build();
//   }


   @Override
   public void configure(WebSecurity security) throws Exception {
      security.ignoring().antMatchers("/storages/type", "/actuator/prometheus");
   }

   @Override
   protected void configure(HttpSecurity http) throws Exception {
      http.authorizeRequests()
            .antMatchers(HttpMethod.GET,"/storages").hasAnyRole("USER", "ADMIN")
            .antMatchers(HttpMethod.DELETE,"/storages").hasRole("ADMIN")
            .antMatchers(HttpMethod.POST,"/storages").hasRole("ADMIN");
   }
}

