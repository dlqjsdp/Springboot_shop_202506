package com.example.shop.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        log.info("---------------Security Filter Chain---------------");

        http
                .authorizeHttpRequests(
                        config -> config
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // 정적 리소스는 누구나 접근 허용
                                .requestMatchers("/", "/members/**", "/item/**").permitAll() // 메인 페이지, 회원 관련 페이지, 상품 조회는 누구나 접근 허용
                                .requestMatchers("/admin/**").hasRole("ADMIN") // /admin으로 시작하는 모든 경로는 ROLE_ADMIN 권한이 있어야 접근 가능
                                .anyRequest().authenticated() // 나머지 모든 요청은 로그인한 사용자만 접근 가능
                );

        http
                .csrf(csrf -> csrf.disable())

                .formLogin(
                        form -> form.loginPage("/members/login")
                                .defaultSuccessUrl("/")

                                // login 화면에서 name=username이면 생략가능
                                // username -> email 사용하기 때문에 반드시 기입해야함.
                                .usernameParameter("email")
//                                .passwordParameter("password")
                                .failureUrl("/members/login/error")
                )
                .logout(logout-> logout
                        .logoutUrl("/members/logout") // 변경된 부분: logoutRequestMatcher 대신 logoutUrl 사용
                        .logoutSuccessUrl("/")

                        .invalidateHttpSession(true) // 세션 무효화 (선택 사항이지만 일반적으로 사용)
                        .deleteCookies("JSESSIONID") // 쿠키 삭제 (선택 사항이지만 일반적으로 사용)
                );



        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
