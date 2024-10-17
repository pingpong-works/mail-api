package com.mail.mail.repository;

import com.mail.mail.entity.Mail;
import com.mail.mail.entity.TrashMail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrashMailRepository extends JpaRepository<TrashMail, Long> {
    Page<TrashMail> findAllByOrderByDeletedAtDesc(Pageable pageable);
    Page<TrashMail> findBySubjectContainingOrRecipientNameContainingOrRecipientEmailContaining(
            String subject, String senderName, String senderEmail, Pageable pageable);
}
