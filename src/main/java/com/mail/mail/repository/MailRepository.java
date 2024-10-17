package com.mail.mail.repository;

import com.mail.mail.entity.Mail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MailRepository extends JpaRepository<Mail, Long>{
    Page<Mail> findAllByOrderBySentAtDesc(Pageable pageable);
    Page<Mail> findBySubjectContainingOrRecipientNameContainingOrRecipientEmailContaining(
            String subject, String senderName, String senderEmail, Pageable pageable);

    // 내게 쓴 메일 조회
    Page<Mail> findBySenderEmailAndRecipientEmail(String senderEmail, String recipientEmail, Pageable pageable);

}