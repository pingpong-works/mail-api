package com.mail.mail.service;

import com.mail.client.auth.AuthServiceClient;
import com.mail.client.auth.UserResponse;
import com.mail.mail.entity.Mail;
import com.mail.mail.entity.MailAttach;
import com.mail.mail.entity.ReceivedMail;
import com.mail.mail.repository.MailRepository;
import com.mail.mail.repository.ReceivedMailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MailService {

    private final MailRepository mailRepository;
    private final ReceivedMailRepository receivedMailRepository;
    private final AuthServiceClient authServiceClient;

    /**
     * 이메일 전송 메소드
     * @param mail 메일 정보가 담긴 객체
     * @return 메일 전송 결과 (1: 성공, 0: 실패)
     */
    public int sendEmail(Mail mail) {
        // 수신자의 정보를 auth-api로부터 가져오기
        try {
            if (mail.getRecipientId() != null) {
                UserResponse userResponse = authServiceClient.getEmployeeByIdForUser(mail.getRecipientId());
                if (userResponse != null && userResponse.getData() != null) {
                    mail.setRecipientName(userResponse.getData().getName());
                    mail.setRecipientEmail(userResponse.getData().getEmail());
                } else {
                    log.error("수신자 정보를 가져올 수 없습니다. ID: {}", mail.getRecipientId());
                    return 0; // 수신자 정보를 얻을 경우 메일 전송 중단
                }
            }

            mail.setSenderEmail("admin@pingpong-works.com");
            mail.setSenderName("관리자");

            // 메일 서버 설정
            Properties props = new Properties();
            props.put("mail.smtp.starttls.enable", "true");
            props.setProperty("mail.transport.protocol", "smtp");
            props.put("mail.debug", "false");
            props.put("mail.smtp.host", "mail.pingpong-works.com");
            props.put("mail.smtp.port", "25");
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.trust", "*");

            String subject = mail.getSubject();
            String body = mail.getBody();
            String recipientEmail = mail.getRecipientEmail();
            String recipientName = mail.getRecipientName();

            // 메일 세션 생성
            Session mail_session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mail.getSenderEmail(), "1234qwer");
                }
            });

            log.info("sendermail->" + mail.getSenderEmail());
            MimeMessage msg = new MimeMessage(mail_session);
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            Multipart multipart = new MimeMultipart();
            int result = 0;

            try {
                // 메일 내용 설정
                msg.setFrom(new InternetAddress(mail.getSenderEmail(), mail.getSenderName()));
                msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail, recipientName));
                msg.setSubject(MimeUtility.encodeText(subject, "UTF-8", "B"));
                mimeBodyPart.setContent(body, "text/html; charset=UTF-8");
                multipart.addBodyPart(mimeBodyPart);
                msg.setContent(multipart);
                Transport.send(msg);

                // 메일 전송 후 메일 정보 저장
                mail.setRead_chk(0L);
                mail.setDelete_chk(0L);
                mail.setSentAt(LocalDateTime.now());
                log.info("mailId before save: " + mail.getMailId());
                mailRepository.save(mail);
                log.info("mailId after save: " + mail.getMailId());

                // 첨부파일 처리
                if (mail.getUploadFile() != null && mail.getUploadFile().length > 0 && !mail.getUploadFile()[0].isEmpty()) {
                    Long i = 1L;
                    for (MultipartFile multipartFile : mail.getUploadFile()) {
                        MailAttach mailAttach = new MailAttach();
                        mailAttach.setMailAttachId(i);
                        mailAttach.setMail(mail);
                        mailAttach.setMailAttachSaveName(uploadFile(multipartFile.getOriginalFilename(), multipartFile.getBytes(), System.getProperty("user.dir") + "/upload/"));
                        mailAttach.setMailAttachSavePath(System.getProperty("user.dir") + "/upload/");
                        i++;
                    }
                }

                result = 1;
            } catch (Exception e) {
                log.error("메일 전송 중 에러 발생: ", e);
            }

            return result;
        } catch (Exception e) {
            log.error("sendEmail 메소드 중 에러 발생: ", e);
            return 0;
        }
    }

    /**
     * 이메일 수신 메소드 (POP3 프로토콜 사용)
     * @param username 사용자 이메일 계정 이름
     * @param password 사용자 이메일 계정 비밀번호
     * @throws MessagingException 메일 수신 관련 예외
     * @throws IOException 파일 처리 관련 예외
     */
    public void receiveEmails(String username, String password) throws MessagingException, IOException {
        // 메일 속성 설정
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "pop3");
        properties.put("mail.pop3.host", "pingpong-works.com");
        properties.put("mail.pop3.port", "110");
        properties.put("mail.pop3.starttls.enable", "true");
        properties.put("mail.pop3.ssl.trust", "*");

        // 메일 세션 생성
        Session emailSession = Session.getDefaultInstance(properties);

        // POP3 스토어 객체 생성 후 서버에 연결
        Store store = emailSession.getStore("pop3");
        store.connect("mail.pingpong-works.com", username, password);

        // 폴더 객체 생성 및 읽기 전용으로 열기
        Folder emailFolder = store.getFolder("INBOX");
        emailFolder.open(Folder.READ_ONLY);

        // 메시지 가져오기
        Message[] messages = emailFolder.getMessages();
        System.out.println("전체 메시지 수: " + messages.length);

        for (int i = 0; i < messages.length; i++) {
            Message message = messages[i];
            System.out.println("---------------------------------");
            System.out.println("이메일 번호 " + (i + 1));
            System.out.println("제목: " + message.getSubject());
            System.out.println("보낸 사람: " + message.getFrom()[0]);
            System.out.println("본문: " + getTextFromMessage(message));

            // 받은 이메일을 데이터베이스에 저장
            ReceivedMail receivedMail = new ReceivedMail();
            receivedMail.setSubject(message.getSubject());
            receivedMail.setSenderEmail(((InternetAddress) message.getFrom()[0]).getAddress());
            receivedMail.setSenderName(((InternetAddress) message.getFrom()[0]).getPersonal());
            receivedMail.setBody(getTextFromMessage(message));
            receivedMail.setReceivedAt(LocalDateTime.now());
            receivedMailRepository.save(receivedMail);
        }

        // 스토어와 폴더 객체 닫기
        emailFolder.close(false);
        store.close();
    }

    /**
     * 메시지 본문을 추출하는 메소드
     * @param message 메시지 객체
     * @return 메시지 본문 내용
     * @throws MessagingException 메시지 처리 중 예외
     * @throws IOException 파일 처리 중 예외
     */
    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    /**
     * 멀티파트 메시지 본문을 추출하는 메소드
     * @param mimeMultipart 멀티파트 객체
     * @return 멀티파트 본문 내용
     * @throws MessagingException 메시지 처리 중 예외
     * @throws IOException 파일 처리 중 예외
     */
    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    /**
     * 파일을 업로드하는 메소드
     * @param originalName 원본 파일 이름
     * @param fileData 파일 데이터 바이트 배열
     * @param uploadPath 업로드 경로
     * @return 저장된 파일 이름
     * @throws IOException 파일 처리 중 예외
     */
    private String uploadFile(String originalName, byte[] fileData, String uploadPath) throws IOException {
        UUID uid = UUID.randomUUID();
        File fileDirectory = new File(uploadPath);
        if (!fileDirectory.exists()) {
            fileDirectory.mkdirs();
        }

        String savedName = uid.toString() + "_" + originalName;
        File target = new File(uploadPath, savedName);
        FileCopyUtils.copy(fileData, target);
        log.info("saveSuccess");
        return savedName;
    }

    /**
     * 메일 삭제 메소드 (소프트 삭제)
     * @param mailId 삭제할 메일의 ID
     * @param isReceivedMail 받은 메일 여부 (true: 받은 메일, false: 보낸 메일)
     * @return 삭제 결과 (1: 성공, 0: 실패)
     */
    public int deleteMail(Long mailId, boolean isReceivedMail) {
        try {
            if (isReceivedMail) {
                // 받은 메일 삭제
                ReceivedMail receivedMail = receivedMailRepository.findById(mailId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 받은 메일 ID: " + mailId));
                receivedMail.setIsDeleted(true);
                receivedMailRepository.save(receivedMail);
            } else {
                // 보낸 메일 삭제
                Mail mail = mailRepository.findById(mailId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 보낸 메일 ID: " + mailId));
                mail.setIsDeleted(true);
                mailRepository.save(mail);
            }
            return 1;
        } catch (Exception e) {
            log.error("deleteMail 메소드 중 에러 발생: ", e);
            return 0;
        }
    }
}