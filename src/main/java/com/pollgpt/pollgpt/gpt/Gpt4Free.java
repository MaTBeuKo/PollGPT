package com.pollgpt.pollgpt.gpt;

import com.pollgpt.pollgpt.external.ProcessExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@PropertySource("classpath:application.properties")
public class Gpt4Free implements GptProvider {
    @Autowired
    public Gpt4Free(ProcessExecutor executor) {
        this.executor = executor;
    }

    @Value("${g4f.path}")
    private String scriptPath;
    private final ProcessExecutor executor;

    String parse(String s) {
        var split = s.split("MMM");
        for (var t : split) {
            if (t.startsWith("start")) {
                return t.substring(5);
            }
        }
        return "no result ;(";
    }

    @Override
    public String ask(String message) {
        try {
            return parse(executor.execute(scriptPath, message));
        } catch (IOException | InterruptedException ex) {
            System.out.println("Error while asking gpt: " + ex.getMessage());
            return null;
        }
    }
}
