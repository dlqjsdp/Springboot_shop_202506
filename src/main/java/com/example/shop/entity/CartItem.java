package com.example.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter
@ToString
@Table(name="cart_item")
public class CartItem {

    @Id
    @GeneratedValue
    @Column(name="cart_item_id")
    private Long cartItemId;

    @ManyToOne // 하나의 장바구니(Cart)는 여러 개의 장바구니 항목(CartItem)을 가짐
    @JoinColumn(name="cart_id")
    private Cart cart;

    @ManyToOne // 하나의 상품(Item)은 여러 장바구니 항목(CartItem)에서 사용될 수 있음
    @JoinColumn(name="item_id")
    private Item item;

    private int count; // 장바구니에 담긴 해당 상품 개수

}
