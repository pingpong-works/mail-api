package com.mail.mail.repository;

import com.mail.mail.entity.Mail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MailRepository extends JpaRepository<Mail, Long>{
    Page<Mail> findAllByOrderBySentAtDesc(Pageable pageable);
    Page<Mail> findBySubjectContainingOrRecipientNameContainingOrRecipientEmailContaining(
            String subject, String senderName, String senderEmail, Pageable pageable);
}