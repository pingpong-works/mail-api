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

    @Lob
    private String body;

    private Long read_chk;

    private Long delete_chk;

    @Transient
    private Long isImportant;
    //첨부파일
    @Transient
    private MultipartFile[] uploadFile;

    private Boolean isRead = false;

    private Boolean isDeleted = false; // 삭제여부

    private LocalDateTime sentAt = LocalDateTime.now();

//    // 메일과 첨부파일의 일대다 관계 설정
//    @OneToMany(mappedBy = "mail", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<MailAttach> attachments = new ArrayList<>();  // 첨부파일 리스트
//
//    public void addAttachement(MailAttach mailAttach){
//        attachments.add(mailAttach);
//        if (mailAttach.getMail() != this){
//            mailAttach.setMail(this);
//        }
//    }

}

