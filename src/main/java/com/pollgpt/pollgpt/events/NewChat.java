package com.pollgpt.pollgpt.events;

import it.tdlight.jni.TdApi;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class NewChat  extends ApplicationEvent {
    public NewChat(Object source, TdApi.Chat chat) {
        super(source);
        this.chat = chat;
    }

    TdApi.Chat chat;
}
