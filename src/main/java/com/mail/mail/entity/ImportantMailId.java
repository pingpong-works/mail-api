package com.mail.mail.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
public class ImportantMailId implements Serializable {
    private Long employeeId;
    private Long mailId;

    // 기본 생성자
    public ImportantMailId() {}

    // 생성자
    public ImportantMailId(Long employeeId, Long mailId) {
        this.employeeId = employeeId;
        this.mailId = mailId;
    }

    // equals()와 hashCode()는 복합 키 비교를 위해 구현
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportantMailId that = (ImportantMailId) o;
        return Objects.equals(employeeId, that.employeeId) && Objects.equals(mailId, that.mailId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, mailId);
    }
}


