package com.pollgpt.pollgpt.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "answers")
@IdClass(AnswerId.class)
@NamedQuery(name = "getUserAnswers", query = "SELECT ans FROM Answer ans WHERE ans.userId = :userId")
public class Answer {
    @Id
    private long userId;
    @Id
    private long messageId;
    @Id
    private int answerId;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinColumns({
            @JoinColumn(name = "messageId", referencedColumnName = "messageId", insertable = false, updatable = false),
            @JoinColumn(name = "answerId", referencedColumnName = "optionId", insertable = false, updatable = false)
    })
    private AnswerDescription answerDescription;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "messageId", referencedColumnName = "messageId", insertable = false, updatable = false)
    private Poll poll;

    public Answer(long userId, long messageId, int answerId) {
        this.userId = userId;
        this.messageId = messageId;
        this.answerId = answerId;
    }

    public Answer() {

    }
}
