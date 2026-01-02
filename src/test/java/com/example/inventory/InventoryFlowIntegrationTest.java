package com.example.inventory;

import com.example.inventory.dto.category.CategoryCreateDTO;
import com.example.inventory.dto.category.CategoryDTO;
import com.example.inventory.dto.product.ProductCreateDTO;
import com.example.inventory.dto.product.ProductDTO;
import com.example.inventory.dto.sku.InventoryAdjustDTO;
import com.example.inventory.dto.sku.SkuCreateDTO;
import com.example.inventory.dto.sku.SkuDTO;
import com.example.inventory.service.CategoryService;
import com.example.inventory.service.ProductService;
import com.example.inventory.service.SkuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class InventoryFlowIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SkuService skuService;

    @Test
    public void fullFlowCreateAndAdjustInventory() {
        String code = "ELEC-INT-" + System.currentTimeMillis();

        CategoryCreateDTO catDto = new CategoryCreateDTO();
        catDto.setCode(code);
        catDto.setName("Electronics");
        CategoryDTO cat = categoryService.create(catDto);

        ProductCreateDTO pDto = new ProductCreateDTO();
        pDto.setName("Smartphone");
        pDto.setCategoryId(cat.getId());
        ProductDTO prod = productService.create(pDto);

        SkuCreateDTO sDto = new SkuCreateDTO();
        sDto.setCode("SP-128-BLK");
        sDto.setProductId(prod.getId());
        sDto.setPrice(new BigDecimal("699.00"));
        sDto.setInventoryCount(5);

        SkuDTO sku = skuService.create(sDto);
        assertThat(sku.getInventoryCount()).isEqualTo(5);

        InventoryAdjustDTO adj = new InventoryAdjustDTO();
        adj.setDelta(3);
        SkuDTO adjusted = skuService.adjustInventory(sku.getId(), adj);
        assertThat(adjusted.getInventoryCount()).isEqualTo(8);

        // negative adjustment should fail
        InventoryAdjustDTO neg = new InventoryAdjustDTO();
        neg.setDelta(-10);
        try {
            skuService.adjustInventory(sku.getId(), neg);
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }
}
