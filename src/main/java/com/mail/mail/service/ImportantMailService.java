package com.mail.mail.service;

import com.mail.mail.dto.EmployeeDTO;
import com.mail.mail.entity.MailImportant;
import com.mail.mail.repository.ImportantMailRepository;
import com.mail.mail.service.feign.EmployeeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImportantMailService {

    private final ImportantMailRepository importantMailRepository;
    private final EmployeeClient employeeClient;  // Employee 서비스와 통신할 Feign 클라이언트

    public MailImportant createImportantMail(Long employeeId, Long mailId) {
        // Employee 서비스에서 사용자 정보 조회
        EmployeeDTO employee = employeeClient.getEmployeeById(employeeId);

        // ImportantMail 객체 생성 및 저장
        MailImportant mailImportant = new MailImportant();
        mailImportant.setEmployeeId(employeeId);
        mailImportant.setMailId(mailId);

        return importantMailRepository.save(mailImportant);
    }
}

