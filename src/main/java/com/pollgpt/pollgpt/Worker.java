package com.pollgpt.pollgpt;

import com.pollgpt.pollgpt.data.UserAnswer;
import com.pollgpt.pollgpt.entities.Answer;
import com.pollgpt.pollgpt.events.NewMessage;
import com.pollgpt.pollgpt.gpt.GptProvider;
import it.tdlight.jni.TdApi;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@PropertySource("classpath:application.properties")
public class Worker implements ApplicationListener<NewMessage> {
    private final TelegramBot bot;
    private final BotConfig config;
    private final Dao dao;
    private final GptProvider provider;
    private final RequestGenerator generator;
    @Value("${worker.messagesPerRequest}")
    private final int messagesPerRequest = 100;
    @Value("${worker.chatsPerUpdate}")
    private final int chatsPerUpdate = 5;

    @Autowired
    public Worker(TelegramBot bot, BotConfig config, Dao dao, GptProvider provider, RequestGenerator generator) {
        this.bot = bot;
        this.config = config;
        this.dao = dao;
        this.provider = provider;
        this.generator = generator;
    }

    public void addPoll(TdApi.Message message) throws ExecutionException, InterruptedException {
        if (!(message.content instanceof TdApi.MessagePoll poll)) return;
        String question = poll.poll.question;
        List<String> options = Arrays.stream(poll.poll.options).map(x -> x.text).toList();
        List<UserAnswer> answers = new ArrayList<>();
        Map<Long, List<Integer>> userToAnswers = new HashMap<>();
        for (int i = 0; i < options.size(); i++) {
            var voters = bot.getPollVoters(message.chatId, message.id, i, poll.poll.options[i].voterCount);
            for (long voterId : voters) {
                var userAnswers = userToAnswers.getOrDefault(voterId, new ArrayList<Integer>());
                userAnswers.add(i);
                userToAnswers.put(voterId, userAnswers);
            }
        }
        for (var entry : userToAnswers.entrySet()) {
            answers.add(new UserAnswer(entry.getKey(), entry.getValue()));
        }
        dao.AddPoll(message.id, message.chatId, question, options, answers, new Timestamp(message.date * 1000L));
    }

    public void addPolls(long chatId, long fromMessageId, int messageAmount) throws ExecutionException, InterruptedException {
        Boolean fullyRead = false;
        var messages = bot.getMessages(chatId, fromMessageId, messageAmount, (x -> x.content instanceof TdApi.MessagePoll), fullyRead);
        for (var message : messages) {
            addPoll(message);
        }
        dao.updateChat(chatId, fullyRead, fromMessageId);
    }

    @Scheduled(fixedRate = 5000)
    public void loadPolls() {
        try {
            bot.loadMoreChats();
        }catch (Exception e){

        }
        //bot.loadMoreChats();
//        List<Long> chats = dao.getUnreadChats(chatsPerUpdate);
//        for (var chatId : chats) {
//            long fromMessageId = dao.getFirstMessage(chatId);
//            try {
//                addPolls(chatId, fromMessageId, messagesPerRequest);
//            } catch (ExecutionException | InterruptedException ex) {
//                System.out.println("Exception while adding polls at schedule, chatId: " + chatId + ". " + ex.getMessage());
//            }
//        }
    }

    @PostConstruct
    public void doSomething() {
        //        System.out.println(provider.ask("hello, how are you doing?"));
//        try {
//            long chatId = -1001292617540L;
//            addPolls(chatId, 1);
//            System.out.println("done");
//        } catch (Exception ex) {
//            System.out.println("Exception: " + ex.getMessage());
//        }
    }

    @Override
    public void onApplicationEvent(@Nonnull NewMessage newMessage) {
        TdApi.Message message = newMessage.getUpdate().message;
        if (!(message.content instanceof TdApi.MessageText content)) {
            return;
        }
        if (!content.text.text.startsWith("/")) {
            return;
        }
        Scanner scanner = new Scanner(content.text.text);
        String command = scanner.next();
        long id = message.chatId;
        switch (command) {
            case "/help" -> {
                bot.sendMessage(id, "No help for you!");
            }
            case "/me" -> {
                List<Answer> answers = dao.getUserAnswers(id);
                if (!answers.isEmpty()) {
                    String request = generator.basicRequest(answers);
                    String result = provider.ask(request);
                    bot.sendMessage(id, result);
                } else {
                    bot.sendMessage(id, "Sorry, I don't know anything about you!");
                }
            }
        }
    }


}
