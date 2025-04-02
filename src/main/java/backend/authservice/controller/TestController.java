package backend.authservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/subscriber")
    public String greetingSubscriber() {
        return "Hello subscriber!";
    }

    @GetMapping("/admin")
    public String greetingAdmin() {
        return "Hello admin!";
    }
}
