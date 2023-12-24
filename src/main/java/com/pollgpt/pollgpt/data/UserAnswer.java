package com.pollgpt.pollgpt.data;

import lombok.Data;

import java.util.List;

@Data
public class UserAnswer {
    private long userId;
    private List<Integer> answersIds;

    public UserAnswer(long userId, List<Integer> answersIds) {
        this.userId = userId;
        this.answersIds = answersIds;
    }
}
