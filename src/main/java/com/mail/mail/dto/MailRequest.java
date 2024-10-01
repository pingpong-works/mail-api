package com.mail.mail.dto;

import lombok.*;

@Getter
@Setter
@Data
public class MailRequest {

    private Long senderId;
    private Long recipientId;
    private String senderEmail;
    private String recipientEmail;
    private String subject;
    private String body;
    private String fileName;
    private String fileUrl;

}
