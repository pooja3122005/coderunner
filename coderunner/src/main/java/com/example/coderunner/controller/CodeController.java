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
            String code     = request.getCode();
            String language = request.getLanguage();

            // ─────────────────────────────────────────
            //  JAVA
            // ─────────────────────────────────────────
            if ("java".equalsIgnoreCase(language)) {

                // 1. Write source
                try (FileWriter w = new FileWriter("Main.java")) { w.write(code); }

                // 2. Compile — capture javac stderr
                ProcessBuilder compilePb = new ProcessBuilder("javac", "Main.java");
                compilePb.redirectErrorStream(true);          // merge stderr → stdout
                Process compile = compilePb.start();

                String compileOut = drain(compile.getInputStream());
                compile.waitFor();

                if (compile.exitValue() != 0) {
                    // Syntax / compile error — return immediately
                    response.setStdout("");
                    response.setStderr(compileOut);
                    response.setExitCode(1);
                    saveExecution(token, code, language, compileOut);
                    return response;
                }

                // 3. Run — merge stdout + stderr so exceptions appear inline
                ProcessBuilder runPb = new ProcessBuilder("java", "Main");
                runPb.redirectErrorStream(true);              // ✅ KEY FIX
                Process run = runPb.start();

                String output = drain(run.getInputStream());  // contains BOTH stdout & stderr
                run.waitFor();

                response.setStdout(output);
                response.setStderr("");                       // already merged into stdout
                response.setExitCode(run.exitValue());
                saveExecution(token, code, language, output);
                return response;

                // ─────────────────────────────────────────
                //  PYTHON
                // ─────────────────────────────────────────
            } else if ("python".equalsIgnoreCase(language)) {

                try (FileWriter w = new FileWriter("script.py")) { w.write(code); }

                ProcessBuilder pb = new ProcessBuilder("python", "script.py");
                pb.redirectErrorStream(true);
                Process run = pb.start();

                String output = drain(run.getInputStream());
                run.waitFor();

                response.setStdout(output);
                response.setStderr("");
                response.setExitCode(run.exitValue());
                saveExecution(token, code, language, output);
                return response;

                // ─────────────────────────────────────────
                //  JAVASCRIPT
                // ─────────────────────────────────────────
            } else if ("javascript".equalsIgnoreCase(language)) {

                try (FileWriter w = new FileWriter("script.js")) { w.write(code); }

                ProcessBuilder pb = new ProcessBuilder("node", "script.js");
                pb.redirectErrorStream(true);
                Process run = pb.start();

                String output = drain(run.getInputStream());
                run.waitFor();

                response.setStdout(output);
                response.setStderr("");
                response.setExitCode(run.exitValue());
                saveExecution(token, code, language, output);
                return response;

            } else {
                response.setStdout("");
                response.setStderr("Language not supported");
                response.setExitCode(1);
                return response;
            }

        } catch (Exception e) {
            response.setStdout("");
            response.setStderr(e.getMessage());
            response.setExitCode(1);
            return response;
        }
    }

    // ─────────────────────────────────────────
    //  HELPER: drain an InputStream to String
    // ─────────────────────────────────────────
    private String drain(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    // ─────────────────────────────────────────
    //  HELPER: save execution to DB
    // ─────────────────────────────────────────
    private void saveExecution(String token, String code, String language, String output) {
        try {
            Long userId = JwtUtil.extractUserId(token.replace("Bearer ", ""));
            User user   = userRepository.findById(userId).orElse(null);

            CodeExecution execution = new CodeExecution();
            execution.setCode(code);
            execution.setLanguage(language);
            execution.setOutput(output);
            execution.setExecutedAt(LocalDateTime.now());
            execution.setUser(user);
            repository.save(execution);
        } catch (Exception ignored) {}
    }

    // ─────────────────────────────────────────
    //  HISTORY
    // ─────────────────────────────────────────
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


