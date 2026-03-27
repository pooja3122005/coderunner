package com.example.coderunner.repository;

import com.example.coderunner.entity.CodeExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CodeExecutionRepository extends JpaRepository<CodeExecution, Long> {


    List<CodeExecution> findByUserId(Long userId);
}