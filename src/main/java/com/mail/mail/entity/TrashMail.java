package com.mail.mail.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class TrashMail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trashMailId;

    private Long mailId;

    private String senderName;

    private String recipientName;

    private String senderEmail;

    private String recipientEmail;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String body;

    private LocalDateTime deletedAt = LocalDateTime.now();

    private LocalDateTime sentAt;
}