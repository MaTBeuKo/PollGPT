package com.pollgpt.pollgpt.gpt;

import com.pollgpt.pollgpt.external.G4FExecutor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@PropertySource("classpath:application.properties")
public class Gpt4Free implements GptProvider {
    @Autowired
    public Gpt4Free(G4FExecutor executor, G4FStrategy strategy) {
        this.executor = executor;
        this.strategy = strategy;
    }

    private final G4FExecutor executor;
    @Setter
    private G4FStrategy strategy;

    private String fileName(G4FStrategy strategy) {
        return strategy.name().toLowerCase() + ".py";
    }

    @Override
    public String ask(String message) {
        try {
            return executor.execute(fileName(strategy), message);
        } catch (IOException | InterruptedException ex) {
            System.out.println("Error while asking gpt: " + ex.getMessage());
            return "Im stupid :|";
        }
    }
}
