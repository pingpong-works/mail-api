package com.mail.mail.service;

import com.mail.client.auth.AuthServiceClient;
import com.mail.client.auth.UserResponse;
import com.mail.mail.entity.Mail;
import com.mail.mail.entity.MailAttach;
import com.mail.mail.entity.ReceivedMail;
import com.mail.mail.entity.TrashMail;
import com.mail.mail.repository.MailAttachRepository;
import com.mail.mail.repository.MailRepository;
import com.mail.mail.repository.ReceivedMailRepository;
import com.mail.mail.repository.TrashMailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MailService {

    private final MailRepository mailRepository;
    private final MailAttachRepository mailAttachRepository;
    private final ReceivedMailRepository receivedMailRepository;
    private final TrashMailRepository trashMailRepository;
    private final AuthServiceClient authServiceClient;

    /**
     * 이메일 전송 메소드
     * @param mail 메일 정보가 담긴 객체
     * @return 메일 전송 결과 (1: 성공, 0: 실패)
     */
    public int sendEmail(Mail mail, Long employeeId) {
        // 수신자의 정보를 auth-api로부터 가져오기
        try {
            // 로그인된 사용자의 정보를 auth-api를 통해 가져오기 (여기서 employeeId로 조회)
            UserResponse userResponse = authServiceClient.getEmployeeByIdForUser(employeeId);
            if (userResponse == null || userResponse.getData() == null) {
                log.error("발신자 정보를 가져올 수 없습니다. ID: {}", employeeId);
                return 0; // 발신자 정보를 얻지 못할 경우 메일 전송 중단
            }

            mail.setSenderEmail(userResponse.getData().getEmail());  // 로그인된 사용자의 이메일 설정
            mail.setSenderName(userResponse.getData().getName());    // 로그인된 사용자의 이름 설정

            // 메일 서버 설정
            Properties props = new Properties();
            props.put("mail.smtp.starttls.enable", "true");
            props.setProperty("mail.transport.protocol", "smtp");
            props.put("mail.debug", "false");
            props.put("mail.smtp.host", "mail2.pingpong-works.com");
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
            mimeBodyPart.setContent(mail.getBody(), "text/html; charset=UTF-8");
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

//                // 첨부파일 처리
//                if (mail.getUploadFile() != null && mail.getUploadFile().length > 0 && !mail.getUploadFile()[0].isEmpty()) {
//                    Long i = 1L;
//                    for (MultipartFile multipartFile : mail.getUploadFile()) {
//                        MailAttach mailAttach = new MailAttach();
//                        mailAttach.setMailAttachId(i);
//                        mailAttach.setMail(mail);
//                        mailAttach.setMailAttachSaveName(uploadFile(multipartFile.getOriginalFilename(), multipartFile.getBytes(), System.getProperty("user.dir") + "/upload/"));
//                        mailAttach.setMailAttachSavePath(System.getProperty("user.dir") + "/upload/");
//                        i++;
//                    }
//                }

                // 첨부파일 처리 개선
                if (mail.getUploadFile() != null && mail.getUploadFile().length > 0 && !mail.getUploadFile()[0].isEmpty()) {
                    Long i = 1L;
                    for (MultipartFile multipartFile : mail.getUploadFile()) {
                        try {
                            // 업로드 경로 생성 여부 확인 및 경로 생성
                            String uploadPath = System.getProperty("user.dir") + "/upload/";
                            File uploadDir = new File(uploadPath);
                            if (!uploadDir.exists()) {
                                uploadDir.mkdirs(); // 디렉토리 생성
                            }

                            // 파일 저장 처리
                            String savedFileName = uploadFile(multipartFile.getOriginalFilename(), multipartFile.getBytes(), uploadPath);

                            // 첨부파일 엔티티 생성 및 저장
                            MailAttach mailAttach = new MailAttach();
                            mailAttach.setMailAttachId(i);
                            mailAttach.setMail(mail);
                            mailAttach.setMailAttachSaveName(savedFileName);
                            mailAttach.setMailAttachSavePath(uploadPath);

                            // 첨부파일 정보를 DB에 저장
                            mailAttachRepository.save(mailAttach);

                            i++;
                        } catch (IOException e) {
                            log.error("첨부파일 저장 중 오류 발생: ", e);
                        }
                    }
                }

                result = 1;
            } catch (Exception e) {
                log.error("메일 전송 중 에러 발생: ", e);
                return -1;
            }

            return result;
        } catch (Exception e) {
            log.error("sendEmail 메소드 중 에러 발생: ", e);
            return 0;
        }
    }

    /**
     * 보낸 메일 전체 조회 메소드
     * @return 보낸 메일 목록
     */
    public Page<Mail> getSentMails(Pageable pageable) {
        return mailRepository.findAllByOrderBySentAtDesc(pageable);
    }

    /**
     * 보낸 메일 검색 메소드
     * @param search 검색어
     * @param pageable 페이징 설정 정보
     * @return 검색된 보낸 메일 목록
     */
    public Page<Mail> searchSentMails(String search, Pageable pageable) {
        return mailRepository.findBySubjectContainingOrRecipientNameContainingOrRecipientEmailContaining(
                search, search, search, pageable);
    }

    /**
     * 특정 메일 상세 조회 메소드
     * @param mailId 메일 ID
     * @return 메일 상세 정보
     */
    public Mail getSentMailById(Long mailId) {
        Mail mail = mailRepository.findById(mailId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메일을 찾을 수 없습니다. ID: " + mailId));

        if (!mail.getIsRead()) {
            // 메일 읽음 상태로 변경
            mail.setIsRead(true);
            mailRepository.save(mail); // 변경된 상태를 DB에 저장
        }

        return mail;
    }
    /**
     * 내게 쓴 메일 조회 메소드
     * @param employeeEmail 로그인된 사용자의 이메일
     * @param pageable 페이징 설정 정보
     * @return 페이징된 내게 쓴 메일 목록
     */
    public Page<Mail> getSelfMails(String employeeEmail, Pageable pageable) {
        return mailRepository.findBySenderEmailAndRecipientEmail(employeeEmail, employeeEmail, pageable);
    }

    /**
     * 휴지통 메일 전체 조회 메소드 (페이징네이션 적용)
     * @param pageable 페이징 설정 정보
     * @return 페이징된 휴지통 메일 목록
     */
    public Page<TrashMail> getTrashMails(Pageable pageable) {
        return trashMailRepository.findAllByOrderByDeletedAtDesc(pageable);
    }

    /**
     * 휴지통 메일 검색 메소드
     * @param search 검색어
     * @param pageable 페이징 설정 정보
     * @return 검색된 휴지통 메일 목록
     */
    public Page<TrashMail> searchTrashMails(String search, Pageable pageable) {
        return trashMailRepository.findBySubjectContainingOrRecipientNameContainingOrRecipientEmailContaining(
                search, search, search, pageable);
    }

    /**
     * 특정 휴지통 메일 상세 조회 메소드
     * @param trashMailId 휴지통 메일 ID
     * @return 휴지통 메일 상세 정보
     */
    public TrashMail getTrashMailById(Long trashMailId) {
        log.info("휴지통 메일 ID: {}", trashMailId);  // 추가된 로깅
        TrashMail trashMail = trashMailRepository.findById(trashMailId)
                .orElseThrow(() -> new IllegalArgumentException("해당 휴지통 메일을 찾을 수 없습니다. ID: " + trashMailId));

        if (!trashMail.getIsRead()) {
            // 메일 읽음 상태로 변경
            trashMail.setIsRead(true);
            trashMailRepository.save(trashMail); // 변경된 상태를 DB에 저장
        }

        return trashMail;
    }


    /**
     * 이메일 수신 메소드 (POP3 프로토콜 사용) - 로그인된 사용자의 이메일 정보 사용
     * @param employeeId 로그인된 사용자의 ID
     * @throws MessagingException 메일 수신 관련 예외
     * @throws IOException 파일 처리 관련 예외
     */
    public void receiveEmails(Long employeeId) throws MessagingException, IOException {
        // 로그인된 사용자의 이메일 정보를 auth-api로부터 가져오기
        UserResponse userResponse = authServiceClient.getEmployeeByIdForUser(employeeId);

        if (userResponse == null || userResponse.getData() == null) {
            throw new IllegalArgumentException("사용자 정보를 가져올 수 없습니다. ID: " + employeeId);
        }

        String username = userResponse.getData().getEmail(); // 사용자 이메일

        // 메일 속성 설정
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "pop3");
        properties.put("mail.pop3.host", "mail2.pingpong-works.com");
        properties.put("mail.pop3.port", "110");
        properties.put("mail.pop3.starttls.enable", "true");
        properties.put("mail.pop3.ssl.trust", "*");

        // 메일 세션 생성
        Session emailSession = Session.getDefaultInstance(properties);

        // POP3 스토어 객체 생성 후 서버에 연결
        Store store = emailSession.getStore("pop3");
        store.connect("mail2.pingpong-works.com", username, "1234qwer");

        // 폴더 객체 생성 및 읽기 전용으로 열기
        Folder emailFolder = store.getFolder("INBOX");
        emailFolder.open(Folder.READ_ONLY);

        // 메시지 가져오기
        Message[] messages = emailFolder.getMessages();
        System.out.println("전체 메시지 수: " + messages.length);

        for (int i = 0; i < messages.length; i++) {
            Message message = messages[i];

            // 메일러 데몬으로부터 온 반송 메일을 무시
            if (message.getFrom()[0].toString().contains("mailer-daemon")) {
                continue; // 반송 메일은 건너뜀
            }

            // 받은 이메일을 데이터베이스에 저장
            ReceivedMail receivedMail = new ReceivedMail();
            receivedMail.setSubject(message.getSubject());
            receivedMail.setSenderEmail(((InternetAddress) message.getFrom()[0]).getAddress());
            receivedMail.setSenderName(((InternetAddress) message.getFrom()[0]).getPersonal());
            receivedMail.setRecipientEmail(username); // 수신자 이메일 저장
            receivedMail.setBody(getTextFromMessage(message));
            receivedMail.setReceivedAt(LocalDateTime.now());
            receivedMailRepository.save(receivedMail);
        }

        // 스토어와 폴더 객체 닫기
        emailFolder.close(false);
        store.close();
    }

    /**
     * 받은 메일 전체 조회 메소드
     * @param pageable 페이징 설정 정보
     * @return 페이징된 받은 메일 목록
     */
    public Page<ReceivedMail> getReceivedMails(Pageable pageable) {
        return receivedMailRepository.findAllByOrderByReceivedAtDesc(pageable);
    }

    /**
     * 검색어를 기준으로 받은 메일을 조회하는 메소드
     * @param search 검색어
     * @param pageable 페이징 설정 정보
     * @return 검색된 메일 목록
     */
    public Page<ReceivedMail> searchReceivedMails(String search, Pageable pageable) {
        return receivedMailRepository.findBySubjectContainingOrSenderNameContainingOrSenderEmailContaining(
                search, search, search, pageable);
    }

    /**
     * 특정 수신 메일 상세 조회 메소드
     * @param receivedMailId 수신 메일 ID
     * @return 수신 메일 상세 정보
     */
    public ReceivedMail getReceivedMailById(Long receivedMailId) {
        ReceivedMail receivedMail = receivedMailRepository.findById(receivedMailId)
                .orElseThrow(() -> new IllegalArgumentException("해당 수신 메일을 찾을 수 없습니다. ID: " + receivedMailId));

        if (!receivedMail.getIsRead()) {
            // 받은 메일을 읽음 상태로 업데이트
            receivedMail.setIsRead(true);
            receivedMailRepository.save(receivedMail); // DB에 변경된 상태 저장
        }

        return receivedMail;
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
        } else if (message.isMimeType("text/html")) {
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
            } else if (bodyPart.isMimeType("text/html")) {
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
            fileDirectory.mkdirs(); // 디렉토리 없으면 생성
        }

        String savedName = uid.toString() + "_" + originalName; //파일 이름 UUID로 생성
        File target = new File(uploadPath, savedName); // 저장할 파일 경로 설정
        FileCopyUtils.copy(fileData, target); // 파일 데이터 저장
        log.info("saveSuccess: " + savedName); // 파일 저장 성공 로그
        return savedName; // 저장된 파일 이름 반환
    }

    /**
     * 메일 삭제 메소드 (휴지통으로 이동)
     * @param mailId 삭제할 메일의 ID
     * @param isReceivedMail 받은 메일 여부 (true: 받은 메일, false: 보낸 메일)
     * @return 삭제 결과 (1: 성공, 0: 실패)
     */
    public int deleteMail(Long mailId, boolean isReceivedMail) {
        try {
            if (isReceivedMail) {
                // 받은 메일 삭제 (휴지통으로 이동)
                ReceivedMail receivedMail = receivedMailRepository.findById(mailId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 받은 메일 ID: " + mailId));

                TrashMail trashMail = new TrashMail();
                trashMail.setMailId(receivedMail.getMailId());
                trashMail.setSenderName(receivedMail.getSenderName());
                trashMail.setSenderEmail(receivedMail.getSenderEmail());
                trashMail.setRecipientEmail(receivedMail.getRecipientEmail());
                trashMail.setSubject(receivedMail.getSubject());
                trashMail.setBody(receivedMail.getBody());
                trashMail.setSentAt(receivedMail.getReceivedAt());
                trashMailRepository.save(trashMail);

                receivedMailRepository.delete(receivedMail);
            } else {
                // 보낸 메일 삭제 (휴지통으로 이동)
                Mail mail = mailRepository.findById(mailId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 보낸 메일 ID: " + mailId));

                TrashMail trashMail = new TrashMail();
                trashMail.setMailId(mail.getMailId());
                trashMail.setSenderName(mail.getSenderName());
                trashMail.setRecipientName(mail.getRecipientName());
                trashMail.setSenderEmail(mail.getSenderEmail());
                trashMail.setRecipientEmail(mail.getRecipientEmail());
                trashMail.setSubject(mail.getSubject());
                trashMail.setBody(mail.getBody());
                trashMail.setSentAt(mail.getSentAt());
                trashMailRepository.save(trashMail);

                mailRepository.delete(mail);
            }
            return 1;
        } catch (Exception e) {
            log.error("deleteMail 메소드 중 에러 발생: ", e);
            return 0;
        }
    }

    /**
     * 휴지통에서 메일 복원 메소드
     * @param trashMailId 복원할 휴지통 메일의 ID
     * @return 복원 결과 (1: 성공, 0: 실패)
     */
    public int restoreMail(Long trashMailId) {
        try {
            TrashMail trashMail = trashMailRepository.findById(trashMailId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 휴지통 메일 ID: " + trashMailId));

            // 원본 메일 복원
            Mail restoredMail = new Mail();
            restoredMail.setSenderName(trashMail.getSenderName());
            restoredMail.setRecipientName(trashMail.getRecipientName());
            restoredMail.setSenderEmail(trashMail.getSenderEmail());
            restoredMail.setRecipientEmail(trashMail.getRecipientEmail());
            restoredMail.setSubject(trashMail.getSubject());
            restoredMail.setBody(trashMail.getBody());
            restoredMail.setSentAt(trashMail.getSentAt());
            restoredMail.setIsDeleted(false);
            mailRepository.save(restoredMail);

            // 휴지통에서 메일 삭제
            trashMailRepository.delete(trashMail);

            return 1;
        } catch (Exception e) {
            log.error("restoreMail 메소드 중 에러 발생: ", e);
            return 0;
        }
    }

    /**
     * 휴지통에서 메일 완전 삭제 메소드
     * @param trashMailId 완전 삭제할 휴지통 메일의 ID
     * @return 삭제 결과 (1: 성공, 0: 실패)
     */
    public int deletePermanently(Long trashMailId) {
        try {
            TrashMail trashMail = trashMailRepository.findById(trashMailId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 휴지통 메일 ID: " + trashMailId));

            // 휴지통에서 메일 완전 삭제
            trashMailRepository.delete(trashMail);
            return 1;
        } catch (Exception e) {
            log.error("deletePermanently 메소드 중 에러 발생: ", e);
            return 0;
        }
    }


}