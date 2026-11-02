package com.rikkei.reshoppingcartwebapp.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.rikkei.reshoppingcartwebapp.service.CartService;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock CartService cartService;
    @InjectMocks CartController controller;
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void add_redirectsHomeAndDelegates() throws Exception {
        mockMvc.perform(post("/cart/1/add/10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        verify(cartService).addProductToCart(1, 10);
    }

    @Test
    void increase_requiresCartIdAndDelegates() throws Exception {
        mockMvc.perform(post("/cart/item/5/increase").param("cartId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        verify(cartService).increaseProductQuantity(5);
    }

    @Test
    void increase_withoutCartId_isBadRequest() throws Exception {
        mockMvc.perform(post("/cart/item/5/increase"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void decrease_removeAndClear_delegateToService() throws Exception {
        mockMvc.perform(post("/cart/item/5/decrease").param("cartId", "1"))
                .andExpect(redirectedUrl("/"));
        mockMvc.perform(post("/cart/item/5/remove").param("cartId", "1"))
                .andExpect(redirectedUrl("/"));
        mockMvc.perform(post("/cart/1/clear"))
                .andExpect(redirectedUrl("/"));

        verify(cartService).decreaseProductQuantity(5);
        verify(cartService).removeProductFromCart(5);
        verify(cartService).clearCart(1);
    }
}
