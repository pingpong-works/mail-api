package com.mail.mail.repository;

import com.mail.mail.entity.ReceivedMail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceivedMailRepository extends JpaRepository<ReceivedMail, Long> {
}
