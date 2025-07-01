package com.example.shop.dto;

import com.example.shop.constant.OrderStatus;
import com.example.shop.entity.Order;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@ToString
public class OrderHistDto {

    private Long orderId; // 주문자 아이디
    private String orderDate; // 주문 날짜
    private OrderStatus orderStatus; // 주문 상태

    // 주문 리스트               orderItemDtoList
    private List<OrderItemDto> orderItemDtoList = new ArrayList<>();

    public OrderHistDto(Order order) {
        this.orderId = order.getId();
        this.orderDate = order.getOrderDate().
                format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.orderStatus = order.getOrderStatus();
    }

    public void addOrderItem(OrderItemDto orderItemDto) {

        this.orderItemDtoList.add(orderItemDto);
    }
}
