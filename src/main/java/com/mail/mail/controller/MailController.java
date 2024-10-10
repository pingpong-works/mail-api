package com.mail.mail.controller;

import com.mail.mail.entity.Mail;
import com.mail.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
@Slf4j
public class MailController {

    private final MailService mailService;

    /**
     * 메일 전송 API
     * @param mail 메일 요청 정보
     * @return 전송된 메일 정보 또는 에러 메시지
     */
    @PostMapping("/send")
    public ResponseEntity sendEmail(@RequestBody Mail mail) {
        if (mail.getRecipientEmail() == null || mail.getRecipientEmail().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("수신자 이메일이 필요합니다.");
        }
        if (mail.getSubject() == null || mail.getSubject().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("메일 제목이 필요합니다.");
        }
        try {
            int sentResult = mailService.sendEmail(mail);
            log.info("메일 전송 성공: {}", mail.getRecipientEmail());
            return ResponseEntity.ok(sentResult);                             // 성공적으로 전송된 메일 정보를 반환
        } catch (Exception e) {
            log.error("메일 전송 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("메일 전송 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/receive")
    public ResponseEntity<String> receiveEmails() {
        try {
            mailService.receiveEmails("admin@pingpong-works.com", "1234qwer");
            return ResponseEntity.ok("이메일을 성공적으로 수신했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이메일 수신 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 메일 삭제 엔드포인트
     * @param mailId 삭제할 메일의 ID
     * @param isReceivedMail 받은 메일 여부 (true: 받은 메일, false: 보낸 메일)
     * @return 삭제 결과 메시지
     */
    @DeleteMapping("/delete/{mailId}")
    public ResponseEntity<String> deleteMail(@PathVariable Long mailId, @RequestParam boolean isReceivedMail) {
        int result = mailService.deleteMail(mailId, isReceivedMail);
        if (result == 1) {
            return ResponseEntity.ok("메일이 성공적으로 삭제되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("메일 삭제 중 오류 발생");
        }
    }
}
