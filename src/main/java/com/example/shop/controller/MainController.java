package com.example.shop.controller;

import com.example.shop.dto.ItemSearchDto;
import com.example.shop.dto.MainItemDto;
import com.example.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    // ItemService 의존성 주입
    private final ItemService itemService;

    /*
        1️⃣ ItemSearchDto에 검색 폼 데이터 자동 바인딩
        2️⃣ Optional<Integer> page에 페이지 번호 받음
        3️⃣ Pageable을 생성해서 페이징 정보 설정
        4️⃣ itemService.getMainItemPage()로 데이터 조회
        5️⃣ 결과를 모델에 담아 뷰로 전달
        6️⃣ main.html 뷰 렌더링
     */

    // 루트 URL("/") 요청 처리
    @GetMapping(value = "/")
    public String main(ItemSearchDto itemSearchDto, // 검색 조건 DTO (Thymeleaf 폼 입력 값 바인딩)
                       Optional<Integer> page, // 페이지 번호 (없으면 Optional.empty)
                       Model model) { // 뷰에 데이터 전달용 Model

        // Pageable 생성
        // page 값이 있으면 그 값 사용, 없으면 0(첫 페이지)
        // 한 페이지에 6개씩 가져오기
        Pageable pageable = PageRequest.of(
                page.isPresent() ? page.get() : 0, 6 // page가 없으면 0, 페이지당 6개
        );

        // 현재 검색 조건과 페이징 정보 로그 출력
        log.info("MainController: itemSearchDto: {}", itemSearchDto);
        log.info("MainController: pageable: {}", pageable.getOffset());
        log.info("MainController: pageable: {}", pageable.getPageSize());

        // 서비스 호출: 검색 조건 + 페이징으로 상품 조회
        // 결과는 MainItemDto로 매핑되고 Page 객체로 반환
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        log.info("MainController : items number: {}", items.getNumber()); // 현재 페이지 번호
        log.info("MainController : items totalPaages: {}", items.getTotalPages()); // 전체 페이지 수

        // 조회 결과를 모델에 담기 (뷰에 전달)
        model.addAttribute("items", items); // 상품 목록 (Page)
        model.addAttribute("itemSearchDto", itemSearchDto); // 검색 조건 (검색어/필터 값 유지)
        model.addAttribute("maxPage", 5); // 페이지네이션 최대 표시 페이지 수

        // main.html 뷰 렌더링
        return "main";
    }
}
