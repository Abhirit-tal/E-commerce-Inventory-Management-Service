package com.example.inventory.service;

import com.example.inventory.dto.category.CategoryCreateDTO;
import com.example.inventory.dto.category.CategoryDTO;
import com.example.inventory.dto.product.ProductCreateDTO;
import com.example.inventory.dto.product.ProductDTO;
import com.example.inventory.dto.sku.SkuCreateDTO;
import com.example.inventory.dto.sku.SkuDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class SkuServiceImplTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SkuService skuService;

    @Test
    public void testCreateSkuFlow() {
        CategoryCreateDTO cDto = new CategoryCreateDTO();
        cDto.setCode("APP");
        cDto.setName("Apparel");
        CategoryDTO cat = categoryService.create(cDto);

        ProductCreateDTO pDto = new ProductCreateDTO();
        pDto.setName("T-Shirt");
        pDto.setCategoryId(cat.getId());
        ProductDTO prod = productService.create(pDto);

        SkuCreateDTO sDto = new SkuCreateDTO();
        sDto.setCode("TS-001");
        sDto.setProductId(prod.getId());
        sDto.setPrice(new BigDecimal("9.99"));
        sDto.setInventoryCount(10);

        SkuDTO sku = skuService.create(sDto);
        assertThat(sku.getId()).isNotNull();
        assertThat(sku.getInventoryCount()).isEqualTo(10);

        SkuDTO fetched = skuService.getById(sku.getId());
        assertThat(fetched.getCode()).isEqualTo("TS-001");
    }
}

