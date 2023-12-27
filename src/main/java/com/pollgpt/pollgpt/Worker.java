package com.pollgpt.pollgpt;

import com.pollgpt.pollgpt.data.UserAnswer;
import com.pollgpt.pollgpt.entities.Answer;
import com.pollgpt.pollgpt.events.NewChat;
import com.pollgpt.pollgpt.events.NewMessage;
import com.pollgpt.pollgpt.gpt.GptProvider;
import it.tdlight.jni.TdApi;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

@Service
@PropertySource("classpath:application.properties")
public class Worker {
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

    public void addPoll(TdApi.Message message, long chatId) throws ExecutionException, InterruptedException {
        if (!(message.content instanceof TdApi.MessagePoll poll) || poll.poll.isAnonymous) return;
        String question = poll.poll.question;
        List<String> options = Arrays.stream(poll.poll.options).map(x -> x.text).toList();
        List<UserAnswer> answers = new ArrayList<>();
        Map<Long, List<Integer>> userToAnswers = new HashMap<>();
        for (int i = 0; i < options.size(); i++) {
            var voters = bot.getPollVoters(message.chatId, message.id, i, poll.poll.options[i].voterCount);
            for (long voterId : voters) {
                var userAnswers = userToAnswers.getOrDefault(voterId, new ArrayList<>());
                userAnswers.add(i);
                userToAnswers.put(voterId, userAnswers);
            }
        }
        for (var entry : userToAnswers.entrySet()) {
            answers.add(new UserAnswer(entry.getKey(), entry.getValue()));
        }
        dao.AddPoll(poll.poll.id, chatId, message.forwardInfo == null ? chatId : message.forwardInfo.fromChatId,
                question, options, answers, new Timestamp(message.date * 1000L));
    }

    public void addPolls(long chatId, long fromMessageId, int messageAmount) throws ExecutionException, InterruptedException {
        AtomicLong updatableMessageId = new AtomicLong(fromMessageId);
        var messages = bot.getMessages(chatId, updatableMessageId, messageAmount, (x -> x.content instanceof TdApi.MessagePoll));
        for (var message : messages) {
            addPoll(message, chatId);
        }
        dao.updateChat(chatId, updatableMessageId.get() == bot.getMessageDivider(), updatableMessageId.get());
    }

    @Scheduled(fixedRate = 1000)
    public void loadPolls() {
        List<Long> chats = dao.getUnreadChats(chatsPerUpdate);
        for (var chatId : chats) {
            long fromMessageId = dao.getFirstMessage(chatId);
            try {
                addPolls(chatId, fromMessageId, messagesPerRequest);
            } catch (ExecutionException | InterruptedException ex) {
                System.out.println("Exception while adding polls at schedule, chatId: " + chatId + ". " + ex.getMessage());
            }
        }
    }


    @PostConstruct
    public void doSomething() {
        //System.out.println(provider.ask("say hello in russian"));
//        long chatId = -1001292617540L;
//        try {
//            addPolls(chatId, 0, 10);
//        } catch (Exception ex) {
//            System.out.println(ex.getMessage());
//        }
        //        bot.sendMessage(config.adminId, "hello");
        //        System.out.println(provider.ask("hello, how are you doing?"));
//        try {
//            long chatId = -1001292617540L;
//            addPolls(chatId, 1);
//            System.out.println("done");
//        } catch (Exception ex) {
//            System.out.println("Exception: " + ex.getMessage());
//        }
    }

    @EventListener
    public void onApplicationEvent(@Nonnull NewChat newChat) {
        dao.addChat(newChat.getChat().id);
    }

    @EventListener
    public void onApplicationEvent(@Nonnull NewMessage newMessage) {
        TdApi.Message message = newMessage.getUpdate().message;
        long chatId = message.chatId;
        if (chatId != config.adminId || message.isOutgoing) return;
        if (message.content instanceof TdApi.MessagePoll) {
            try {
                addPoll(message, message.chatId);
            } catch (ExecutionException | InterruptedException ex) {
                System.out.println("Exception while adding poll from new message " + ex.getMessage());
            }
            return;
        }
        if (!(message.content instanceof TdApi.MessageText content)) {
            System.out.println("Sorry! I don't know what it is.\n use /help to see supported commands.");
            return;
        }

        Scanner scanner = new Scanner(content.text.text);
        String command = scanner.next();

        switch (command) {
            case "/help" -> {
                bot.sendMessage(chatId, "/me - info about you!");
            }
            case "/me" -> {
                List<Answer> answers = dao.getUserAnswers(chatId, 3);
                if (!answers.isEmpty()) {
                    String request = generator.basicRequest(answers);
                    String result = provider.ask(request);
                    bot.sendMessage(chatId, Objects.requireNonNullElse(result, "Something went wrong..."));
                } else {
                    bot.sendMessage(chatId, "Sorry, I don't know anything about you, add me to some channels!");
                }
            }
//            case "/join" -> {
//                Pattern pattern = Pattern.compile("https://t.me/(.*?)$");
//                Matcher matcher = pattern.matcher(command);
//                if (matcher.matches()) {
//                    bot.sendMessage(chatId, "Joining " + matcher.group(1));
//                }
//            }
        }
    }
}
