package com.pollgpt.pollgpt;
import com.pollgpt.pollgpt.entities.Answer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RequestGenerator {
    public String basicRequest(List<Answer> answers) {
        String pre = "I'll provide you a few polls in a following format: Poll question : User answer ." +
                " You should make assumptions about this user referring to their answers and" +
                " tell it like you was talking to them personally. тык means, that person refused to answer this question.\n";
        StringBuilder data = new StringBuilder();
        for (var answer : answers) {
            data.append(answer.getPoll().getQuestion());
            data.append(" : ");
            data.append(answer.getAnswerDescription().getOptionText());
            data.append("\n");
        }
        return pre + data;
    }
}
