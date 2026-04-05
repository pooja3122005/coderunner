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
            String input = request.getStdin(); // ✅ NEW

            // ===================== JAVA =====================
            if ("java".equalsIgnoreCase(language)) {

                try (FileWriter w = new FileWriter("Main.java")) {
                    w.write(code);
                }

                ProcessBuilder compilePb = new ProcessBuilder("javac", "Main.java");
                compilePb.redirectErrorStream(true);
                Process compile = compilePb.start();

                String compileOut = drain(compile.getInputStream());
                compile.waitFor();

                if (compile.exitValue() != 0) {
                    response.setStdout("");
                    response.setStderr(compileOut);
                    response.setExitCode(1);
                    saveExecution(token, code, language, compileOut);
                    return response;
                }

                ProcessBuilder runPb = new ProcessBuilder("java", "Main");
                runPb.redirectErrorStream(true);
                Process run = runPb.start();

                // ✅ SEND INPUT
                writeInput(run, input);

                String output = drain(run.getInputStream());
                run.waitFor();

                response.setStdout(output);
                response.setStderr("");
                response.setExitCode(run.exitValue());
                saveExecution(token, code, language, output);
                return response;
            }

            // ===================== PYTHON =====================
            else if ("python".equalsIgnoreCase(language)) {

                try (FileWriter w = new FileWriter("script.py")) {
                    w.write(code);
                }

                ProcessBuilder pb = new ProcessBuilder("python", "script.py");
                pb.redirectErrorStream(true);
                Process run = pb.start();

                writeInput(run, input);

                String output = drain(run.getInputStream());
                run.waitFor();

                response.setStdout(output);
                response.setStderr("");
                response.setExitCode(run.exitValue());
                saveExecution(token, code, language, output);
                return response;
            }

            // ===================== JAVASCRIPT =====================
            else if ("javascript".equalsIgnoreCase(language)) {

                try (FileWriter w = new FileWriter("script.js")) {
                    w.write(code);
                }

                ProcessBuilder pb = new ProcessBuilder("node", "script.js");
                pb.redirectErrorStream(true);
                Process run = pb.start();

                writeInput(run, input);

                String output = drain(run.getInputStream());
                run.waitFor();

                response.setStdout(output);
                response.setStderr("");
                response.setExitCode(run.exitValue());
                saveExecution(token, code, language, output);
                return response;
            }

            else {
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

    // ===================== WRITE INPUT =====================
    private void writeInput(Process process, String input) throws IOException {
        if (input != null && !input.isEmpty()) {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream())
            );
            writer.write(input);
            writer.newLine();
            writer.flush();
            writer.close();
        }
    }

    // ===================== READ OUTPUT =====================
    private String drain(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    // ===================== SAVE =====================
    private void saveExecution(String token, String code, String language, String output) {
        try {
            Long userId = JwtUtil.extractUserId(token.replace("Bearer ", ""));
            User user = userRepository.findById(userId).orElse(null);

            CodeExecution execution = new CodeExecution();
            execution.setCode(code);
            execution.setLanguage(language);
            execution.setOutput(output);
            execution.setExecutedAt(LocalDateTime.now());
            execution.setUser(user);

            repository.save(execution);
        } catch (Exception ignored) {}
    }

    // ===================== HISTORY =====================
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