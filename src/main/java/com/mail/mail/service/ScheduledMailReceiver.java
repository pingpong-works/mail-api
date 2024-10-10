package com.mail.mail.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;

@Service
public class ScheduledMailReceiver {

    private final MailService mailService;

    public ScheduledMailReceiver(MailService mailService) {
        this.mailService = mailService;
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void checkEmails() throws MessagingException, IOException {
        mailService.receiveEmails("admin@pingpong-works.com", "1234qwer");
    }
}
