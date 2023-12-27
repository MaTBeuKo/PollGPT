package com.pollgpt.pollgpt.entities;

import jakarta.persistence.*;

@Entity
@IdClass(PollChatId.class)
@Table(name = "pollchat")
public class PollChat {
    @Id
    long pollId;

    @Id
    long chatId;
    @ManyToOne
    @JoinColumn(name = "pollId", referencedColumnName = "pollId", insertable = false, updatable = false)
    Poll poll;

    public PollChat(long pollId, long chatId) {
        this.pollId = pollId;
        this.chatId = chatId;
    }

    public PollChat() {

    }
}
