package com.example.shop.repository;

import com.example.shop.constant.ItemSellStatus;
import com.example.shop.dto.ItemSearchDto;
import com.example.shop.dto.MainItemDto;
import com.example.shop.dto.QMainItemDto;
import com.example.shop.entity.Item;
import com.example.shop.entity.QItem;
import com.example.shop.entity.QItemImg;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    private JPAQueryFactory queryFactory;

    // EntityManager를 받아 Querydsl 쿼리 팩토리 초기화
    public ItemRepositoryCustomImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 판매 상태 조건 처리
     * - SELL 또는 SOLD_OUT일 경우 해당 조건을 반환
     * - null이면 조건 없음 (전체 조회)
     */
    private BooleanExpression searchSellStatus(ItemSellStatus itemSellStatus) {
//        private BooleanExpression itemSellStatus(ItemSellStatus itemSellStatus) {
        return itemSellStatus == null
                ? null
                : QItem.item.itemSellStatus.eq(itemSellStatus);
    }

    /**
     * 등록일 기준 조건 처리
     * - 오늘, 1주일, 1개월, 6개월 전 등의 조건을 계산하여 필터링
     * - "all" 또는 null이면 조건 없음
     */
    private BooleanExpression regDtsAfter(String searchDateType){
        LocalDateTime dateTime = LocalDateTime.now();

        if(StringUtils.equals("all", searchDateType) ||
                searchDateType == null) {
            return null;
        }else if (StringUtils.equals("1d", searchDateType)) {
            dateTime = dateTime.minusDays(1);
        }else if (StringUtils.equals("1w", searchDateType)) {
            dateTime = dateTime.minusWeeks(1);
        }else if (StringUtils.equals("1m", searchDateType)) {
            dateTime = dateTime.minusMonths(1);
        }else if (StringUtils.equals("6m", searchDateType)) {
            dateTime = dateTime.minusMonths(6);
        }
        // regTime이 dateTime 이후인 경우만 반환
        return QItem.item.regTime.after(dateTime);
    }

    /**
     * 검색 조건 처리 (상품명 또는 등록자 기준)
     * - searchBy에 따라 해당 컬럼을 LIKE 검색
     * - null 또는 빈 문자열이면 조건 없음
     */
    private BooleanExpression searchByLike(String searchBy, String searchQuery){
        if(StringUtils.equals("itemNm", searchBy)) {
            return QItem.item.itemNm.contains(searchQuery);
        }else if(StringUtils.equals("createdBy", searchBy)) {
            return QItem.item.createdBy.contains(searchQuery);
        }
        return null;
    }

    /**
     * 관리자 페이지 상품 목록 조회
     * - 동적 조건 필터 + 페이징 처리
     */
    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {

        // 검색 조건 로그 출력 (디버깅용)
        log.info("itemSearchDto.getSearchDateType() : {}", itemSearchDto.getSearchDateType());
        log.info("itemSearchDto.getItemSellStatus() : {}", itemSearchDto.getSearchSellStatus());
//        log.info("itemSearchDto.getItemSellStatus() : {}", itemSearchDto.getitemSellStatus());
        log.info("itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery() : {}",
                searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()));

        // Querydsl을 사용한 상품 목록 + 총 개수 조회 (페이징 처리)
        QueryResults<Item> results = queryFactory
                .selectFrom(QItem.item)
                .where(
                        regDtsAfter(itemSearchDto.getSearchDateType())
                        , searchSellStatus(itemSearchDto.getSearchSellStatus())
//                        , itemSellStatus(itemSearchDto.getItemSellStatus())
                        , searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery())
                )
                .orderBy(QItem.item.id.desc()) // 최신순 정렬
                .offset(pageable.getOffset())  // 페이지 시작 위치
                .limit(pageable.getPageSize()) // 페이지 크기
                .fetchResults(); // 목록 + 전체 개수 한번에 조회 (Querydsl 4.x 방식)

        // 조회 결과 추출
        List<Item> content = results.getResults(); // 현재 페이지의 데이터 목록
        long total = results.getTotal(); // 전체 개수

        // Page 객체로 반환 (Spring Data JPA 방식)
        return new PageImpl<>(content, pageable, total);
    }
    
    private BooleanExpression itemNmLike(String searchQuery){
        return StringUtils.isEmpty(searchQuery) ? 
                null : 
                QItem.item.itemNm.contains(searchQuery);
    }


    /*
        1️⃣ ItemImg에서 시작해 Item 조인
        2️⃣ 대표 이미지만 가져오기 (repimgYn = 'Y')
        3️⃣ 검색어 조건 (itemNm LIKE %검색어%)
        4️⃣ 페이징 적용 (offset, limit)
        5️⃣ MainItemDto에 매핑
        6️⃣ Page 객체 반환
     */
    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {

        // Querydsl Q타입 객체 생성
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        // Querydsl Q타입 객체 생성
        QueryResults<MainItemDto> results = queryFactory
                .select(
                        new QMainItemDto(
                                item.id, // 상품 ID
                                item.itemNm, // 상품명
                                item.itemDetail, // 상품 상세
                                itemImg.imgUrl, // 대표 이미지 URL
                                item.price // 가격
                        )
                )
                .from(itemImg) // ItemImg 테이블 기준으로 시작
                .join(itemImg.item, item) // Item 테이블과 조인
                .where(itemImg.repimgYn.eq("Y")) // 대표 이미지만
                .where(itemNmLike(itemSearchDto.getSearchQuery())) // 검색어 조건
                .orderBy(item.id.desc()) // 상품 ID 역순
                .offset(pageable.getOffset()) // 시작 위치
                .limit(pageable.getPageSize()) // 페이지 크기
                .fetchResults(); // 결과 + 카운트

        // 결과 리스트
        List<MainItemDto> content = results.getResults();
        // 전체 데이터 수
        long total = results.getTotal();

        // Page 객체 반환
        return new PageImpl<>(content, pageable, total);
    }
}
