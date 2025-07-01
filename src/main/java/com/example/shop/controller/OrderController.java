package com.example.shop.controller;

import com.example.shop.dto.OrderDto;
import com.example.shop.dto.OrderHistDto;
import com.example.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/order")
    public @ResponseBody ResponseEntity<?> order(@RequestBody @Valid OrderDto orderDto,
                                              BindingResult bindingResult, Principal principal) {

        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();

            List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            for(FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }

            log.info("sb >>>>>>>>>>> : {} ", sb.toString());
            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        String email = principal.getName();

        Long orderId = 0L;

        try{
            orderId = orderService.order(orderDto, email);
        }catch (Exception e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(orderId, HttpStatus.OK);
    }

    @GetMapping(value = {"/orders", "/orders/{page}"})
    public String orderHist(@PathVariable("page") Optional<Integer> page,
                            Principal principal,
                            Model model) {

        Pageable pageable =
                PageRequest.of(page.isPresent() ? page.get() : 0, 4);

        Page<OrderHistDto> orderHistDtoList =
                orderService.getOrderList(principal.getName(), pageable);

        model.addAttribute("orders", orderHistDtoList);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("maxPage", 5);

        return "order/orderHist";
    }

    // 주문취소
    @PostMapping(value = "/order/{orderId}/cancel")
    public @ResponseBody ResponseEntity<?> cancelOrder(@PathVariable("orderId") Long orderId,
                                                       Principal principal) {
        if(!orderService.validateOrder(orderId, principal.getName())) {
            return new ResponseEntity<String>("주문 취소 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        orderService.cancelOrder(orderId);

        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }
}
