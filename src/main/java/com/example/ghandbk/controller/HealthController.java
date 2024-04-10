package com.example.ghandbk.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/health")
@RestController
public class HealthController {

    @GetMapping("getHealth")
    public ResponseEntity<String> getHealth() {
        return new ResponseEntity("Ok", HttpStatus.ACCEPTED);
    }
}
