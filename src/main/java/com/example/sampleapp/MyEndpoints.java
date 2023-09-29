package com.example.sampleapp;

import java.util.Map;

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

    // You can inspect your Principal as a JSON object.
    @RequestMapping("/inspect")
    public Object inspectUser() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
