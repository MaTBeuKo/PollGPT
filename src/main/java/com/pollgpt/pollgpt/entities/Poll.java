package com.pollgpt.pollgpt.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "polls")
public class Poll {
    long chatId;
    String question;
    @Temporal(TemporalType.TIMESTAMP)
    Timestamp timePosted;
    @Id
    long pollId;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "chatId", referencedColumnName = "chatId", insertable = false, updatable = false)
    private Chat chat;

    public Poll(long chatId, String question, Timestamp timePosted, long pollId) {
        this.pollId = pollId;
        this.question = question;
        this.timePosted = timePosted;
        this.chatId = chatId;
    }

    public Poll() {
    }

}
