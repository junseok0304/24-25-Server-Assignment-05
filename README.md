# 24-25-Server-Assignment-05
5주차 과제 파일을 추가하였습니다.

## Q1. 사용자 관점에서의 로그인 과정 흐름

여기 페페(사용자)가 있습니다.
페페는 수강신청을 위해서 로그인을 하려고 하는군요…
사용자 로그인 페이지에 접속하여 ID와 PW를 입력합니다..

1. 로그인 버튼을 누르면 MemberController의 /api/members/login API가 호출됩니다.
2. `MemberService`에서 login메서드를 호출합니다. 
3. loginId가 비어있거나 null인 경우 `IllegalStateException`을 발생시키고 
`"로그인 ID가 필요합니다."` 를 반환합니다. 
이때 응답코드에 대한 예외처리는 안했으므로 사용자에게는 `400 Bad Request`를 반환함
4. loginId가 비어있지 않으면, `memberRepository.findByLoginId(loginReqDto.loginId())`를 통해 loginId에 해당하는 사용자가 데이터베이스에 존재하는지 조회합니다.
5. 데이터베이스에 해당 사용자가 존재하지 않으면 `"회원 정보를 찾을 수 없습니다."` 를 반환합니다.
이때 응답코드에 대한 예외처리는 안했으므로 사용자에게는 `500 Internal Server Error`를 반환함

### 비밀번호 검증에 대한 좀 더 자세한 로직 :

`LoginReqDto`객체에 담긴 사용자의 loginId, pwd가 전달되고, 
ID,PW검증 로직을 수행합니다. → `MemberService`의 login 메서드를 호출하고,  
loginId에 해당되는 사용자가 있는지 조회하고 → 비밀번호를 비교합니다. 

`if (!encoder.matches(rawPassword, encodedPassword)) {` 구문에서 
사용자가 입력한 평문의 암호는 `matches()` 에 전달되고, 
`matches()`는 암호화된 비밀번호와 “동일한 방식”으로 평문 암호를 인코딩한 뒤, 
데이터베이스에 저장된 암호화된 비밀번호와 비교하여 일치 여부를 반환합니다.

AppConfig에서 정의한 PasswordEncoder 메서드를 통해 
스프링 시큐리티에서 제공하는 `BCryptPasswordEncoder`으로 암호화되어 데이터베이스에 저장된 
비밀번호와 비교하는 과정을 거치는 단계임.
(**DB가 해킹되어도 원본 비밀번호는 유출되지 않음, DB에서는 원본 비밀번호를 저장하지 않음**)


## A1-1(로그인 성공) : 페페가 아이디와 비밀번호를 잘 입력했습니다.

(로그인 성공시에만 세션을 생성함.)

1. 사용자가 입력한 loginId가 존재하는 경우, `validationPassword` 메서드를 통한 
비밀번호 검증을 진행합니다. 
2. 비밀번호가 일치하면 `MemberLoginResDto`를 생성하고 사용자 정보를 반환합니다.
3. 로그인 성공 후 login메서드에서는 `HttpServletRequest`로 세션을 가져옵니다.
(세션이 없으면 생성함), 이를 통해 `SecurityContext` 에 인증 정보를 설정할 준비를 합니다.
4. `UsernamePasswordAuthenticationToken`을 생성하여 인증 정보를 설정하게 됩니다.
5. `FilterSecurityInterceptor`는 권한 부여를 위해서 접근 제어 결정을 하는 접근 결정 관리자 역할을 수행합니다. (이 부분은 SecurityConfig에 선언되지는 않았지만 스프링 시큐리티에서는 자동으로 이를 포함해 요청에 대한 접근 제어를 수행합니다.)
6. `authorizeHttpRequests` 설정을 통해 정의된 접근 제어 규칙을 기반으로 `FilterSecurityInterceptor`는 요청을 필터링하여 권한에 따라 접근을 허용하거나 거부하게 됩니다.
7. `UsernamePasswordAuthenticationToken`을 이용해 `Authentication`객체를 생성합니다.
8. `SecurityContextHolder.createEmptyContext()`를 이용해 비어있는 `SecurityContext`를 생성하고, 생성된 `SecurityContext`에 `Authentication`객체를 설정합니다.
이제 `SecurityContext`에는 사용자의 인증된 상태의 정보가 담깁니다.
9. 이후 세션에 SecurityContext를 
`SPRING_SECURITY_CONTEXT` 이름의 키로 저장합니다. 인증 정보를 세션에 저장해 활용하게 됩니다.
10. 페페가 로그인 후 다른 요청을 보낼 때마다 서버는 세션에 저장된 
`SPRING_SECURITY_CONTEXT` 키를 통해 페페의 인증 정보를 확인할 수 있습니다. 
이를 통해 페페는 별도의 인증 과정을 거치지 않아도 세션이 유지되는 동안 
인증된 상태로 리소스에 접근할 수 있습니다.


## A1-2(로그인 실패) : 이런, 페페가 아이디를 틀렸거나, 비밀번호를 틀렸거나, 회원가입을 안했네요…
(위에 작성한 3번 ~ 5번 까지의 예외가 로그인 실패에 대한 내용을 일부 포함함.)

1. 비밀번호가 일치하지 않는 경우
`encoder.matches(rawPassword, encodedPassword)` 메서드가 **비밀번호가 일치하지 않음**을 반환할 경우, `validationPassword` 메서드에서 `IllegalStateException` 예외가 발생합니다.
2. `"회원 정보를 찾을 수 없습니다."` 를 반환하는데, 이때 응답코드에 대한 예외처리는 안했으므로 
사용자(클라이언트)에게 `500 Internal Server Error`를 반환합니다.

## Q2.로그인 성공 후 사용자 경험 및 사용자 관점에서의 로그인 유지

A2-1. 로그인 성공 후 사용자는 인증이 필요한 다른 API들에 접근할 수 있습니다. 
사용자에게 접근 권한이 부여되었으므로 허용된 모든 리소스에는 접근할 수 있습니다.

A2-2. 로그인 유지는 사용자 단에서 인증이 지속적으로 유지되는 상태를 의미합니다. 
브라우저 세션이 유지되는 동안 세션 ID를 통한 인증으로 서버가 사용자를 식별할 수 있기 때문입니다.
사용자가 로그인한 상태로 페이지를 새로고침해도 동일한 세션 내에서는 로그인이 유지됩니다.

## 서버가 로그인한 사용자를 인식하는 방식
Q3.로그인한 사용자를 서버가 지속적으로 인식하기 위해 쿠키와 세션이 어떻게 사용되는지 설명하세요.  
(서버 딴에서 api 호출할 때마다 어떤 일이 일어나는지.)

A3. 로그인한 사용자를 서버가 지속적으로 인식하기 위해
쿠키와 세션을 이용하는데, 사용자가 로그인에 성공하면 `SecurityContext`에 인증정보를 저장하고, `HttpSession`을 생성해 세션 ID를 사용자 브라우저의 쿠키에 저장합니다.  `JSESSIONID`
(JSESSIONID는 코드에 명시되지는 않았지만 스프링 프레임워크와 서블릿 컨테이너에서 자동적으로 생성됨)
사용자가 API를 호출할 때마다 브라우저에서는 세션ID가 포함된 쿠키를 서버로 전송합니다. 서버에서는 이를 활용해 해당 세션을 조회하고 SecurityContext에서 사용자 정보를 확인해 요청을 처리합니다.

### Q4. 쿠키가 세션 기반 로그인에서 어떤 역할을 하는지 구체적으로 서술하고, 
특히 세션 ID가 쿠키에 저장되어 클라이언트와 서버 간에 전달되는 과정에 대해 설명하세요.

A4. 쿠키는 세션 기반 로그인에서 사용자와 서버 간 인증 정보를 전달하는 역할을 합니다. 
로그인 성공 후 서버에서는 세션 ID를 생성하고 사용자의 쿠키에 JSESSIONID로 저장합니다.
사용자는 이후 요청시마다 JSESSIONID 쿠키를 포함해 서버로 요청을 보내고, 
서버에서는 사용자의 세션을 식별하고 저장된 인증 정보를 대조해서 요청을 인증된 사용자로 처리합니다.

세션 생성 : `HttpServletRequest`의 `getSession(true)` 메서드를 호출하면 세션이 생성됩니다.
이 메서드가 호출되면 서버는 새로운 세션을 생성하거나 기존 세션을 반환합니다.
세션이 처음 생성될 때, 서버는 세션을 식별하기 위해 고유한 세션 ID를 생성합니다.

쿠키 설정 : 세션 ID는 `JSESSIONID`라는 이름의 쿠키에 저장됩니다.
`JSESSIONID` 쿠키는 자동으로 클라이언트에게 전송되며, 이후 요청 시 클라이언트는 이 쿠키를 통해 서버와의 세션을 유지합니다. `HttpSession`이 처음 생성되면 서블릿 컨테이너가 `JSESSIONID` 쿠키를 설정하여 클라이언트와 서버 간 세션을 관리하게 됩니다.

## Q5. 세션을 구현할 때 보안적으로 고려해야할 부분이 뭐가 있을까요? 
(5주차 코드에는 생략되어 있음) 찾아보시기 바랍니다.

A5.
- 세션 타임아웃 : 
세션 타임아웃 설정을 통해 일정 시간이 지나면 서버에서 세션이 만료되도록 설정하여 사용자가 
장시간 로그인 되어있는 경우 자동으로 로그아웃 되게 해서, 보안성을 향상시키는 것이 좋을 것 같습니다.
- https 사용 : 
https를 사용하여 사용자의 쿠키를 서버로 전송하는 과정에서의 탈취를 방지하는게 좋을 것 같습니다.
https는 데이터 전송과정을 암호화 하므로 중간자(MITM) 공격을 방지할 수 있습니다.
- CSRF 보호 활성화 : 
과제 예제 코드에서는 csrf가 비활성화 되어있는데, 이를 활성화 하여 
공격자가 사용자의 토큰을 악용하지 못하도록 방지하는것이 좋을 것 같습니다.
`.csrf(AbstractHttpConfigurer::disable)`







## Description
- 스프링 시큐리티 필터와 관련된 사진을 첨부했습니다.

**스프링 시큐리티 : 스프링 기반 애플리케이션의 보안을 담당하는 스프링 하위 프레임워크
보안 관련옵션을 제공하며 CSRF공격(사용자의 권한을 가지고 특정 동작을 수행하도록 유도),
세션 고정 공격(사용자의 인증 정보를 탈취하거나 변조하는 공격)을 방어해주고 
요청 헤더도 보안처리를 해주므로 개발자는 이를 활용하여 보안성을 향상시킬 수 있음.**

![image](https://github.com/user-attachments/assets/61edc1c0-fcd6-4cb4-9d76-f88b3b924ad4)

## Important content
- md파일 내에 틀린 내용이나 더 보충할 만한 내용이 있으면 알려주시면 감사하겠습니다!

## Reference

<!-- 참고한 레퍼런스가 있다면 공유해 주세요 -->
[SecurityContextHolder에 관한 블로그 글](https://swampwar.github.io/2020/07/06/SpringSecurity-%ED%95%84%ED%84%B0%EB%93%A42.html)
[스프링 시큐리티 주요 아키텍처 이해](https://catsbi.oopy.io/f9b0d83c-4775-47da-9c81-2261851fe0d0)
[중간자 공격에 대하여](https://blog.naver.com/ucert/221201640816)
