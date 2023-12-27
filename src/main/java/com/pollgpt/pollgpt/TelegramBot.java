package com.pollgpt.pollgpt;

import com.pollgpt.pollgpt.events.NewChat;
import com.pollgpt.pollgpt.events.NewMessage;
import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

@Component
public class TelegramBot {

    private final SimpleTelegramClient client;
    private final SimpleTelegramClientFactory clientFactory;
    private final BotConfig config;
    private final ApplicationEventPublisher publisher;
    @Getter
    private final long messageDivider = 1048576;

    @Autowired
    public TelegramBot(BotConfig config, SimpleTelegramClientFactory clientFactory, TDLibSettings settings, ApplicationEventPublisher publisher) {
        this.clientFactory = clientFactory;
        this.config = config;
        this.publisher = publisher;
        var clientBuilder = clientFactory.builder(settings);
        registerHandlers(clientBuilder);
        //this.client = clientBuilder.build(AuthenticationSupplier.bot(config.botToken));
        this.client = clientBuilder.build(AuthenticationSupplier.qrCode());
    }

    void registerHandlers(SimpleTelegramClientBuilder builder) {
        builder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
        builder.addUpdateHandler(TdApi.UpdateNewMessage.class, this::onUpdateNewMessage);
        builder.addUpdateHandler(TdApi.UpdateNewChat.class, this::onUpdateNewChat);
    }

    public void sendMessage(long chatId, String text) {
        var req = new TdApi.SendMessage();
        req.chatId = chatId;
        var txt = new TdApi.InputMessageText();
        txt.text = new TdApi.FormattedText(text, new TdApi.TextEntity[0]);
        req.inputMessageContent = txt;
        client.sendMessage(req, true)
                .whenCompleteAsync((res, error) -> {
                    if (error == null) {
                        System.out.println("Sent!");
                    } else {
                        System.out.println("Error while sending a message: " + error.getMessage());

                    }
                });
    }

    private void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
        TdApi.AuthorizationState authorizationState = update.authorizationState;
        if (authorizationState instanceof TdApi.AuthorizationStateReady) {
            System.out.println("Logged in");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosing) {
            System.out.println("Closing...");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
            System.out.println("Closed");
        } else if (authorizationState instanceof TdApi.AuthorizationStateLoggingOut) {
            System.out.println("Logging out...");
        }
    }

    public TdApi.Message[] getSomeMessages(long chatId, long fromMessage, int limit) throws InterruptedException, ExecutionException {
        var req = new TdApi.GetChatHistory(chatId, fromMessage, 0, Math.min(limit, 100), false);
        return client.send(req).get().messages;
    }

    public List<TdApi.Message> getMessages(long chatId, AtomicLong fromMessageId, int amount, Predicate<TdApi.Message> filter) {
        List<TdApi.Message> result = new ArrayList<>();
        while (amount > 0) {
            TdApi.Message[] messages;
            try {
                messages = getSomeMessages(chatId, fromMessageId.get(), amount);
            } catch (InterruptedException | ExecutionException ex) {
                System.out.println("Exception while retrieving messages: " + ex.getMessage());
                if (ex.getCause() instanceof TelegramError error && error.getErrorCode() == 400) {
                    fromMessageId.set(messageDivider);
                }
                break;
            }
            for (var msg : messages) {
                if (filter.test(msg)) {
                    result.add(msg);
                }
            }
            if (messages.length == 0) {
                break;
            }
            fromMessageId.set(messages[messages.length - 1].id);
            amount -= messages.length;
        }
        return result;
    }

    private List<Long> getSomePollVoters(long chatId, long messageId, int optionId, int offset, int limit) throws ExecutionException, InterruptedException {
        var req = new TdApi.GetPollVoters(chatId, messageId, optionId, offset, Math.min(limit, 50));
        return Arrays.stream(client.send(req).get().senders).map(x -> (x instanceof TdApi.MessageSenderUser ? ((TdApi.MessageSenderUser) x).userId
                : ((TdApi.MessageSenderChat) x).chatId)).toList();
    }

    public List<Long> getPollVoters(long chatId, long messageId, int optionId, int limit) throws ExecutionException, InterruptedException {
        List<Long> result = new ArrayList<>();
        int offset = 0;
        while (offset < limit) {
            var voters = getSomePollVoters(chatId, messageId, optionId, offset, limit);
            result.addAll(voters);
            offset += voters.size();
        }
        return result;
    }

    public void printChats(int limit) throws ExecutionException, InterruptedException {
        var req = new TdApi.GetChats();
        req.limit = limit;
        var chats = client.send(req).get().chatIds;
        for (long chatId : chats) {
            client.send(new TdApi.GetChat(chatId))
                    .whenCompleteAsync((chatIdResult, error) -> {
                        if (error != null) {
                            System.err.printf("Can't get chat title of chat %s%n", chatId);
                            error.printStackTrace(System.err);
                        } else {
                            String title = chatIdResult.title;
                            System.out.println(title + "   id: " + chatId);
                        }
                    });
        }
    }

    private void onUpdateNewMessage(TdApi.UpdateNewMessage update) {
        publisher.publishEvent(new NewMessage(this, update));
    }

    private void onUpdateNewChat(TdApi.UpdateNewChat update) {
        publisher.publishEvent(new NewChat(this, update.chat));
    }

    @PreDestroy
    public void destructor() throws Exception {
        client.close();
        clientFactory.close();
    }
}
