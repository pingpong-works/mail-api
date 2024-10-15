package com.mail.mail.repository;

import com.mail.mail.entity.ReceivedMail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceivedMailRepository extends JpaRepository<ReceivedMail, Long> {
    Page<ReceivedMail> findAllByOrderByReceivedAtDesc(Pageable pageable);
    Optional<ReceivedMail> findTopByOrderByReceivedAtDesc();
}
