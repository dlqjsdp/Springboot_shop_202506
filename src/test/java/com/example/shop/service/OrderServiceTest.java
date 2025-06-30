package com.example.shop.service;

import com.example.shop.constant.ItemSellStatus;
import com.example.shop.dto.OrderDto;
import com.example.shop.dto.OrderHistDto;
import com.example.shop.entity.Item;
import com.example.shop.entity.Member;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.example.shop.repository.ItemRepository;
import com.example.shop.repository.MemberRepository;
import com.example.shop.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@WithMockUser(username = "user1@user.com", roles = "ADMIN")
@Transactional // DB에 저장하지 마세요
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    @Test
    @DisplayName("새우깡 상품주문 테스트")
    public void testOrderService() { // 이 테스트코드는 자꾸 테스트 실패함

        String email = "user1@user.com";

        OrderDto orderDto = new OrderDto();

        orderDto.setCount(2);
        orderDto.setItemId(3L);

        // 주문을 딱 한 번 생성
        Long order = orderService.order(orderDto, email);

        log.info("-------------order-------------- : {}", order);

        Order savedOrder = orderRepository.findById(order)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        log.info("-------------savedOrder-------------- : {}", savedOrder);

        savedOrder.getOrderItems().forEach(orderItem -> {log.info("OrderItem: {}", orderItem);});

    }

    @Test
    @DisplayName("떡볶이 상품주문 테스트")
    public void testOrderService2() { // 이 테스트코드는 테스트 성공함

        String email = "user1@user.com";

        OrderDto orderDto = new OrderDto();

        orderDto.setCount(5);
        orderDto.setItemId(2L);

        Long order = orderService.order(orderDto, email);

        log.info("-------------order--------------");


    }

    @Transactional
    @Test
    public void getOrderListTest(){
        String email = "test@test.com";

        Pageable pageable = PageRequest.of(0, 5);

        Page<OrderHistDto> orderHistDtoList = orderService.getOrderList(email, pageable);

        orderHistDtoList.getContent().forEach(list -> log.info("list: {}", list));
        log.info("totalCount : {}", orderHistDtoList.getTotalElements());
    }

}