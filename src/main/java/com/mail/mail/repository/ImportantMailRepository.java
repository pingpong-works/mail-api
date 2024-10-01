package com.mail.mail.repository;

import com.mail.mail.entity.ImportantMail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportantMailRepository extends JpaRepository<ImportantMail, Long> {
}
