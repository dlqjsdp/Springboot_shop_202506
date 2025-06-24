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
    @GeneratedValue
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
}
