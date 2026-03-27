package com.example.coderunner.controller;

import com.example.coderunner.entity.User;
import com.example.coderunner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody User user) {
        userService.register(user);
        return Map.of("message", "User registered successfully");
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User user) {
        String token = userService.login(user.getUsername(), user.getPassword());

        return Map.of(
                "token", token
        );
    }
}