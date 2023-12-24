package com.pollgpt.pollgpt.data;

import lombok.Data;

import java.util.List;

@Data
public class UserPollResult {
    private String question;
    private List<String> options;
    private int answer;

    public UserPollResult(String question, List<String> options, int answer) {
        this.question = question;
        this.options = options;
        this.answer = answer;
    }
}
