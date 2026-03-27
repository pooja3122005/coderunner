package com.example.coderunner.controller;

import com.example.coderunner.config.JwtUtil;
import com.example.coderunner.dto.CodeExecutionRequest;
import com.example.coderunner.dto.CodeExecutionResponse;
import com.example.coderunner.entity.CodeExecution;
import com.example.coderunner.entity.User;
import com.example.coderunner.repository.CodeExecutionRepository;
import com.example.coderunner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/code")
@CrossOrigin(origins = "*")
public class CodeController {

    @Autowired
    private CodeExecutionRepository repository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/execute")
    public CodeExecutionResponse executeCode(
            @RequestHeader("Authorization") String token,
            @RequestBody CodeExecutionRequest request) {

        CodeExecutionResponse response = new CodeExecutionResponse();

        try {

            String code = request.getCode();
            String language = request.getLanguage();

            Process run;

            // 🔥 HANDLE MULTIPLE LANGUAGES
            if ("java".equalsIgnoreCase(language)) {

                File file = new File("Main.java");
                FileWriter writer = new FileWriter(file);
                writer.write(code);
                writer.close();

                Process compile = Runtime.getRuntime().exec("javac Main.java");
                compile.waitFor();

                run = Runtime.getRuntime().exec("java Main");

            } else if ("python".equalsIgnoreCase(language)) {

                File file = new File("script.py");
                FileWriter writer = new FileWriter(file);
                writer.write(code);
                writer.close();

                run = Runtime.getRuntime().exec("python script.py");

            } else if ("javascript".equalsIgnoreCase(language)) {

                File file = new File("script.js");
                FileWriter writer = new FileWriter(file);
                writer.write(code);
                writer.close();

                run = Runtime.getRuntime().exec("node script.js");

            } else {
                response.setStdout("");
                response.setStderr("Language not supported");
                response.setExitCode(1);
                return response;
            }

            // 🔥 READ OUTPUT
            BufferedReader outputReader = new BufferedReader(
                    new InputStreamReader(run.getInputStream())
            );

            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(run.getErrorStream())
            );

            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

            String line;

            while ((line = outputReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            while ((line = errorReader.readLine()) != null) {
                error.append(line).append("\n");
            }

            run.waitFor();

            // 🔥 GET USER FROM JWT
            Long userId = JwtUtil.extractUserId(token.replace("Bearer ", ""));
            User user = userRepository.findById(userId).orElse(null);

            // 🔥 SAVE TO DB
            CodeExecution execution = new CodeExecution();
            execution.setCode(code);
            execution.setLanguage(language);
            execution.setOutput(output.toString() + error.toString());
            execution.setExecutedAt(LocalDateTime.now());
            execution.setUser(user);

            repository.save(execution);

            // 🔥 RESPONSE
            String finalOutput = output.toString();
            String finalError = error.toString();

            response.setStdout(finalOutput);
            response.setStderr(finalError);

// ✅ FIX: detect runtime errors properly
            if (finalError.contains("Exception") || finalError.contains("Error")) {
                response.setExitCode(1);
            } else {
                response.setExitCode(0);
            }

            return response;

        } catch (Exception e) {
            response.setStdout("");
            response.setStderr(e.getMessage());
            response.setExitCode(1);
            return response;
        }
    }

    // 🔥 USER-SPECIFIC HISTORY
    @GetMapping("/history")
    public List<CodeExecution> getHistory(@RequestHeader("Authorization") String token) {

        Long userId = JwtUtil.extractUserId(token.replace("Bearer ", ""));
        return repository.findByUserId(userId);
    }

    @DeleteMapping("/history/{id}")
    public String deleteHistory(@PathVariable Long id) {
        repository.deleteById(id);
        return "Deleted successfully";
    }
}