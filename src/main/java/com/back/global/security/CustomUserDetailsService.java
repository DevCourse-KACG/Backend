// test에서 유저 데이터를 삽입하는 코드입니다.
// member 구현 후 수정 예정

//package com.back.global.security;
//
//import com.back.domain.member.member.entity.Member;
//import com.back.domain.member.member.service.MemberService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class CustomUserDetailsService implements UserDetailsService {
//    private final MemberService memberService;
//
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        Member member = memberService.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
//
//        return new SecurityUser(
//                member.getId(),
//                member.getEmail(),
//                member.getName(),
//                "N/A",
//                member.isAdmin(),
//                member.getAuthorities()
//        );
//    }
//}
