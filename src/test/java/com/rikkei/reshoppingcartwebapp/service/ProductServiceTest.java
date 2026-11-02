package com.rikkei.reshoppingcartwebapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rikkei.reshoppingcartwebapp.model.Product;
import com.rikkei.reshoppingcartwebapp.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @InjectMocks ProductService productService;

    @Test
    void getAllProducts_returnsRepositoryProducts() {
        List<Product> products = List.of(new Product());
        when(productRepository.findAll()).thenReturn(products);

        assertThat(productService.getAllProducts()).isSameAs(products);
        verify(productRepository).findAll();
    }
}
