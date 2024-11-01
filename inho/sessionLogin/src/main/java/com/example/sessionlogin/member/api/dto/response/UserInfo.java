package com.example.sessionlogin.member.api.dto.response;

public record UserInfo(
        String loginId,
        String pwd,
        String name
) {
}
