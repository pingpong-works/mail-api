package com.mail.mail.service;

import com.mail.mail.entity.MailImportant;
import com.mail.mail.repository.ImportantMailRepository;
import com.mail.client.auth.AuthServiceClient;
import com.mail.client.auth.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImportantMailService {

    private final ImportantMailRepository importantMailRepository;
    private final AuthServiceClient authServiceClient;  // Auth 서비스와 통신할 Feign 클라이언트

    public MailImportant createImportantMail(Long employeeId, Long mailId) {
        // Auth 서비스에서 사용자 정보 조회
        UserResponse userResponse = authServiceClient.getEmployeeByIdForUser(employeeId);
        if (userResponse == null || userResponse.getData() == null) {
            throw new IllegalArgumentException("유효하지 않은 직원 ID입니다: " + employeeId);
        }

        // ImportantMail 객체 생성 및 저장
        MailImportant mailImportant = new MailImportant();
        mailImportant.setEmployeeId(employeeId);
        mailImportant.setMailId(mailId);

        return importantMailRepository.save(mailImportant);
    }
}