package com.mail.mail.repository;

import com.mail.mail.entity.ReceivedMail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceivedMailRepository extends JpaRepository<ReceivedMail, Long> {
    // 필요시 추가적인 조회 메서드를 정의할 수 있습니다.
}
