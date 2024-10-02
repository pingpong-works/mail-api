package com.mail.mail.service;

import com.mail.mail.dto.MailRequest;
import com.mail.mail.entity.Mail;
import com.mail.mail.repository.MailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final MailRepository mailRepository;
    private final JavaMailSender mailSender;

    /**
     * 메일 발송 및 DB 저장을 담당하는 메서드
     * @param mailRequest 메일 요청 정보
     * @return 전송된 메일 정보
     * @throws MessagingException 메일 발송 실패 시 예외
     */
    @Async  // 비동기 처리
    public Mail sendEmail(MailRequest mailRequest) throws MessagingException, IOException {
        // 메일 객체 생성 및 설정
        Mail mail = createMailFromRequest(mailRequest);

        // JavaMailSender를 사용해 메일 발송
        boolean isMailSent = sendMimeEmail(mailRequest);

        // 메일 DB에 저장 (발송 성공 여부에 따라 다르게 처리 가능)
        mail.setSentAt(LocalDateTime.now());
        mail.setIsRead(false);
        mail.setIsDeleted(false);

        if (isMailSent) {
            log.info("메일 발송 성공: {}", mail.getRecipientEmail());
            return mailRepository.save(mail);
        } else {
            log.error("메일 발송 실패: {}", mail.getRecipientEmail());
            throw new MessagingException("메일 발송 실패");
        }
    }

    /**
     * 메일 요청 정보를 기반으로 Mail 엔티티를 생성하는 메서드
     * @param mailRequest 메일 요청 정보
     * @return 생성된 Mail 엔티티
     */
    private Mail createMailFromRequest(MailRequest mailRequest) {
        Mail mail = new Mail();
        // senderId가 없는 경우도 지원
        if (mailRequest.getSenderId() != null) {
            mail.setSenderId(mailRequest.getSenderId());
        }
        mail.setRecipientId(mailRequest.getRecipientId());
        mail.setSenderEmail(mailRequest.getSenderEmail());
        mail.setRecipientEmail(mailRequest.getRecipientEmail());
        mail.setSubject(mailRequest.getSubject());
        mail.setBody(mailRequest.getBody());
        mail.setFileName(mailRequest.getFileName());
        mail.setFileUrl(mailRequest.getFileUrl());
        return mail;
    }

    /**
     * 실제 메일을 전송하는 로직
     * @param mailRequest 메일 요청 정보
     * @return 메일 전송 성공 여부
     */
    private boolean sendMimeEmail(MailRequest mailRequest) throws MessagingException, IOException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(mailRequest.getSenderEmail());
            helper.setTo(mailRequest.getRecipientEmail());
            helper.setSubject(mailRequest.getSubject());
            helper.setText(mailRequest.getBody(), true);  // HTML 지원

            // 첨부 파일이 있는 경우 처리
            if (mailRequest.getFileUrl() != null) {
                File file = new File(mailRequest.getFileUrl());
                if (file.exists()) {
                    helper.addAttachment(mailRequest.getFileName(), file);
                } else {
                    log.warn("첨부 파일을 찾을 수 없습니다: {}", mailRequest.getFileUrl());
                }
            }

            // 메일 전송
            mailSender.send(message);
            return true;

        } catch (MessagingException e) {
            log.error("메일 전송 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 받은 메일 단일 조회
     * @param mailId 조회할 메일의 ID
     * @return 조회된 메일 정보
     */
    public Mail getMailById(Long mailId) {
        // 메일을 조회하고 존재하지 않으면 예외 처리
        return mailRepository.findById(mailId)
                .orElseThrow(() -> new RuntimeException("메일을 찾을 수 없습니다: " + mailId));
    }

    // 답장 메일 발송 기능을 추가
    public Mail replyToMail(Long originalMailId, MailRequest mailRequest) throws MessagingException, IOException {
        Mail originalMail = getMailById(originalMailId);

        // 새로운 메일 요청 객체 생성
        MailRequest replyMailRequest = new MailRequest();
        replyMailRequest.setSenderEmail(mailRequest.getSenderEmail());
        replyMailRequest.setRecipientEmail(mailRequest.getRecipientEmail());
        replyMailRequest.setSubject("Re: " + originalMail.getSubject());  // 답장 메일 제목 설정
        replyMailRequest.setBody("원본 메일:\n" + originalMail.getBody());

        return sendEmail(replyMailRequest);
    }
//    /**
//     * 파일을 업로드하고 경로를 반환하는 메서드
//     * @param multipartFile 업로드할 파일
//     * @return 파일이 저장된 경로
//     * @throws IOException 파일 저장 실패 시 예외
//     */
//    private String uploadFile(MultipartFile multipartFile) throws IOException {
//        String uploadFolder = "your-upload-path";  // 실제 파일이 저장되는 경로 설정
//        String fileName = multipartFile.getOriginalFilename();
//
//        // 디렉터리 생성 확인
//        File directory = new File(uploadFolder);
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//
//        File saveFile = new File(uploadFolder, fileName);
//        multipartFile.transferTo(saveFile);
//        return saveFile.getAbsolutePath();
//    }
}
