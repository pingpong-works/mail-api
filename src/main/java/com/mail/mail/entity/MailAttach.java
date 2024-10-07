package com.mail.mail.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@IdClass(MailAttachId.class)
@Entity
@NoArgsConstructor
@Table(name = "MailAttach")
public class MailAttach {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonManagedReference
    @JoinColumn(name = "mailId")
    private Mail mail;

    @Id
    private Long mailAttachId;

    private String mailAttachSavePath;
    private String mailAttachSaveName;


}





