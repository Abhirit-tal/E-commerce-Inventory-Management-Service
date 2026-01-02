package com.example.inventory.service;

import com.example.inventory.dto.product.ProductCreateDTO;
import com.example.inventory.dto.product.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductDTO create(ProductCreateDTO dto);
    ProductDTO getById(Long id);
    Page<ProductDTO> list(String q, Long categoryId, Pageable pageable);
    ProductDTO update(Long id, ProductCreateDTO dto);
    void delete(Long id);
}

