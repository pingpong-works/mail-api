package com.mail.mail.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class ReceivedMail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mailId;

    @Column(nullable = false)
    private String subject;

    @Lob
    private String body;

    @Column(nullable = false)
    private String senderEmail;

    private String senderName;

    @Column(nullable = false)
    private String recipientEmail;

    private LocalDateTime receivedAt;

    private Boolean isRead = false;

    private Boolean isDeleted = false;
}
