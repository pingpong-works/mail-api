package com.mail.mail.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "received_mail")
public class ReceivedMail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderEmail;

    private String senderName;

    private String recipientEmail;

    private String subject;

    @Column(length = 5000) // 이메일 본문은 길어질 수 있으므로 길이 설정
    private String body;

    private LocalDateTime receivedAt;

    private Boolean isRead = false;

    // 첨부파일이 있다면 여기에 추가할 수 있음
    // private List<String> attachments;
}
