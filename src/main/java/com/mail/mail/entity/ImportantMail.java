package com.mail.mail.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Setter
@Getter
@Entity
@IdClass(ImportantMailId.class)  // 복합키 사용 시 필요
public class ImportantMail {

    @Id
    private Long employeeId;  // Employee ID

    @Id
    private Long mailId;  // Mail ID

}
