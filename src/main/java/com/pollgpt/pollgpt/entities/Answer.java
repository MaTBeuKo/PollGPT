package com.pollgpt.pollgpt.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "answers")
@IdClass(AnswerId.class)
@NamedQuery(name = "getUserAnswers", query = "SELECT ans FROM Answer ans WHERE ans.userId = :userId ORDER BY rand()")
public class Answer {
    @Id
    private long userId;
    @Id
    private long pollId;
    @Id
    private int answerId;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinColumns({
            @JoinColumn(name = "pollId", referencedColumnName = "pollId", insertable = false, updatable = false),
            @JoinColumn(name = "answerId", referencedColumnName = "optionId", insertable = false, updatable = false)
    })
    private AnswerDescription answerDescription;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "pollId", referencedColumnName = "pollId", insertable = false, updatable = false)
    private Poll poll;

    public Answer(long userId, long pollId, int answerId) {
        this.userId = userId;
        this.pollId = pollId;
        this.answerId = answerId;
    }

    public Answer() {

    }
}
