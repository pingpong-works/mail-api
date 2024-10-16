package com.mail.mail.entity;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "MailAttach")
public class MailAttach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mailAttachId;  // 첨부파일의 고유 ID

    private String mailAttachSavePath;
    private String mailAttachSaveName;

    // 메일과의 다대일 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mailId")
    private Mail mail;

//    public void setMail(Mail mail) {
//        this.mail = mail;
//        if (!mail.getAttachments().contains(this)) {
//            mail.getAttachments().add(this);
//        }
//    }
}





