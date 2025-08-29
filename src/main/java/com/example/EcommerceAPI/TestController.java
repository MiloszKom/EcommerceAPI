package com.example.EcommerceAPI;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String testEndpoint() {
        return "Test route working";
    }
}
