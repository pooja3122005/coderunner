package com.example.coderunner.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.example.coderunner.entity.User;
//import java.util.List;
@Entity
public class CodeExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String code;

    @Column(columnDefinition = "TEXT")
    private String output;

    private LocalDateTime executedAt;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}