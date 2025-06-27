package com.example.shop.dto;

import com.example.shop.constant.ItemSellStatus;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ItemSearchDto {

    private String searchDateType; // 날짜 조회

    private ItemSellStatus searchSellStatus; // 판매 상태 조회

    private String searchBy; // 상품명(itemNm), 상품등록자(createBy) 조회

    private String searchQuery = ""; // 상품명(라면), 상품등록자ID(id : test1)
}
