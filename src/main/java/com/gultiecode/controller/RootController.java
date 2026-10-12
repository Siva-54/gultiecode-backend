package com.gultiecode.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> rootWelcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("app", "Gultie Code API");
        response.put("status", "UP");
        response.put("message", "Welcome to Gultie Code REST API Engine. Access the Web Frontend at http://localhost");
        response.put("documentation", "http://localhost:8080/actuator/health");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> apiWelcome() {
        return rootWelcome();
    }
}
