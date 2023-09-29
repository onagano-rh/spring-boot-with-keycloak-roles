package com.example.sampleapp;

import java.util.Map;

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
    
}
