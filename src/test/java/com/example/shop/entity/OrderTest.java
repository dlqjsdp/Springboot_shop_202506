package com.example.shop.entity;

import com.example.shop.constant.ItemSellStatus;
import com.example.shop.dto.MemberFormDto;
import com.example.shop.repository.ItemRepository;
import com.example.shop.repository.MemberRepository;
import com.example.shop.repository.OrderItemRepository;
import com.example.shop.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@Transactional
class OrderTest {

    static int i=1; // 아이템 이름 구분용

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ItemRepository itemRepository;

    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    @DisplayName("지연 로딩 테스트")
    public void lazyLodingTest(){
        // 1. 테스트용 주문 생성 (주문상품 3개 포함)
        Order order = this.createOrder();

        // 2. 첫 번째 주문상품의 ID만 추출
        Long OrderItemId = order.getOrderItems().get(0).getId();

        // 프록시 객체를 확인하기 위해 ID만 추출해 놓음
        log.info("OrderItemId ===> {}", OrderItemId);
        // 3. 영속성 컨텍스트 초기화 (1차 캐시 제거 → DB에서 새로 조회되도록 유도)
        em.flush();
        em.clear();

        // 4. OrderItem 엔티티를 ID로 조회 (DB에서 실제로 SELECT 발생)
        OrderItem orderItem = orderItemRepository.findById(OrderItemId)
                .orElseThrow(() -> new EntityNotFoundException("id값 없음"));

        // 5. order 필드(연관 객체)는 아직 로딩되지 않은 상태 (Lazy)
        log.info("order class ===> {}", orderItem); // OrderItem 전체 정보 출력
//        log.info("order class ===> {}", orderItem.getOrder().getClass()); // order 필드가 프록시인지 확인

        // 6. 만약 여기서 orderItem.getOrder().getOrderDate() 등 실제 필드에 접근하면
        //    그 시점에 SQL 실행됨 (Lazy 초기화)
    }

    // 주문 생성 메서드
    public Order createOrder() {
        Order order = new Order();

        // 주문 상품 3개 생성 및 주문에 추가
        for (int i = 0; i < 3; i++) {
            Item item = createItem(); // 테스트용 아이템 생성
            itemRepository.save(item); // DB 저장

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item); // 어떤 상품인지 설정
            orderItem.setCount(10); // 수량
            orderItem.setOrderPrice(1000); // 가격
            orderItem.setOrder(order); // 연관관계 설정

            order.getOrderItems().add(orderItem); // 주문에 추가
        }

        // 회원 생성 및 저장
        Member member = new Member();
        memberRepository.save(member);

        // 주문과 회원 연관관계 설정
        order.setMember(member);

        // 주문 저장 → Cascade 설정으로 OrderItem도 함께 저장
        orderRepository.save(order);

        return order;
    }

    // 고아 객체 제거 테스트
    @Test
    @DisplayName("고아객체 제거 테스트")
    public void orphanRemovalTest(){
        // 주문 + 주문상품 3개 생성
        Order order = this.createOrder();

        // 주문에서 첫 번째 주문상품 제거
        order.getOrderItems().remove(0);

        // flush 시점에 orphanRemoval = true 이므로
        // 연관관계가 끊긴 주문상품(orderItem)은 자동으로 DB에서 삭제됨
        em.flush(); // SQL 실행 (DELETE 확인 가능)
    }

    // 테스트용 상품 생성 메서드
    public Item createItem(){
        Item item = new Item();

        item.setItemNm("테스트 상품"+i);
        item.setPrice(10000*i);
        item.setItemDetail("상세설명"+i);
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100*i);
        item.setRegTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        i++;
        return item;
    }

    // Cascade 저장 테스트
    @Test
    public void cascadeTest(){
        // 1. 새로운 주문 생성
        Order order = new Order();

        // 2. 3개의 주문 상품(OrderItem)을 생성하고 주문(Order)에 추가
        for(int i=0; i<3; i++){
            // (1) 테스트용 상품 생성 및 저장
            Item item = createItem(); // 별도로 정의된 테스트 상품 생성 메서드
            itemRepository.save(item); // DB에 저장

            // (2) 주문 상품 객체 생성
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item); // 어떤 상품인지 설정
            orderItem.setCount(10); // 수량
            orderItem.setOrderPrice(1000); // 가격

            // (3) 주문과 연관관계 설정 (주문상품 → 주문)
            orderItem.setOrder(order);

            // (4) 주문에 주문상품 추가 (주문 → 주문상품)
            order.getOrderItems().add(orderItem); // 양방향 연관관계 설정
        }

        // 3. 주문 객체 내부의 주문상품 로그 출력
        log.info("--------------------------------");
        order.getOrderItems().forEach(orderItem -> {
            log.info(orderItem.toString());
        });

        // 4. 주문 저장
        // CascadeType.ALL 이므로 orderItems도 자동으로 저장됨
        orderRepository.save(order);

        // 5. 영속성 컨텍스트 초기화 (강제로 flush & clear)
        em.flush(); // SQL 실행
        em.clear(); // 1차 캐시 비움

        // 6. DB에서 새로 조회
        Order savedOrder =  orderRepository.findById(order.getId())
                .orElseThrow(()-> new EntityNotFoundException("ID 없음"));

        // 7. 주문상품이 3개 저장되었는지 검증
        assertEquals(3, savedOrder.getOrderItems().size());
    }

}