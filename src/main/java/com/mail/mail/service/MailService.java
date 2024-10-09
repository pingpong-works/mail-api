package com.mail.mail.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import com.mail.mail.entity.Mail;
import com.mail.mail.entity.MailAttach;
import com.mail.mail.repository.MailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MailService { // 메일서버와 송수신할수있는 서비스

    private final MailRepository mailRepository;

    public int sendEmail(Mail mail) {
//        EmpForSearch emp = (EmpForSearch) session.getAttribute("empForSearch");
        mail.setSenderEmail("admin@pingpong-works.com");
        mail.setSenderName("관리자"); //누가 보냈는지
        System.out.println("보내는 메일 주소->" + mail.getSenderEmail());
        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true"); //메일을 보낼때 props를 보냄 props에 담궈줌
        props.setProperty("mail.transport.protocol", "smtp");
        props.put("mail.debug", "false");
        props.put("mail.smtp.host", "mail.pingpong-works.com"); // mail도메인
        props.put("mail.smtp.port", "25"); // smtp포트
//        props.put("mail.smtp.starttls.enable", "true"); // 587 포트 사용 시
        props.put("mail.smtp.connectiontimeout", "5000"); // timeout 시간 정해주는것
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.trust", "*"); // ssl 검증 비활성화
//        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        String subject = mail.getSubject();
        String body = mail.getBody();
        String recipientEmail = mail.getRecipientEmail();
        String recipientName = mail.getRecipientName();
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
        Transport t = null;
        try {
            msg.setFrom(new InternetAddress(mail.getSenderEmail(), mail.getSenderName()));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail, recipientName));
            msg.setSubject(MimeUtility.encodeText(subject, "UTF-8", "B"));
            //msg.setContent(mail_content, "text/html; charset=UTF-8");
            log.info("---------------------------------1");
            mimeBodyPart.setContent(body, "text/html; charset=UTF-8");
            multipart.addBodyPart(mimeBodyPart);
            log.info("---------------------------------2");
            //t = msgSession.getTransport("smtp");
            //log.info("Upload mail.getUploadFile().length : " + mail.getUploadFile().length);

//            if(!mail.getUploadFile()[0].isEmpty()) { // 첨부파일이 있다면
//                for(MultipartFile multipartFile : mail.getUploadFile()) {
//                    log.info("---------------------------------");
//                    log.info("Upload File Name : " + multipartFile.getOriginalFilename());
//                    log.info("Upload File Size : " + multipartFile.getSize());
//                    //File saveFile = new File(uploadFolder, multipartFile.getOriginalFilename());
//
//                    //multipartFile.transferTo(saveFile);
//                    MimeBodyPart mimeAttachPart = new MimeBodyPart();
//
//                    //DataSource source = new FileDataSource(uploadFolder + "\\" + multipartFile.getOriginalFilename());
//                    DataSource source = new ByteArrayDataSource(multipartFile.getBytes(), multipartFile.getContentType());
//
//                    mimeAttachPart.setDataHandler(new DataHandler(source));
//                    try {
//                        mimeAttachPart.setFileName(MimeUtility.encodeText(multipartFile.getOriginalFilename()));
//                    } catch (Exception e) {
//                        log.error(e.getMessage());
//                    }
//                    multipart.addBodyPart(mimeAttachPart);
//                }
//            }

            msg.setContent(multipart);
            log.info("senderMail->" + mail.getSenderEmail());
            Transport.send(msg);
//			t.connect();
//			t.sendMessage(msg, msg.getAllRecipients());
            String uploadFolder = System.getProperty("user.dir") + "/upload/";
            mail.setRead_chk(0L);
            mail.setDelete_chk(0L);
            mail.setSentAt(LocalDateTime.now());
            // mailRepository.save(mail)을 호출하기 전에 로그로 mailId를 확인
            log.info("mailId before save: " + mail.getMailId());
            mailRepository.save(mail);
            log.info("mailId after save: " + mail.getMailId());

            // 업로드 파일이 있는지 확인
            if (mail.getUploadFile() != null && mail.getUploadFile().length > 0 && !mail.getUploadFile()[0].isEmpty()) {
                Long i = 1L;
                for(MultipartFile multipartFile : mail.getUploadFile()) {
                    MailAttach mailAttach = new MailAttach();
                    mailAttach.setMailAttachId(i);
                    mailAttach.setMail(mail);
                    mailAttach.setMailAttachSaveName(uploadFile(multipartFile.getOriginalFilename(),multipartFile.getBytes(),uploadFolder));
                    mailAttach.setMailAttachSavePath(uploadFolder);
                    //mailRepository.saveAttach(mailAttach);
                    i++;
                }
                log.info("---------------------------------3");
            }

            result = 1;
        } catch (Exception e) {
            log.error("메일 전송 중 에러 발생: ", e);
        }finally {
//			if(t!=null) {
//				try {
//					t.close();
//				} catch (MessagingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
        }
        log.info("---------------------------------메일 송신");
        return result;
    }


    public void receiveEmails(String username, String password) throws MessagingException, IOException {
        // 메일 속성 설정
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "pop3");
        properties.put("mail.pop3.host", "pingpong-works.com"); // 메일 서버 주소
        properties.put("mail.pop3.port", "110"); // 보안 연결을 사용할 경우 995 포트를 사용
        properties.put("mail.pop3.starttls.enable", "true");
        properties.put("mail.pop3.ssl.trust", "*");

        // 메일 세션 생성
        Session emailSession = Session.getDefaultInstance(properties);

        // POP3 스토어 객체 생성 후 서버에 연결
        Store store = emailSession.getStore("pop3");
        store.connect("mail.pingpong-works.com", username, password); // 여기에 이메일과 비밀번호 입력

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
            System.out.println("본문: " + getTextFromMessage(message)); // 메시지에서 본문 추출
        }

        // 스토어와 폴더 객체 닫기
        emailFolder.close(false);
        store.close();
    }

    // 메시지 본문을 추출하는 헬퍼 메소드
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

    // 멀티파트 콘텐츠 처리용 헬퍼 메소드
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


    private String uploadFile(String originalName, byte[] fileData, String uploadPath) throws IOException {

        // universally unique identifier (UUID) 국제적으로 유일한 구별자
        UUID uid = UUID.randomUUID();
        // requestPath = requestPath + "/resources/image";
        System.out.println("uploadPath->" + uploadPath);
        //Directory 생성
        File fileDirectory = new File(uploadPath);
        if(!fileDirectory.exists()) {
            //신규 폴더(Directory) 생성
            fileDirectory.mkdirs();
            System.out.println("업로드용 폴더 생성 : " + uploadPath);
        }

        String savedName = uid.toString() + "_" + originalName;
        log.info("savedName : " + savedName);
        File target = new File(uploadPath, savedName);
        //File target = new File(requestPath, savedName);
        //File Upload ---> uploadPath / UUID+_+originalName
        FileCopyUtils.copy(fileData, target); // org.springframework.util.FileCopyUtils
        log.info("saveSuccess");
        return savedName;
    }


}
