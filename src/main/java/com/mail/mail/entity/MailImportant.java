package com.mail.mail.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@IdClass(MailImportantId.class)  // 복합키 사용 시 필요
public class MailImportant {

    @Id
    private Long employeeId;  // Employee ID

    @Id
    private Long mailId;  // Mail ID

    @Id
    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "mailId", insertable = false, updatable = false)
    private Mail mail;  // Mail 엔티티와의 관계

}
