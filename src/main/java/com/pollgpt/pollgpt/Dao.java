package com.pollgpt.pollgpt;

import com.pollgpt.pollgpt.data.UserAnswer;
import com.pollgpt.pollgpt.entities.*;
import jakarta.annotation.PreDestroy;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class Dao {
    private final SessionFactory sessionFactory;

    @Autowired
    public Dao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void AddPoll(long pollId, long chatId, long fromChatId, String question, List<String> options, List<UserAnswer> userAnswers, Timestamp timestamp) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        Chat chat = session.get(Chat.class, chatId);
        if (chat == null) {
            chat = new Chat();
            chat.setChatId(chatId);
            session.persist(chat);
        }
        session.merge(new Poll(question, timestamp, pollId));
        session.merge(new PollChat(pollId, chatId));
        if (chatId != fromChatId) {
            session.merge(new PollChat(pollId, fromChatId));
        }

        for (int i = 0; i < options.size(); i++) {
            session.merge(new AnswerDescription(pollId, i, options.get(i)));
        }
        for (var answer : userAnswers) {
            for (var answerId : answer.getAnswersIds()) {
                session.merge(new Answer(answer.getUserId(), pollId, answerId));
            }
        }
        session.getTransaction().commit();
    }

    public void addChat(long chatId) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Chat chat = new Chat();
        chat.setChatId(chatId);
        session.merge(chat);
        session.getTransaction().commit();
    }

    public List<Long> getUnreadChats(int count) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        List<Long> res = session.createNamedQuery("getUnreadChats", Chat.class).setMaxResults(count).stream().map(Chat::getChatId).toList();
        session.getTransaction().commit();
        return res;
    }

    public List<Answer> getUserAnswers(long userId, int amount) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        List<Answer> answers = session.createNamedQuery("getUserAnswers", Answer.class).setParameter("userId", userId)
                .setMaxResults(amount).getResultList();
        session.getTransaction().commit();
        return answers;
    }

    public void updateChat(long chatId, boolean isFullyRead, long firstMessage) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        Chat chat = session.get(Chat.class, chatId);
        chat.setFullyRead(isFullyRead);
        chat.setFirstMessage(firstMessage);
        session.getTransaction().commit();
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
