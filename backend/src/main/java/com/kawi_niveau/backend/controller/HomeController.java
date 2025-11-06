package com.kawi_niveau.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class HomeController {

    @GetMapping("/home")
    public String home(Authentication authentication) {
        return "Hello " + authentication.getName();
    }
}