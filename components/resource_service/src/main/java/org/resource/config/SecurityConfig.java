package org.resource.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
public class SecurityConfig {

   @Bean
   SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http.mvcMatcher("/resources/**")
            .authorizeRequests()
            .mvcMatchers("/resources/**")
            .access("hasAuthority('SCOPE_songs.read')")
            .and()
            .oauth2ResourceServer()
            .jwt();
      return http.build();
   }

}
