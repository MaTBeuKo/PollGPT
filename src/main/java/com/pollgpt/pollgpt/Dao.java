package com.pollgpt.pollgpt;

import com.pollgpt.pollgpt.data.UserAnswer;
import com.pollgpt.pollgpt.data.UserPollResult;
import com.pollgpt.pollgpt.entities.Answer;
import com.pollgpt.pollgpt.entities.AnswerDescription;
import com.pollgpt.pollgpt.entities.Chat;
import com.pollgpt.pollgpt.entities.Poll;
import it.tdlight.jni.TdApi;
import jakarta.annotation.PreDestroy;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class Dao {
    private final SessionFactory sessionFactory;

    @Autowired
    public Dao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void AddPoll(long messageId, long chatId, String question, List<String> options, List<UserAnswer> userAnswers, Timestamp timestamp) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        session.persist(new Poll(chatId, question, timestamp, messageId));
        for (int i = 0; i < options.size(); i++) {
            session.persist(new AnswerDescription(messageId, i + 1, options.get(i)));
        }
        for (var answer : userAnswers) {
            for (var answerId : answer.getAnswersIds()) {
                session.persist(new Answer(answer.getUserId(), messageId, answerId));
            }
        }
        session.getTransaction().commit();
    }

    public List<Long> getUnreadChats(int count) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        return session.createNamedQuery("getUnreadChats", Chat.class).setMaxResults(count).stream().map(Chat::getChatId).toList();
    }

    public List<Answer> getUserAnswers(long userId) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        List<Answer> answers = session.createNamedQuery("getUserAnswers", Answer.class).setParameter("userId", userId).getResultList();
        session.getTransaction().commit();
        return answers;
    }

    public void updateChat(long chatId, boolean isFullyRead, long firstMessage) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Chat chat = session.get(Chat.class, chatId);
        chat.setFullyRead(isFullyRead);
        chat.setFirstMessage(firstMessage);
    }

    public long getFirstMessage(long chatId) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Chat chat = session.get(Chat.class, chatId);
        session.getTransaction().commit();
        if (chat == null) {
            return 0;
        }
        return chat.getFirstMessage();
    }

    @PreDestroy
    public void destructor() {
        sessionFactory.close();
    }
}
