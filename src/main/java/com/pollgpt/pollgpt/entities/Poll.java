package com.pollgpt.pollgpt.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@Table(name = "polls")
public class Poll {
    String question;
    @Temporal(TemporalType.TIMESTAMP)
    Timestamp timePosted;
    @Id
    long pollId;
    @OneToMany(mappedBy = "poll")
    List<PollChat> pollChat;

    public Poll(String question, Timestamp timePosted, long pollId) {
        this.pollId = pollId;
        this.question = question;
        this.timePosted = timePosted;
    }

    public Poll() {
    }

}
