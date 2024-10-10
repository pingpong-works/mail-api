package com.mail.client.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeData {
    private Long employeeId;
    private String name;
    private String departmentName;
    private String email;
}