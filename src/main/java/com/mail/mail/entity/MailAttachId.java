package com.mail.mail.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Embeddable
public class MailAttachId implements Serializable {
    private Long mail;
    private Long mailAttachId;
}
