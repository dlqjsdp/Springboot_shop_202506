package com.example.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name="order_item")
@Getter @Setter
@ToString(exclude = "order")
public class OrderItem extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name="order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id")
//    @ToString.Exclude
    private Order order;

    private int orderPrice; // 주문가격
    private int count; // 주문수량


    /**
     * 주문상품 생성 팩토리 메서드
     * - 주문상품을 생성하고 재고를 차감함
     * @param item 주문할 상품
     * @param count 주문 수량
     * @return 생성된 주문상품 엔티티
     */
    public static OrderItem createOrderItem(Item item, int count) {
        OrderItem orderItem = new OrderItem(); // 새로운 OrderItem 인스턴스 생성

        orderItem.setItem(item); // 주문상품에 상품 정보 설정
        orderItem.setCount(count); // 주문 수량 설정
        orderItem.setOrderPrice(item.getPrice()); // 주문 당시의 상품 가격 저장 -> 스냅샷

        item.removeStock(count); // 주문 수량만큼 상품 재고 차감

        return orderItem; // 생성된 주문상품 반환
    }

    public void cancel(){
        this.getItem().addStock(count);
    }


    /**
     * 주문상품의 총 가격 계산
     * @return (주문 가격 × 수량)
     */
    public int getTotalPrice() {

        return this.getOrderPrice()*this.count;
    }
}
