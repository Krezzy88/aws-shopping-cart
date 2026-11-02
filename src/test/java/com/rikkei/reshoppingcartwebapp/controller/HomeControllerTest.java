package com.rikkei.reshoppingcartwebapp.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.rikkei.reshoppingcartwebapp.service.CartService;
import com.rikkei.reshoppingcartwebapp.service.ProductService;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock ProductService productService;
    @Mock CartService cartService;
    @InjectMocks HomeController controller;
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void home_populatesModelForDemoCart() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of());
        when(cartService.getCartItems(1)).thenReturn(List.of());
        when(cartService.calculateTotal(1)).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("cartId", 1))
                .andExpect(model().attributeExists("products", "cartItems", "totalAmount"));
    }
}
