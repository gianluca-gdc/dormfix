package com.dormfix.hello;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world endpoint for Phase 1. GET /api/hello returns a small JSON object
 * that the Compose frontend displays to prove the two halves can talk.
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public HelloResponse hello() {
        return new HelloResponse("Hello from the DormFix backend!", Instant.now().toString());
    }
}
