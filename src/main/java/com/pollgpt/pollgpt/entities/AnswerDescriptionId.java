package com.pollgpt.pollgpt.entities;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;


@Data
public class AnswerDescriptionId implements Serializable {
    private long messageId;
    private int optionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnswerDescriptionId that = (AnswerDescriptionId) o;
        return messageId == that.messageId && optionId == that.optionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, optionId);
    }
}
