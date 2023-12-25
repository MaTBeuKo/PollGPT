package com.pollgpt.pollgpt.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;


@Entity
@Table(name = "chats")
@Data
@NamedQuery(name = "getUnreadChats",query = "SELECT c FROM Chat c WHERE c.fullyRead = false")
public class Chat {
    @Id
    private long chatId;
    private long firstMessage;
    private boolean fullyRead;
    public Chat() {

    }
}
