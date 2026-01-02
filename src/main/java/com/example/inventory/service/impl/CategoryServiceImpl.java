package com.example.inventory.service.impl;

import com.example.inventory.dto.category.CategoryCreateDTO;
import com.example.inventory.dto.category.CategoryDTO;
import com.example.inventory.entity.Category;
import com.example.inventory.exception.ConflictException;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.repository.CategoryRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public CategoryDTO create(CategoryCreateDTO dto) {
        Category category = new Category();
        category.setCode(dto.getCode());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        Category saved = categoryRepository.save(category);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getById(Long id) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category not found"));
        return toDto(c);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> list(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public CategoryDTO update(Long id, CategoryCreateDTO dto) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category not found"));
        c.setName(dto.getName());
        c.setDescription(dto.getDescription());
        Category saved = categoryRepository.save(c);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category not found"));
        long count = productRepository.countByCategoryId(id);
        if (count > 0) {
            throw new ConflictException("Cannot delete category with existing products");
        }
        categoryRepository.delete(c);
    }

    private CategoryDTO toDto(Category c) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(c.getId());
        dto.setCode(c.getCode());
        dto.setName(c.getName());
        dto.setDescription(c.getDescription());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}
