package com.mail.mail.dto;

import lombok.*;

@Getter
@Setter
@Data
public class MailRequest {

    private String senderName;
    private String recipientName;
    private String senderEmail;
    private String recipientEmail;
    private String subject;
    private String body;

}
