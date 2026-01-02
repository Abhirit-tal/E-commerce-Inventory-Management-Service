package com.example.inventory.service;

import com.example.inventory.dto.category.CategoryCreateDTO;
import com.example.inventory.dto.category.CategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryDTO create(CategoryCreateDTO dto);
    CategoryDTO getById(Long id);
    Page<CategoryDTO> list(Pageable pageable);
    CategoryDTO update(Long id, CategoryCreateDTO dto);
    void delete(Long id);
}

