package com.mail.mail.controller;

import com.mail.dto.MultiResponseDto;
import com.mail.dto.SingleResponseDto;
import com.mail.mail.entity.Mail;
import com.mail.mail.entity.ReceivedMail;
import com.mail.mail.entity.TrashMail;
import com.mail.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173") // 실제 프론트엔드 도메인과 포트로 변경
@RestController
@RequestMapping("/mail")
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

    // 보낸 메일 전체 조회
    @GetMapping("/sent")
    public ResponseEntity<MultiResponseDto<Mail>> getSentMails(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size); // 페이지 번호는 0부터 시작하므로 page-1
        Page<Mail> mailPage = mailService.getSentMails(pageable);
        MultiResponseDto<Mail> response = new MultiResponseDto<>(mailPage.getContent(), mailPage);
        return ResponseEntity.ok(response);
    }

    // 보낸 메일 상세 조회 엔드포인트
    @GetMapping("/sent/{mailId}")
    public ResponseEntity<SingleResponseDto<Mail>> getSentMailById(@PathVariable Long mailId) {
        Mail mail = mailService.getSentMailById(mailId);
        return ResponseEntity.ok(new SingleResponseDto<>(mail));
    }

    // 휴지통 메일 전체 조회
    @GetMapping("/trash")
    public ResponseEntity<MultiResponseDto<TrashMail>> getTrashMails(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size); // 페이지 번호는 0부터 시작하므로 page-1
        Page<TrashMail> trashMailPage = mailService.getTrashMails(pageable);
        MultiResponseDto<TrashMail> response = new MultiResponseDto<>(trashMailPage.getContent(), trashMailPage);
        return ResponseEntity.ok(response);
    }

    // 휴지통 메일 상세 조회 엔드포인트
    @GetMapping("/trash/{trashMailId}")
    public ResponseEntity<SingleResponseDto<TrashMail>> getTrashMailById(@PathVariable Long trashMailId) {
        TrashMail trashMail = mailService.getTrashMailById(trashMailId);
        return ResponseEntity.ok(new SingleResponseDto<>(trashMail));
    }

    //pop3서버에서 메일을 가져오는 메서드
    //엔드포인트를 호출하여 수신 메일을 pop3서버에서 가져오고 이를 db에 저장
    @GetMapping("/receive")
    public ResponseEntity<String> receiveEmails() {
        try {
            mailService.receiveEmails("admin@pingpong-works.com", "1234qwer");
            return ResponseEntity.ok("이메일을 성공적으로 수신했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이메일 수신 중 오류 발생: " + e.getMessage());
        }
    }

    // 수신 메일 전체 조회
    @GetMapping("/received")
    public ResponseEntity<MultiResponseDto<ReceivedMail>> getReceivedMails(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size); // 페이지 번호는 0부터 시작하므로 page-1
        Page<ReceivedMail> receivedMailPage = mailService.getReceivedMails(pageable);
        MultiResponseDto<ReceivedMail> response = new MultiResponseDto<>(receivedMailPage.getContent(), receivedMailPage);
        return ResponseEntity.ok(response);
    }

    // 수신 메일 상세 조회 엔드포인트
    @GetMapping("/received/{receivedMailId}")
    public ResponseEntity<SingleResponseDto<ReceivedMail>> getReceivedMailById(@PathVariable Long receivedMailId) {
        ReceivedMail receivedMail = mailService.getReceivedMailById(receivedMailId);
        return ResponseEntity.ok(new SingleResponseDto<>(receivedMail));
    }


    /**
     * 메일 삭제 엔드포인트
     * @param mailId 삭제할 메일의 ID
     * @param isReceivedMail 받은 메일 여부 (true: 받은 메일, false: 보낸 메일)
     * @return 삭제 결과 메시지
     */
    @DeleteMapping("/{mailId}")
    public ResponseEntity<String> deleteMail(@PathVariable Long mailId, @RequestParam boolean isReceivedMail) {
        int result = mailService.deleteMail(mailId, isReceivedMail);
        if (result == 1) {
            return ResponseEntity.ok("메일이 성공적으로 삭제되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("메일 삭제 중 오류 발생");
        }
    }

    // 휴지통 메일 복원
    @PutMapping("/trash/restore/{trashMailId}")
    public ResponseEntity<String> restoreMail(@PathVariable Long trashMailId) {
        int result = mailService.restoreMail(trashMailId);
        if (result == 1) {
            return ResponseEntity.ok("메일이 복원되었습니다.");
        } else {
            return ResponseEntity.status(500).body("메일 복원 중 오류가 발생했습니다.");
        }
    }

    // 휴지통 메일 완전 삭제
    @DeleteMapping("/trash/{trashMailId}")
    public ResponseEntity<String> deletePermanently(@PathVariable Long trashMailId) {
        int result = mailService.deletePermanently(trashMailId);
        if (result == 1) {
            return ResponseEntity.ok("메일이 영구 삭제되었습니다.");
        } else {
            return ResponseEntity.status(500).body("메일 영구 삭제 중 오류가 발생했습니다.");
        }
    }
}
