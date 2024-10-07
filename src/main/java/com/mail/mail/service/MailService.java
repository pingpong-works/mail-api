package com.mail.mail.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
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
        props.put("mail.smtp.host", "pingpong-works.com"); // mail도메인
        props.put("mail.smtp.port", "25"); // smtp포트
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
            mailRepository.save(mail);
            Long i = 1L;
            if(!mail.getUploadFile()[0].isEmpty()) {
                for(MultipartFile multipartFile : mail.getUploadFile()) {
                    MailAttach mailAttach = new MailAttach();
                    mailAttach.setMailAttachId(i);
                    mailAttach.setMail(mail);
                    mailAttach.setMailAttachSaveName(uploadFile(multipartFile.getOriginalFilename(),multipartFile.getBytes(),uploadFolder));
                    mailAttach.setMailAttachSavePath(uploadFolder);
                    //mailRepository.saveAttach(mailAttach);
                    i++;
                }
            }
            result = 1;
        } catch (Exception e) {
            System.out.println("error"+e.getMessage());
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

        return result;
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
