package com.pollgpt.pollgpt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Configuration
@PropertySource("classpath:application.properties")
public class BotConfig {
    @Value("${bot.name}")
    String botName;
    @Value("${bot.token}")
    String botToken;
    @Value("${api.id}")
    int apiId;
    @Value("${api.hash}")
    String apiHash;
    @Value("${admin.id}")
    Long adminId;
    @Value("${sessionPath}")
    String sessionPath;
    @Value("${DatabaseDirectoryPath}")
    String DatabaseDirectoryPath;
    @Value("${DownloadedFilesDirectoryPath}")
    String DownloadedFilesDirectoryPath;
}
