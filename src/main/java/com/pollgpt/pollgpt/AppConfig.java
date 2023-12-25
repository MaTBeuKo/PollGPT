package com.pollgpt.pollgpt;

import com.pollgpt.pollgpt.entities.*;
import com.pollgpt.pollgpt.gpt.G4FStrategy;
import it.tdlight.client.*;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableScheduling
@PropertySource("classpath:application.properties")
public class AppConfig {
    @Bean
    SimpleTelegramClientFactory simpleClientFactory() {
        return new SimpleTelegramClientFactory();
    }

    @Bean
    TDLibSettings settings(BotConfig config) {
        TDLibSettings settings = TDLibSettings.create(new APIToken(config.apiId, config.apiHash));
        Path sessionPath = Paths.get(config.sessionPath);
        settings.setDatabaseDirectoryPath(sessionPath.resolve(config.DatabaseDirectoryPath));
        settings.setDownloadedFilesDirectoryPath(sessionPath.resolve(config.DownloadedFilesDirectoryPath));
        return settings;
    }

    @Bean
    SessionFactory sessionFactory() {
        var config = new org.hibernate.cfg.Configuration().configure("hibernate.cfg.xml");
        config.addAnnotatedClass(Answer.class);
        config.addAnnotatedClass(AnswerDescription.class);
        config.addAnnotatedClass(Poll.class);
        config.addAnnotatedClass(Chat.class);
        return config.buildSessionFactory();
    }
    @Bean ProcessBuilder processBuilder(){
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        return processBuilder;
    }
    @Bean
    G4FStrategy g4FStrategy(){
        return G4FStrategy.SYNC_ONE;
    }
    @Value("${proxyFile}")
    private String proxyFile;
    @Bean
    List<String> proxies(){
        try {
            return Files.readAllLines(Path.of(proxyFile));
        }catch (IOException ex){
            System.out.println("Exception, couldn't read proxy file: " + ex.getMessage());
            return new ArrayList<>();
        }
    }
}
