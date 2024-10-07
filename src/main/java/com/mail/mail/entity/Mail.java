package com.mail.mail.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Mail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mailId;

    private String senderName;

    private String recipientName;

    private String senderEmail;

    private String recipientEmail;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String body;

    private Long read_chk;

    private Long delete_chk;

    @Transient
    private Long isImportant;
    @Transient
    private Long employeeId;
    //첨부파일
    @Transient
    private MultipartFile[] uploadFile;

    private Boolean isRead = false;

    private Boolean isDeleted = false;

    private LocalDateTime sentAt = LocalDateTime.now();

}

