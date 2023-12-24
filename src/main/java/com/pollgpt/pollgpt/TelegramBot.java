package com.pollgpt.pollgpt;

import com.pollgpt.pollgpt.events.NewMessage;
import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

@Component
public class TelegramBot {

    private final SimpleTelegramClient client;
    private final SimpleTelegramClientFactory clientFactory;
    private final BotConfig config;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public TelegramBot(BotConfig config, SimpleTelegramClientFactory clientFactory, TDLibSettings settings, ApplicationEventPublisher publisher) {
        this.clientFactory = clientFactory;
        this.config = config;
        this.publisher = publisher;
        var clientBuilder = clientFactory.builder(settings);
        registerHandlers(clientBuilder);
        this.client = clientBuilder.build(AuthenticationSupplier.qrCode());
    }

    void registerHandlers(SimpleTelegramClientBuilder builder) {
        builder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
        builder.addCommandHandler("stop", this::onStopCommand);
        builder.addUpdateHandler(TdApi.UpdateNewMessage.class, this::onUpdateNewMessage);
        builder.addUpdateHandler(TdApi.UpdateChatAction.class, this::onUpdateChatAction);
    }

    public void close() throws Exception {
        client.close();
        clientFactory.close();
    }


    public SimpleTelegramClient getClient() {
        return client;
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

    int cnt = 0;

    public boolean loadMoreChats(){
        var req = new TdApi.LoadChats();
        req.limit = 1;
        req.chatList = new TdApi.ChatListArchive();
        //sendMessage(config.adminId, "loading chats");
        try {
            client.send(req).get();

        } catch (Exception ex) {
            System.out.println("While loading more chats:");
            System.out.println(ex.getMessage());
            return false;
        }
        return true;
    }

    private void onUpdateChatAction(TdApi.UpdateChatAction update) {
        System.out.println("Chat action" + cnt++);
        System.out.println(" ");
    }

    public TdApi.Message[] getSomeMessages(long chatId, long fromMessage, int limit) throws InterruptedException, ExecutionException {
        var req = new TdApi.GetChatHistory(chatId, fromMessage, 0, Math.min(limit, 100), false);
        return client.send(req).get().messages;
    }

    public List<TdApi.Message> getMessages(long chatId, Long fromMessageId, int amount, Predicate<TdApi.Message> filter, Boolean fullyRead) {
        List<TdApi.Message> result = new ArrayList<>();
        while (amount > 0) {
            TdApi.Message[] messages;
            try {
                messages = getSomeMessages(chatId, fromMessageId, amount);
            } catch (InterruptedException | ExecutionException ex) {
                System.out.println("Exception while retrieving messages: " + ex.getMessage());
                break;
            }
            for (var msg : messages) {
                if (filter.test(msg)) {
                    result.add(msg);
                }
            }
            if (messages.length == 0) {
                fullyRead = true;
                break;
            }
            fromMessageId = messages[messages.length - 1].id;
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

    private void onStopCommand(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
        if (isAdmin(commandSender)) {
            System.out.println("Received stop command. closing...");
            client.sendClose();
        }
    }

    public boolean isAdmin(TdApi.MessageSender sender) {
        if (sender instanceof TdApi.MessageSenderUser messageSenderUser) {
            return messageSenderUser.userId == config.adminId;
        } else {
            return false;
        }
    }

    @PreDestroy
    public void destructor() throws Exception {
        clientFactory.close();
        client.close();
    }
}
