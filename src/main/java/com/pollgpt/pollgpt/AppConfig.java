package com.pollgpt.pollgpt;

import com.pollgpt.pollgpt.entities.*;
import it.tdlight.client.*;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@EnableScheduling
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
}
