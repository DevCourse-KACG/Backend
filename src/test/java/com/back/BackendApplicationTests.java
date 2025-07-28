package com.back;

import com.back.domain.auth.service.AuthService;
import com.back.domain.member.member.dto.MemberAuthResponse;
import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class BackendApplicationTests {
@Autowired
    AuthService authService;
@Autowired
    MemberService memberService;
    @Test
    void contextLoads() {
    }

    @Test
    void authServiceTest(){

         MemberAuthResponse resp = memberService.register(
                new MemberDto("test@test.com",
                        "dummypassword",
                        "test",
                        "bio"
                        )
        );
         String accessToken = resp.accessToken();
         Map<String,Object> payload =  memberService.payload(accessToken);
        System.out.println(payload);
    }

}
