package com.example.shop.controller;

import com.example.shop.dto.CartDetailDto;
import com.example.shop.dto.CartItemDto;
import com.example.shop.dto.CartOrderDto;
import com.example.shop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @PostMapping(value = "/cart")
    public @ResponseBody ResponseEntity<?> order (@RequestBody
           @Valid CartItemDto cartItemDto, BindingResult bindingResult, Principal principal) {

        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for(FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }
            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        String email = principal.getName();
        Long cartItemId;

        try{
            cartItemId = cartService.addCart(cartItemDto, email);
        }catch (Exception e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Long>(cartItemId, HttpStatus.CREATED);
    } // end order

    @GetMapping(value = "/cart")
    public String orderHist(Principal principal, Model model) {
        List<CartDetailDto> cartDetailList = cartService.getCartList(principal.getName());

        model.addAttribute("cartItems", cartDetailList);

        return "cart/cartList";
    }

    // PATCH, var url = "/cartItem/" + cartItemId+"?count=" + count;
    @PatchMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity<?> updateCartItem(
                            @PathVariable("cartItemId") Long cartItemId,
                            @RequestParam("count") int count, Principal principal) {

        // 예외처리 부분부터 처리
        if(count<=0){
            return new ResponseEntity<String>("최소 1개 이상 담아주세요", HttpStatus.BAD_REQUEST);
        }else if(!cartService.validateCartItem(cartItemId, principal.getName())){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.updateCartItem(cartItemId, count);

        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    // var url = "/cartItem/" + cartItemId;
    @DeleteMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity<?> deleteCartItem(@PathVariable("cartItemId") Long cartItemId,
                                                          Principal principal) {
        if(!cartService.validateCartItem(cartItemId, principal.getName())){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.deleteCartItem(cartItemId);

        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    } // end deleteCartItem

    // var url = "/cart/orders";
    // paramData['cartOrderDtoList'] = dataList;
    @PostMapping(value = "cart/orders")
    public @ResponseBody ResponseEntity<?> orders(@RequestBody CartOrderDto cartOrderDto,
                                                  Principal principal) {

        log.info("-------------orders------------");
        log.info("cartOrderDto: {}", cartOrderDto);

        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();

        if(cartOrderDtoList == null || cartOrderDtoList.size() == 0){
            return new ResponseEntity<String>("주문 상품을 선택해 주세요", HttpStatus.BAD_REQUEST);
        }

        for (CartOrderDto cartOrder : cartOrderDtoList) {
            if(!cartService.validateCartItem(cartOrder.getCartItemId(), principal.getName())){
                return new ResponseEntity<String>("주문 권한이 없습니다.", HttpStatus.FORBIDDEN);
            }
        }

        Long orderId = cartService.OrderCartItem(cartOrderDtoList, principal.getName());

        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }
}
