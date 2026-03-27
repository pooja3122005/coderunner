package com.example.coderunner.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.coderunner.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username); // ✅ NOT Optional
}