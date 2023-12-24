package com.pollgpt.pollgpt.entities;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
@Embeddable
public class AnswerId implements Serializable {
    private long userId;
    private long messageId;
    private int answerId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnswerId answerId1 = (AnswerId) o;
        return userId == answerId1.userId && messageId == answerId1.messageId && answerId == answerId1.answerId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, messageId, answerId);
    }
}
