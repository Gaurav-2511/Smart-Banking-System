package com.smartbanking.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> healthCheck() {

        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("message", "Smart Banking Backend is running successfully");
        response.put("phase", "Phase 2 - Backend Setup");

        return ResponseEntity.ok(response);
    }
}