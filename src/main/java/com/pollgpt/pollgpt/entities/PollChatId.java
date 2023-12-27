package com.pollgpt.pollgpt.entities;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class PollChatId implements Serializable {
    private long pollId;
    private long chatId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PollChatId that = (PollChatId) o;
        return pollId == that.pollId && chatId == that.chatId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pollId, chatId);
    }
}
