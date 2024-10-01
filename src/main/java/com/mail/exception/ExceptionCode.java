package com.mail.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ExceptionCode {

    //member 관련
    MEMBER_NOT_FOUND(404,"Member Not Found"),
    //토큰 인증 관련
    UNAUTHORIZED_MEMBER(401, "권한이 없는 멤버입니다."),
    TOKEN_INVALID(403, "토큰값이 유효하지 않습니다."),
    TOKEN_ISNULL(404,"토큰값을 전달받지 못했습니다.");

    @Getter
    private int statusCode;

    @Getter
    private String statusDescription;
}