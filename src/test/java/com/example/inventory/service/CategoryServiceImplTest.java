package com.example.inventory.service;

import com.example.inventory.dto.category.CategoryCreateDTO;
import com.example.inventory.dto.category.CategoryDTO;
import com.example.inventory.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class CategoryServiceImplTest {

    @Autowired
    private CategoryService categoryService;

    @Test
    public void testCreateAndGet() {
        CategoryCreateDTO dto = new CategoryCreateDTO();
        dto.setCode("ELEC");
        dto.setName("Electronics");
        dto.setDescription("Electronic items");

        CategoryDTO created = categoryService.create(dto);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getCode()).isEqualTo("ELEC");

        CategoryDTO fetched = categoryService.getById(created.getId());
        assertThat(fetched).isNotNull();
        assertThat(fetched.getName()).isEqualTo("Electronics");
    }
}

