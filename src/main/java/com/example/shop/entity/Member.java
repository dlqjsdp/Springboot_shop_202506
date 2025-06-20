package com.example.shop.entity;

import com.example.shop.constant.Role;
import com.example.shop.dto.MemberFormDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Getter
@Setter
@Table(name = "member")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String address;

    @Enumerated(EnumType.STRING)
    private Role role;

    // MemberFormDto -> member entity 변환 => 왜? DB에 저장하기 위해서
    public static Member createMember(MemberFormDto memberFormDto,
                                      PasswordEncoder passwordEncoder) {

        // 비밀번호 인코딩 때문에 DB에는 절대 원본 비밀번호가 저장되지 않고, $2a$10$... 형태의 BCrypt 해시값이 저장
//        String Password = passwordEncoder.encode(memberFormDto.getPassword());
        return Member.builder()
                .name(memberFormDto.getName())
                .email(memberFormDto.getEmail())
                .password(passwordEncoder.encode(memberFormDto.getPassword())) // 비밀번호 인코딩
                .address(memberFormDto.getAddress())
                .role(Role.ADMIN) // 기본 권한 설정
                .build();
    }
}
