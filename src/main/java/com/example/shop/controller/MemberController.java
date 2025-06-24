package com.example.shop.controller;

import com.example.shop.dto.MemberFormDto;
import com.example.shop.entity.Member;
import com.example.shop.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    // 1. 로그인 폼 제공
    @GetMapping(value = "/login")
    public String loginMember(){
        return "member/memberLoginForm"; // /members/login 요청 → memberLoginForm.html 반환
    }

    // 2. 로그인 실패 시 처리
    @GetMapping(value = "/login/error")
    public String loginError(Model model){
        model.addAttribute("loginErrorMsg","아이디 또는 비밀번호를 확인해주세요.");
        return "member/memberLoginForm"; // 로그인 실패 시 /members/login/error로 리다이렉트됨
    }

    // localhost:8080/members/new
    // 3. 회원가입 폼 제공
    @GetMapping(value ="/new") // /members/new 요청 시 회원가입 폼 보여줌
    public String MemberForm(Model model) {
        // th:object="${memberFormDto}" 사용을 위해 Dto를 model에 추가
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/memberForm";
    }

    // 4. 회원가입 처리
    @PostMapping(value = "/new")
    public String MemberForm(@Valid MemberFormDto memberFormDto, // @Valid로 DTO 유효성 검사
                             BindingResult bindingResult, Model model) { // BindingResult에 에러가 있으면 memberForm.html 다시 반환

        // @Valid : memberFormDto의 필드에 설정된 유효성 검사를 수행
        // BindingResult : 유효성 검사 후 오류 정보를 담는 객체
        // Model : 뷰에 데이터를 전달하기 위한 Spring의 모델 객체

        if(bindingResult.hasErrors()) {
            // 유효성 검사에서 오류가 발생한 경우 회원가입 폼으로 다시 이동
            return "member/memberForm";
        }

        try {
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            // ↑ 여기서 MemberFormDto를 받아 Member 엔티티 객체로 변환하면서,
            // 동시에 passwordEncoder를 사용해 비밀번호를 암호화하는 로직이 실행됩니다.
            memberService.save(member);
        }catch (IllegalArgumentException e) {
            // 회원 생성 중 IllegalArgumentException 발생 시 에러 메시지를 모델에 담아서 폼 페이지로 이동
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberForm";
        }

        // 회원가입 성공 시 홈 페이지로 리다이렉트
        return "redirect:/";
    }
}
