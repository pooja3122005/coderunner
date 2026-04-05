package com.example.coderunner.dto;

public class CodeExecutionRequest {

    private String code;
    private String language;
    private String stdin;   // ✅ NEW FIELD

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStdin() {
        return stdin;
    }

    public void setStdin(String stdin) {
        this.stdin = stdin;
    }
}