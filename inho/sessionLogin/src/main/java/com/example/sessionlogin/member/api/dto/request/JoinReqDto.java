package com.example.sessionlogin.member.api.dto.request;

public record JoinReqDto(
        String loginId,
        String pwd,
        String name
) {
}
