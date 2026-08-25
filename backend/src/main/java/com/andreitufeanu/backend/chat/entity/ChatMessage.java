package com.andreitufeanu.backend.chat.entity;

import com.andreitufeanu.backend.chat.enums.ChatMessageRole;
import com.andreitufeanu.backend.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_messages")
public class ChatMessage {

    public ChatMessage(
            User user,
            ChatMessageRole role,
            String content,
            Instant createdAt
    ) {
        this.user = user;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ChatMessageRole role;

    @NotBlank(message = "You can not send an empty message")
    @Column(name = "content", nullable = false)
    private String content;

    @NotNull(message = "The created_at field is required")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}