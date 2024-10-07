package com.mail.mail.repository;

import com.mail.mail.entity.Mail;
import com.mail.mail.entity.MailAttach;
import com.mail.mail.entity.MailImportant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface MailRepository extends JpaRepository<Mail, Long>{
}