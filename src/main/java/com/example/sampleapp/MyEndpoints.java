package com.example.sampleapp;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyEndpoints {

    @RequestMapping("/*")
    public Map<String, String> topPage() {
        return Map.of("message", "Here is the top page.");
    }

    @RequestMapping("/public")
    public Map<String, String> publicArea() {
        return Map.of("message", "Here is a public area.");
    }

    @RequestMapping("/protected")
    public Map<String, String> protectedArea() {
        OidcUser user = (OidcUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Map.of(
            "message", "Here is a protected area.",
            "username", user.getName());
    }

    @RequestMapping("/protected/normal")
    public Map<String, String> protectedNormalArea() {
        OidcUser user = (OidcUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        @SuppressWarnings("unchecked")
        Collection<GrantedAuthority> roles = (Collection<GrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        return Map.of(
            "message", "Here is a protected normal area.",
            "username", user.getName(),
            "authorities", String.valueOf(roles));
    }

    @RequestMapping("/protected/admin")
    public Map<String, String> protectedAdminArea() {
        OidcUser user = (OidcUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        @SuppressWarnings("unchecked")
        Collection<GrantedAuthority> roles = (Collection<GrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        return Map.of(
            "message", "Here is a protected admin area.",
            "username", user.getName(),
            "authorities", String.valueOf(roles));
    }

    // You can inspect your authentication as a JSON object.
    @RequestMapping("/inspect")
    public Object inspect() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
