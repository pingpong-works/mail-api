package com.mail.mail.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Mail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mailId;

    private Long senderId;

    private Long recipientId;

    @Column(nullable = false)
    private String senderEmail;

    private String recipientEmail;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String body;

    private String fileName;

    private String fileUrl;

    private Boolean isRead = false;

    private Boolean isDeleted = false;

    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

}

