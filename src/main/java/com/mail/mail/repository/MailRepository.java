package com.mail.mail.repository;

import com.mail.mail.entity.Mail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MailRepository extends JpaRepository<Mail, Long>{
    Page<Mail> findAllByOrderBySentAtDesc(Pageable pageable);
}