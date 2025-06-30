package com.example.shop.entity;

import com.example.shop.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="orders") // ORDER는 SQL 예약어(예: ORDER BY)이기 때문에 충돌을 방지를 위해 s추가
@Getter @Setter
//@ToString
public class Order extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // OrderItem 엔티티에 있는 order 필드가 외래키(FK)를 관리하는 주인
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
            orphanRemoval = true)
//    @ToString.Exclude
    private List<OrderItem> orderItems = new ArrayList<>();

    private LocalDateTime orderDate; // 주문일

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; // 주문상태

    // 주문 상품 정보들을 담아둔다!
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }


    /**
     * 주문 엔티티를 생성하고 초기화하는 팩토리 메서드
     * - 주문자(member) 지정
     * - 주문상품(orderItemList) 연관관계 설정
     * - 주문 상태/일자 설정
     */
    public static Order createOrder(Member member, List<OrderItem> orderItemList){
        Order order = new Order(); // 새로운 Order 인스턴스 생성
        order.setMember(member); // 주문자 설정

        // 주문상품 목록을 주문에 추가하며 연관관계 세팅
        for(OrderItem orderItem : orderItemList){
            order.addOrderItem(orderItem);
        }

        order.setOrderStatus(OrderStatus.ORDER); // 주문 상태를 'ORDER'로 설정
        order.setOrderDate(LocalDateTime.now()); // 주문일자를 현재 시각으로 설정

        return order; // 생성된 주문 엔티티 반환
    }

    /**
     * 주문에 포함된 모든 주문상품의 총 금액을 계산
     * @return 주문 총 금액
     */
    // 총 주문 금액 합계
    public int getTotalPrice(){
        int totalPrice = 0; // 총 금액을 저장할 변수 초기화

        // 주문에 담긴 각 주문상품을 순회
        for(OrderItem orderItem : orderItems){
            totalPrice += orderItem.getTotalPrice(); // 주문상품의 금액을 더함 (orderPrice * count)
        }
        return totalPrice; // 주문 총 금액 반환
    }
}
