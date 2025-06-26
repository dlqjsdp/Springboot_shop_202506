package com.example.shop.entity;

import com.example.shop.constant.ItemSellStatus;
import com.example.shop.repository.ItemRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class ItemTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    void setUp() {
        queryFactory = new JPAQueryFactory(em);
    }

    @Test
    public void testFindByItemNm() {
        List<Item> items = itemRepository.findByItemNm("떡볶이");
        items.forEach(item->log.info(item.toString()));
//        items.forEach(System.out::println);
        log.info("--------------QueryDSL---------------");

        QItem qItem = QItem.item;

        List<Item> item2 = queryFactory
                .select(qItem)
                .from(qItem)
                .where(qItem.itemNm.eq("떡볶이"))
                .fetch();
        item2.forEach(item->log.info(item.toString()));

    }

    // And
    @Test
    public void testFindByItemNmAndPrice() {
        QItem qItem = QItem.item;

        List<Item> item = queryFactory
                .selectFrom(qItem)
                .where(
                        qItem.itemNm.eq("떡볶이"),
                        qItem.price.gt(10000)
                )
                .fetch();

        log.info(item.toString());
    }

    // OR 조건 검색
    @Test
    public void testFindByitemNmOrItemDetail(){
        QItem qItem = QItem.item;

        List<Item> items = queryFactory
                .select(qItem)
                .from(qItem)
                .where(
                        qItem.itemNm.contains("부트")
                                .or(qItem.itemDetail.contains("자바"))
                )
                .fetch();
        items.forEach(item->log.info(item.toString()));
    }

    // Enum 조건 검색
    @Test
    public void testFindBySellStatus(){
        QItem qItem = QItem.item;

        List<Item> items = queryFactory
                .selectFrom(qItem)
                .where(qItem.itemSellStatus.eq(ItemSellStatus.SOLD_OUT))
                .fetch();

        items.forEach(item->log.info(item.toString()));
    }

    // 동적 조건 검색(BooleanBuilder 사용)
    @Test
    public void testDynamicSearch(){
        QItem qItem = QItem.item;
        BooleanBuilder builder = new BooleanBuilder();

        String searchNm = "라면";
        Integer minPrice = 1000;

        if(searchNm != null){
            builder.and(qItem.itemNm.contains(searchNm));
        }

        if(minPrice != null){
            builder.and(qItem.price.gt(minPrice));
        }

        List<Item> items = queryFactory
                .selectFrom(qItem)
                .where(builder)
                .fetch();
        items.forEach(item->log.info(item.toString()));
    }

    // 정렬
    @Test
    public void testPaging(){
        QItem qItem = QItem.item;

        List<Item> items = queryFactory
                .selectFrom(qItem)
                .where(qItem.price.gt(100))
                .orderBy(qItem.price.asc())
                .fetch();

        log.info(items.toString());
    }

    // 정렬 + 페이징 처리
    @Test
    public void testPagingAndSort(){
        QItem qItem = QItem.item;

        List<Item> items = queryFactory
                .selectFrom(qItem)
//                .where(qItem.i.gt(100))
                .orderBy(qItem.id.asc()) // ID 기준 오름차순
                .offset(2) // 3번째 데이터부터 시작 (0은 첫 번째)
                .limit(3) // 3개만 조회
                .fetch();

        log.info(items.toString());
    }

    // 그룹화, 직계함수(count, max, avg 등)
    @Test
    public void testAggreegateFunction(){
        QItem qItem = QItem.item;

        Tuple tuple = queryFactory
                .select(
                        qItem.itemSellStatus,
                        qItem.price.avg()
                )
                .from(qItem)
                .groupBy(qItem.itemSellStatus)
                .fetchOne();

        log.info("판매 상태 : {}", tuple.get(qItem.itemSellStatus));
        log.info("평균 가격 : {}", tuple.get(qItem.price.avg()));
    }

    // ItemImg 조회
    @Test
    public void testItemImg(){
        QItemImg qItemImg = QItemImg.itemImg;

        List<ItemImg> result = queryFactory
                .selectFrom(qItemImg)
                .where(qItemImg.repimgYn.eq("Y")) // 대표 이미지만 조회
                .fetch(); // 리스트로 결과 가져오기
        result.forEach(item->log.info(item.toString())); // 로그로 출력
    }

    // ItemImg, Item Join
    /*
        select i.*
        from item_img i
        join item t on i.item_id = t.item_id
        where t.item_nm like "%자바%";
     */
    @Test
    public void testJoin(){
        QItem qItem = QItem.item;
        QItemImg qItemImg = QItemImg.itemImg;

        List<ItemImg> result = queryFactory
                .selectFrom(qItemImg) // ItemImg를 기준으로
                .join(qItemImg.item, qItem) // ItemImg가 참조하는 Item 조인
                .where(qItem.itemNm.contains("자바")) // Item의 이름에 "자바" 포함
                .fetch(); // 결과 조회

        log.info(result.toString());
    }
}