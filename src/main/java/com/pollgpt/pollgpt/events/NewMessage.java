package com.pollgpt.pollgpt.events;

import it.tdlight.jni.TdApi;
import lombok.Data;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NewMessage extends ApplicationEvent {
    TdApi.UpdateNewMessage update;

    public NewMessage(Object source, TdApi.UpdateNewMessage update) {
        super(source);
        this.update = update;
    }

}
