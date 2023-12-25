package com.pollgpt.pollgpt.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "answers_description")
@IdClass(AnswerDescriptionId.class)
public class AnswerDescription {
    @Id
    private long pollId;
    @Id
    private int optionId;
    private String optionText;

    public AnswerDescription(long pollId, int optionId, String optionText) {
        this.pollId = pollId;
        this.optionId = optionId;
        this.optionText = optionText;
    }

    public AnswerDescription() {

    }
}
