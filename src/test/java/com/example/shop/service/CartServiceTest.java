package com.example.shop.service;

import com.example.shop.constant.ItemSellStatus;
import com.example.shop.dto.CartItemDto;
import com.example.shop.entity.CartItem;
import com.example.shop.entity.Item;
import com.example.shop.entity.Member;
import com.example.shop.repository.CartItemRepository;
import com.example.shop.repository.CartRepository;
import com.example.shop.repository.ItemRepository;
import com.example.shop.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@WithMockUser(username = "user1@user.com", roles = "ADMIN")
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    CartItemRepository cartItemRepository;


    @Test
    public void testAddCart() {
        // given
        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setItemId(1L);
        cartItemDto.setCount(10);
        String email = "user1@user.com";

        // when
        Long result = cartService.addCart(cartItemDto, email);

        //then
        CartItem cartItem = cartItemRepository.findById(result)
                .orElseThrow(()->new EntityNotFoundException());
        log.info("result: {}", result);

        assertEquals(cartItemDto.getItemId(), 1L);

    }


/*
       // 1. 사용자의 장바구니가 없으면 새로 생성한다.
       // 2. 장바구니에 동일한 상품이 이미 담겨 있다면 개수를 증가시킨다.
       // 3. 동일한 상품이 없다면 새로운 CartItem을 생성하여 담는다.
       // 4. 처리 후 추가되거나 업데이트된 CartItem의 id를 반환한다.


    public Member saveMember(){
        Member member = new Member();
        member.setEmail("cart@cart.com");
        return memberRepository.save(member);
    }

    public Item saveItem(){
        Item item = new Item();
        item.setItemNm("테스트라면");
        item.setPrice(5000);
        item.setItemDetail("테스트설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        return itemRepository.save(item);
    }

    @Test
    public void testAddCart() {

        // given
        Member member = saveMember();
        Item item = saveItem();

        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setCount(10);
        cartItemDto.setItemId(item.getId());

        // when
        Long cartItemId = cartService.addCart(cartItemDto, member.getEmail());

        // then
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException());

        assertEquals(item.getId(), cartItem.getItem().getId());
        assertEquals(cartItemDto.getCount(), cartItem.getCount());

    }

 */
  
}