package com.rikkei.reshoppingcartwebapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.rikkei.reshoppingcartwebapp.service.CartService;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Add a product to the cart
    @PostMapping("/{cartId}/add/{productId}")
    public String addProductToCart(
            @PathVariable Integer cartId,
            @PathVariable Integer productId) {

        cartService.addProductToCart(cartId, productId);
        return "redirect:/";
    }

    @PostMapping("/item/{cartItemId}/increase")
    public String increaseQuantity(
            @PathVariable Integer cartItemId,
            @RequestParam Integer cartId) {

        cartService.increaseProductQuantity(cartItemId);
        return "redirect:/";
    }

    // Decrease item quantity
    @PostMapping("/item/{cartItemId}/decrease")
    public String decreaseQuantity(
            @PathVariable Integer cartItemId,
            @RequestParam Integer cartId) {

        cartService.decreaseProductQuantity(cartItemId);
        return "redirect:/";
    }

    // Remove one item
    @PostMapping("/item/{cartItemId}/remove")
    public String removeItem(
            @PathVariable Integer cartItemId,
            @RequestParam Integer cartId) {

        cartService.removeProductFromCart(cartItemId);
        return "redirect:/";
    }

    // Clear the cart
    @PostMapping("/{cartId}/clear")
    public String clearCart(@PathVariable Integer cartId) {

        cartService.clearCart(cartId);
        return "redirect:/";
    }
}
