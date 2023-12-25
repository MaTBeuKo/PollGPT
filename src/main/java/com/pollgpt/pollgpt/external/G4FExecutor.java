package com.pollgpt.pollgpt.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class G4FExecutor {
    @Autowired
    public G4FExecutor(ProcessBuilder builder, @Qualifier("proxies") List<String> proxy) {
        this.builder = builder;
        this.proxy =proxy;
    }

    private final List<String> proxy;
    private final Random random = new Random();

    private String randomProxy() {
        if (proxy.isEmpty()) {
            return "";
        } else {
            return proxy.get(random.nextInt(proxy.size()));
        }
    }

    private final ProcessBuilder builder;
    private final String src = "src\\main\\python\\";

    public String execute(String fileName, String args) throws IOException, InterruptedException {
        String outputFileName = fileName + ".txt";
        String prox = randomProxy();
        builder.command("python", src + fileName, outputFileName, '"' + args + '"', prox);
        Process process = builder.start();
        int exitCode = process.waitFor();
        String out = new String(process.getInputStream().readAllBytes());
        if (exitCode != 0) {
            throw new IOException("Exit code: " + exitCode + "\nProcess output:\n");
        }

        Path path = Path.of(outputFileName);
        var ex = Files.exists(path);
        String result = Files.readString(path);
        Files.delete(path);
        return result;
    }
}
