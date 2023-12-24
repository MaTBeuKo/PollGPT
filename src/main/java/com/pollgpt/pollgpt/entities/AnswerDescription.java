package com.pollgpt.pollgpt.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "answers_description")
@IdClass(AnswerDescriptionId.class)
public class AnswerDescription {
    @Id
    private long messageId;
    @Id
    private int optionId;
    private String optionText;

    public AnswerDescription(long messageId, int optionId, String optionText) {
        this.messageId = messageId;
        this.optionId = optionId;
        this.optionText = optionText;
    }

    public AnswerDescription() {

    }
}
