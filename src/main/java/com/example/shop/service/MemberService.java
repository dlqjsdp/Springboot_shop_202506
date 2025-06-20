package com.example.shop.service;

import com.example.shop.entity.Member;
import com.example.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
//@Transactional
@Slf4j
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public Member save(Member member) {
        // email 중복체크
        validateDuplicateMember(member);
        return memberRepository.save(member);
    }

    private void validateDuplicateMember(Member member) {
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if (findMember != null) {
            throw new IllegalArgumentException("이미 가입된 회원 입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        log.info("--------------loadUserByUsername--------------");
        Member member = memberRepository.findByEmail(email); // DB 조회

        if (member == null) {
            throw new UsernameNotFoundException(email); // 없으면 예외
        }

        return User.builder() // Spring Security 내장 User 객체로 변환
                .username(member.getEmail()) // 로그인에 사용되는 아이디
                .password(member.getPassword()) // 인코딩된 비밀번호
                .roles(member.getRole().toString()) // ROLE_USER 등 역할 부여
                .build();
    }
}
