package com.mail.mail.repository;

import com.mail.mail.entity.MailImportant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportantMailRepository extends JpaRepository<MailImportant, Long> {

}
