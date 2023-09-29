package com.example.sampleapp;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MySecurityConfig {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
            .oauth2Login(oauthLogin ->
                oauthLogin.userInfoEndpoint(userInfo ->
                    userInfo.userAuthoritiesMapper(userAuthoritiesMapper())))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/protected").authenticated()
                .requestMatchers("/protected/normal").hasAnyRole("admin", "normal")
                .requestMatchers("/protected/admin").hasRole("admin")
                .anyRequest().permitAll());
        return http.build();
    }

    /*
     * Map realm_access.roles claim to GrantedAuthority.
     * These are from ID token, not access token.
     * You should enable "Add ID Token" flag on "Client Scopes > roles > Mappers > realm roles".
     * Taken from: https://kazuhira-r.hatenablog.com/entry/2022/09/08/015028
     */
    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>(authorities);

            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority) {
                    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;
                    OidcIdToken idToken = oidcUserAuthority.getIdToken();
                    Map<String, Object> claims = idToken.getClaims();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
                    if (realmAccess != null) {
                        @SuppressWarnings("unchecked")
                        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
                        if (roles != null) {
                            roles.forEach(role -> {
                                mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                            });
                        }
                    }
                }
            });

            return mappedAuthorities;
        };
    }

}
