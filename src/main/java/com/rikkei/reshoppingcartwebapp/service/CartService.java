package com.rikkei.reshoppingcartwebapp.service;

import org.springframework.stereotype.Service;

import com.rikkei.reshoppingcartwebapp.model.Cart;
import com.rikkei.reshoppingcartwebapp.model.CartItem;
import com.rikkei.reshoppingcartwebapp.model.Product;
import com.rikkei.reshoppingcartwebapp.repository.CartItemRepository;
import com.rikkei.reshoppingcartwebapp.repository.CartRepository;
import com.rikkei.reshoppingcartwebapp.repository.ProductRepository;
import com.rikkei.reshoppingcartwebapp.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    // =========================
    // CART ITEMS
    // =========================

    public List<CartItem> getCartItems(Integer cartId) {
        return cartItemRepository.findByCartId(cartId);
    }

    // Add a product to the cart
    public void addProductToCart(Integer cartId, Integer productId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cartId, productId)
                .orElse(null);

        if (item != null) {
            // Increment the existing cart item
            item.setQuantity(item.getQuantity() + 1);
        } else {
            // Create a new cart item
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(1);
        }

        cartItemRepository.save(item);
    }

    // Increase item quantity
    public void increaseProductQuantity(Integer cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow();
        item.setQuantity(item.getQuantity() + 1);
        cartItemRepository.save(item);
    }

    // Decrease item quantity
    public void decreaseProductQuantity(Integer cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow();

        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            cartItemRepository.save(item);
        } else {
            cartItemRepository.delete(item);
        }
    }

    public void removeProductFromCart(Integer cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    public void clearCart(Integer cartId) {
        cartItemRepository.deleteByCartId(cartId);
    }

    // =========================
    // CALCULATION
    // =========================
    public BigDecimal calculateTotal(Integer cartId) {
        return cartItemRepository.findByCartId(cartId)
                .stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
