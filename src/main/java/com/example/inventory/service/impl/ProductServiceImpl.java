package com.example.inventory.service.impl;

import com.example.inventory.dto.product.ProductCreateDTO;
import com.example.inventory.dto.product.ProductDTO;
import com.example.inventory.entity.Category;
import com.example.inventory.entity.Product;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.repository.CategoryRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public ProductDTO create(ProductCreateDTO dto) {
        Category c = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new NotFoundException("Category not found"));
        Product p = new Product();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setEnabled(dto.getEnabled());
        p.setCategory(c);
        Product saved = productRepository.save(p);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getById(Long id) {
        Product p = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));
        return toDto(p);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> list(String q, Long categoryId, Pageable pageable) {
        if (q != null && !q.isEmpty()) {
            return productRepository.findByNameContainingIgnoreCase(q, pageable).map(this::toDto);
        }
        if (categoryId != null) {
            return productRepository.findByCategoryId(categoryId, pageable).map(this::toDto);
        }
        return productRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public ProductDTO update(Long id, ProductCreateDTO dto) {
        Product p = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));
        if (dto.getCategoryId() != null) {
            Category c = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new NotFoundException("Category not found"));
            p.setCategory(c);
        }
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setEnabled(dto.getEnabled());
        Product saved = productRepository.save(p);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product p = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));
        productRepository.delete(p);
    }

    private ProductDTO toDto(Product p) {
        ProductDTO dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setEnabled(p.getEnabled());
        dto.setCategoryId(p.getCategory() != null ? p.getCategory().getId() : null);
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}

