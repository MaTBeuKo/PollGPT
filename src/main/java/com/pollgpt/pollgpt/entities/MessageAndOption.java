package com.pollgpt.pollgpt.entities;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class MessageAndOption implements Serializable {
    private long messageId;
    private long optionId;

    public MessageAndOption() {
    }

    public MessageAndOption(long messageId, long optionId) {
        this.messageId = messageId;
        this.optionId = optionId;
    }
}
