package org.sipfoundry.commons.security;

import jakarta.servlet.ServletContext; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Configuration
public class SipXWebSecurityConfigurerAdapter {

    @Autowired
    private ServletContext context;

    private static final String SIPXCONFIG_PATH = "/sipxconfig";
    private static final String AUTHENTICATION_MANAGER = "authenticationManager";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/**")).authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.realmName("SipXecs"))
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        // Ensure SIPXCONFIG_PATH is valid and context is properly initialized
        ServletContext sipxconfigCtx = context.getContext(SIPXCONFIG_PATH);
        if (sipxconfigCtx != null) {
            ApplicationContext webContext = WebApplicationContextUtils
                    .getRequiredWebApplicationContext(sipxconfigCtx);
            return (ProviderManager) webContext.getBean(AUTHENTICATION_MANAGER);
        } else {
            throw new IllegalStateException("ServletContext path " + SIPXCONFIG_PATH + " not found.");
        }
    }
}