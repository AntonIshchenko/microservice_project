package com.example.securityserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig extends GlobalAuthenticationConfigurerAdapter {

   @Override
   public void init(AuthenticationManagerBuilder auth) throws Exception {
      auth.inMemoryAuthentication()
            .withUser("admin")
            .password(passwordEncoder().encode("1234"))
            .roles("ADMIN")
            .and()
            .withUser("user")
            .password(passwordEncoder().encode("4321"))
            .roles("USER");
   }

   @Bean
   public BCryptPasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
   }

}
