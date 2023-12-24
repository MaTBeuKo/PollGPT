package com.pollgpt.pollgpt.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class ProcessExecutor {
    @Autowired
    public ProcessExecutor(ProcessBuilder builder) {
        this.builder = builder;
    }

    private final ProcessBuilder builder;

    public String execute(String fileName, String args) throws IOException, InterruptedException {
        builder.command("python", fileName, args);
        Process process = builder.start();
        int exitCode = process.waitFor();
        String result = new String(process.getInputStream().readAllBytes());

        if (exitCode != 0) throw new IOException("Exit code: " + exitCode);
        return result;
    }
}
