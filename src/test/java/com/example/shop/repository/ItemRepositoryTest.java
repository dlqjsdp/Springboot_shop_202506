package com.example.shop.repository;

import com.example.shop.constant.ItemSellStatus;
import com.example.shop.dto.ItemSearchDto;
import com.example.shop.entity.Item;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat; // 따로 추가함

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("상품 저장 테스트")
    public void createItemTest(){
        Item item = new Item();

        item.setItemNm("테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("테스트 상품 상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        item.setRegTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());

        Item savedItem = itemRepository.save(item);

        log.info("savedItem : {} ", savedItem.toString());
    }


    public void createItemList(){

        for(int i=1; i<=10; i++){

            Item item = new Item();

            item.setItemNm("테스트 상품" + i);
            item.setPrice(10000 + i);
            item.setItemDetail("테스트 상품 상세 설명" + i);
            item.setItemSellStatus(ItemSellStatus.SELL);
            item.setStockNumber(100);
            item.setRegTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());

            Item savedItem = itemRepository.save(item);
        }
    }

    @Test
    @DisplayName("상품명 조회 테스트")
    public void findByItemNmTest(){
        this.createItemList();

        List<Item> itemList = itemRepository.findByItemNm("테스트 상품1");
        itemList.forEach(item -> log.info("item : {} ", item.toString()));
    }

    @Test
    @DisplayName("상품명 Like 조회 테스트")
    public void findByItemNmLikeTest(){
        this.createItemList();

        List<Item> itemList = itemRepository.findByItemNmLike("%테스트 상품1%");
        itemList.forEach(item -> log.info("item : {} ", item.toString()));
    }

    @Test
    @DisplayName("가격 조회 테스트")
    public void findByPriceLessThanTest(){
        this.createItemList();

        List<Item> itemList = itemRepository.findByPriceLessThan(10005);
        itemList.forEach(item -> log.info("item : {} ", item.toString()));
    }

    @Test
    @DisplayName("@Query를 이용한 상품 조회 테스트")
    public void findByItemDetailLike(){
        this.createItemList();

        List<Item> itemList = itemRepository.findByItemDetailLike("설명1");
        itemList.forEach(item -> log.info("item : {} ", item.toString()));
    }

    @Test
    @DisplayName("@Query Native를 이용한 상품 조회 테스트")
    public void findByItemDetailByNative(){
        this.createItemList();

        List<Item> itemList = itemRepository.findByItemDetailByNative("설명1");
        itemList.forEach(item -> log.info("item : {} ", item.toString()));
    }

    @Autowired
    private EntityManager em;

    @Test
    public void getGetAdminItemPage(){

        // Given: 테스트에 필요한 검색 조건 설정
        ItemSearchDto searchDto = new ItemSearchDto();
        searchDto.setSearchDateType("all"); // 등록일 조건 없음 (전체 검색)
        searchDto.setSearchSellStatus(ItemSellStatus.SOLD_OUT); // 품절 상품만 조회
        searchDto.setSearchBy("itemNm"); // 검색 기준: 상품명
        searchDto.setSearchQuery("자바"); // 검색어: "자바" 포함

        PageRequest pageRequest = PageRequest.of(0, 5); // 페이지 번호 0, 한 페이지 5개

        // When: 실제 메서드 호출
        Page<Item> result = itemRepository.getAdminItemPage(searchDto, pageRequest);

        // Then: 결과 검증 (단언문)
        assertThat(result.getTotalElements()).isEqualTo(3); // 전체 품절 "자바" 상품 수는 3개 (총 3개가 있어야 함)
        assertThat(result.getContent().size()).isEqualTo(3); // 현재 페이지의 실제 항목 수도 3개 (현재 페이지 결과도 3개)
        assertThat(result.getContent().get(0).getItemNm()).contains("자바"); // 첫 번째 결과의 이름에 "자바" 포함

        result.getContent().forEach(item -> log.info("item : {} ", item.toString()));
    }

    @Test
    public void getGetAdminItemPage2(){

        // Given: 테스트에 필요한 검색 조건 설정
        ItemSearchDto searchDto = new ItemSearchDto();
        searchDto.setSearchDateType("all"); // 등록일 조건 없음 (전체 검색)
        searchDto.setSearchSellStatus(ItemSellStatus.SELL); // 판매중 상품만 조회
        // searchBy, searchQuery 없음 (전체 SELL)

        PageRequest pageRequest = PageRequest.of(0, 5); // 페이지 번호 0, 한 페이지 5개
//        PageRequest pageRequest = PageRequest.of(1, 5); // 페이지 번호 1(두 번째 페이지 (0부터 시작하니까)), 한 페이지 5개

        // When: 실제 메서드 호출
        Page<Item> result = itemRepository.getAdminItemPage(searchDto, pageRequest);

        // Then: 결과 검증 (단언문)
        assertThat(result.getTotalElements()).isEqualTo(8); // 조건에 맞는 전체 데이터 개수 (페이징과 상관없음) - 전체 SELL 상품이 8개면 8
        assertThat(result.getContent().size()).isEqualTo(5); // 현재 페이지에 가져온 데이터 개수 (페이징 영향 받음) - 한 페이지에 5개씩인데 첫 페이지면 5, 두 번째 페이지면 남은 3

        // 조회된 데이터 출력
        result.getContent().forEach(item -> log.info("item : {} ", item.toString()));
    }

    @Test
    public void getGetAdminItemPage3(){

        // Given: 테스트에 필요한 검색 조건 설정
        ItemSearchDto searchDto = new ItemSearchDto();
        searchDto.setSearchDateType("1w"); // 등록일: 최근 일주일 이내
        searchDto.setSearchSellStatus(ItemSellStatus.SELL); // 판매중 상품만 조회
        // searchBy, searchQuery 없음 (전체 SELL)

        PageRequest pageRequest = PageRequest.of(0, 5); // 페이지 번호 0, 한 페이지 5개
//        PageRequest pageRequest = PageRequest.of(1, 5); // 페이지 번호 1, 한 페이지 5개

        // When: 실제 메서드 호출
        Page<Item> result = itemRepository.getAdminItemPage(searchDto, pageRequest);

        // Then: 결과 검증 (단언문)
        assertThat(result.getTotalElements()).isEqualTo(8); // 실제 조건에 맞는 총 데이터 수
        assertThat(result.getContent().size()).isEqualTo(5); // 첫 페이지에 담긴 데이터 수

        // 조회된 데이터 출력
        result.getContent().forEach(item -> log.info("item : {} ", item.toString()));
    }
}