package com.example.sessionlogin.member.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    //  세션으로 접근 잘 되는지 테스트용 api
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("test good", HttpStatus.OK);
    }
}
