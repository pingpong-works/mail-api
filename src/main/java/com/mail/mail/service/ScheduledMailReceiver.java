package com.mail.mail.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.mail.client.auth.AuthServiceClient;
import com.mail.client.auth.UserResponse;

import javax.mail.MessagingException;
import java.io.IOException;

@Service
public class ScheduledMailReceiver {

    private final MailService mailService;
    private final AuthServiceClient authServiceClient;

    public ScheduledMailReceiver(MailService mailService, AuthServiceClient authServiceClient) {
        this.mailService = mailService;
        this.authServiceClient = authServiceClient;
    }

    // 1분마다 실행되며, employeeId를 기반으로 이메일을 받아오는 스케줄러
    @Scheduled(fixedRate = 60000)
    public void checkEmails() throws MessagingException, IOException {
        Long employeeId = 1L; // 실제 환경에서는 동적으로 가져와야 할 수 있습니다. 지금은 예시로 1L.

        try {
            // employeeId를 이용해 이메일 가져오기
            UserResponse userResponse = authServiceClient.getEmployeeByIdForUser(employeeId);

            if (userResponse != null && userResponse.getData() != null) {
                String email = userResponse.getData().getEmail();
                mailService.receiveEmails(employeeId); // 해당 employeeId로 메일 가져오기
            } else {
                System.out.println("사용자 정보를 가져올 수 없습니다. employeeId: " + employeeId);
            }
        } catch (Exception e) {
            System.err.println("이메일 수신 중 오류 발생: " + e.getMessage());
        }
    }
}
