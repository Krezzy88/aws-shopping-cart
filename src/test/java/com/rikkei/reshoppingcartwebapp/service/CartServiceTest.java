package com.rikkei.reshoppingcartwebapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rikkei.reshoppingcartwebapp.model.Cart;
import com.rikkei.reshoppingcartwebapp.model.CartItem;
import com.rikkei.reshoppingcartwebapp.model.Product;
import com.rikkei.reshoppingcartwebapp.repository.CartItemRepository;
import com.rikkei.reshoppingcartwebapp.repository.CartRepository;
import com.rikkei.reshoppingcartwebapp.repository.ProductRepository;
import com.rikkei.reshoppingcartwebapp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @InjectMocks CartService cartService;

    private final Cart cart = new Cart(1, null, null);
    private final Product phoneCase = new Product(10, "Phone case", "case.jpg", new BigDecimal("155000.00"));
    private final Product charger = new Product(11, "Charger", "charger.jpg", new BigDecimal("150000.00"));

    @Test
    @DisplayName("adds a new item with quantity one")
    void addProductToCart_createsNewItem() {
        when(cartRepository.findById(1)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10)).thenReturn(Optional.of(phoneCase));
        when(cartItemRepository.findByCartIdAndProductId(1, 10)).thenReturn(Optional.empty());

        cartService.addProductToCart(1, 10);

        ArgumentCaptor<CartItem> saved = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(saved.capture());
        assertThat(saved.getValue().getQuantity()).isEqualTo(1);
        assertThat(saved.getValue().getProduct()).isSameAs(phoneCase);
        assertThat(saved.getValue().getCart()).isSameAs(cart);
    }

    @Test
    @DisplayName("increments quantity for an existing item")
    void addProductToCart_incrementsExistingItem() {
        CartItem existing = new CartItem(5, cart, phoneCase, 2);
        when(cartRepository.findById(1)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10)).thenReturn(Optional.of(phoneCase));
        when(cartItemRepository.findByCartIdAndProductId(1, 10)).thenReturn(Optional.of(existing));

        cartService.addProductToCart(1, 10);

        verify(cartItemRepository).save(existing);
        assertThat(existing.getQuantity()).isEqualTo(3);
    }

    @Test
    void addProductToCart_doesNotSaveWhenCartDoesNotExist() {
        when(cartRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addProductToCart(99, 10))
                .isInstanceOf(NoSuchElementException.class);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addProductToCart_doesNotSaveWhenProductDoesNotExist() {
        when(cartRepository.findById(1)).thenReturn(Optional.of(cart));
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addProductToCart(1, 99))
                .isInstanceOf(NoSuchElementException.class);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void increaseProductQuantity_addsOne() {
        CartItem item = new CartItem(5, cart, phoneCase, 1);
        when(cartItemRepository.findById(5)).thenReturn(Optional.of(item));

        cartService.increaseProductQuantity(5);

        assertThat(item.getQuantity()).isEqualTo(2);
        verify(cartItemRepository).save(item);
    }

    @Test
    void decreaseProductQuantity_subtractsOneWhenAboveOne() {
        CartItem item = new CartItem(5, cart, phoneCase, 3);
        when(cartItemRepository.findById(5)).thenReturn(Optional.of(item));

        cartService.decreaseProductQuantity(5);

        assertThat(item.getQuantity()).isEqualTo(2);
        verify(cartItemRepository).save(item);
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void decreaseProductQuantity_deletesLineWhenQuantityIsOne() {
        CartItem item = new CartItem(5, cart, phoneCase, 1);
        when(cartItemRepository.findById(5)).thenReturn(Optional.of(item));

        cartService.decreaseProductQuantity(5);

        verify(cartItemRepository).delete(item);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void removeAndClear_delegateToRepository() {
        cartService.removeProductFromCart(5);
        cartService.clearCart(1);

        verify(cartItemRepository).deleteById(5);
        verify(cartItemRepository).deleteByCartId(1);
    }

    @Test
    @DisplayName("calculates totals using exact BigDecimal arithmetic")
    void calculateTotal_sumsLines() {
        when(cartItemRepository.findByCartId(1)).thenReturn(List.of(
                new CartItem(1, cart, phoneCase, 2),
                new CartItem(2, cart, charger, 1)));

        assertThat(cartService.calculateTotal(1)).isEqualByComparingTo("460000.00");
    }

    @Test
    void calculateTotal_isZeroForEmptyCart() {
        when(cartItemRepository.findByCartId(1)).thenReturn(List.of());

        assertThat(cartService.calculateTotal(1)).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
