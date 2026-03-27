package com.example.coderunner.service;

import com.example.coderunner.config.JwtUtil;
import com.example.coderunner.entity.User;
import com.example.coderunner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User register(User user) {
        return userRepository.save(user);
    }

    public String login(String username, String password) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }

        return JwtUtil.generateToken(user.getId());
    }
}