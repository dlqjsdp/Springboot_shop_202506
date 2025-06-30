package com.example.shop.service;

import com.example.shop.dto.OrderDto;
import com.example.shop.dto.OrderHistDto;
import com.example.shop.dto.OrderItemDto;
import com.example.shop.entity.*;
import com.example.shop.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    // 의존성 주입
    private final OrderRepository orderRepository; // 주문 엔티티 저장소
    private final ItemRepository itemRepository; // 상품 엔티티 저장소
    private final MemberRepository memberRepository; // 회원 엔티티 저장소
    private final ItemImgRepository itemImgRepository; // 상품 이미지 저장소

    /**
     * 주문 생성 서비스 로직
     * @param orderDto 주문할 상품 정보 (상품ID, 수량)
     * @param email 주문자 이메일
     * @return 생성된 주문 ID
     */
    // orderDto(맥주, 2병), email(1번 테이블)
    public Long order(OrderDto orderDto, String email) {

        // 1) 주문할 상품 엔티티 조회 (상품이 없으면 예외 발생)
        // 예: itemId = 1 -> '맥주' 상품 엔티티
        Item item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException());

        // 2) 주문자 회원 엔티티 조회 (이메일로 조회)
        // 예: email = "hong@test.com" -> '홍길동' 회원 엔티티
        Member member = memberRepository.findByEmail(email);

        // 3) 주문상품 리스트 생성
        List<OrderItem> orderItemList = new ArrayList<>();

        // 4) 주문상품 생성 (재고 차감 포함)
        // 예: 맥주 2병 주문 -> 재고 10 -> 8로 차감
        OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());

        // 5) 주문상품을 리스트에 추가
        orderItemList.add(orderItem);

        // 6) 주문 엔티티 생성 (주문자 + 주문상품들)
        // - 주문자(member)
        // - 주문상품 리스트(orderItemList)
        // - 상태: ORDER
        // - 주문일자: 현재 시각
        Order order = Order.createOrder(member, orderItemList);

        // 7) 주문 저장 (Order, OrderItem 모두 저장됨)
        // - Order와 OrderItem 모두 DB에 저장 (cascade = ALL)
        orderRepository.save(order);

        // 8) 생성된 주문번호 반환
        return order.getId();
    }

    /**
     * 주문 내역 조회 서비스 로직
     * @param email 로그인한 사용자 이메일
     * @param pageable 페이징 정보
     * @return 주문 내역 페이지
     */
    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderList(String email, Pageable pageable) {

        // 1) 사용자의 주문 목록 조회 (페이징 처리)
        // - JPQL: 이메일로 조회 + 최신순 정렬
        List<Order> orders = orderRepository.findOrders(email, pageable);

        // 로그로 주문 엔티티 내용 확인
        orders.forEach(order -> log.info(order.toString()));

        // 2) 해당 사용자의 전체 주문 건수 조회
        Long totalCount = orderRepository.countOrder(email);

        // 3) 반환할 DTO 리스트 생성
        List<OrderHistDto> orderHistDtoList = new ArrayList<>();

        // 4) 조회된 주문 엔티티 -> DTO로 변환
        for (Order order : orders) {
            // 주문 이력 DTO 생성 (주문 ID, 주문일자, 상태)
            OrderHistDto orderHistDto = new OrderHistDto(order);

            // 해당 주문에 속한 주문상품 리스트 가져오기
            List<OrderItem> orderItems = order.getOrderItems();

            log.info("---------------------------------");
            log.info("orderItems.size() : {}", orderItems.size());
            log.info("---------------------------------");

            // 각 주문상품 처리
            for (OrderItem orderItem : orderItems) {

                // 4-1) 해당 상품의 대표 이미지 조회
                ItemImg itemImg = itemImgRepository
                        .findByItemIdAndRepimgYn(orderItem.getItem().getId(), "Y");

                // 4-2) 주문상품 DTO 생성 (상품명, 수량, 금액, 이미지 URL)
                OrderItemDto orderItemDto = new OrderItemDto(
                        orderItem, itemImg.getImgUrl());

                // 4-3) 주문 이력 DTO에 주문상품 DTO 추가
                orderHistDto.addOrderItem(orderItemDto);
            }
            // 4-4) 최종 주문 이력 DTO를 리스트에 추가
            orderHistDtoList.add(orderHistDto);
        }
        // 5) PageImpl로 감싸 반환 (페이징 메타정보 포함)
        return new PageImpl<>(orderHistDtoList, pageable, totalCount);
    }
}
