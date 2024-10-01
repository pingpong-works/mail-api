package com.mail.mail.controller;

import com.mail.mail.dto.MailRequest;
import com.mail.mail.entity.Mail;
import com.mail.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.IOException;

@RestController
@RequestMapping("/api/mail")

@RequiredArgsConstructor
@Slf4j
public class MailController {

    private final MailService mailService;

    /**
     * 메일 전송 API
     * @param mailRequest 메일 요청 정보
     * @return 전송된 메일 정보 또는 에러 메시지
     */
    @PostMapping("/send")
    public ResponseEntity sendEmail(@RequestBody MailRequest mailRequest) {
        try {
            Mail sentMail = mailService.sendEmail(mailRequest);
            log.info("메일 전송 성공: {}", mailRequest.getRecipientEmail());
            return ResponseEntity.ok(sentMail);  // 성공적으로 전송된 메일 정보를 반환
        } catch (MessagingException | IOException e) {
            log.error("메일 전송 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("메일 전송 중 오류 발생: " + e.getMessage());
        }
    }
}
