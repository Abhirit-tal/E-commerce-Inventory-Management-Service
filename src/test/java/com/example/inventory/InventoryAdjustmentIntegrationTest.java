package com.example.inventory;

import com.example.inventory.dto.category.CategoryCreateDTO;
import com.example.inventory.dto.product.ProductCreateDTO;
import com.example.inventory.dto.sku.SkuCreateDTO;
import com.example.inventory.dto.sku.SkuDTO;
import com.example.inventory.dto.inventory.InventoryAdjustmentCreateDTO;
import com.example.inventory.entity.InventoryAdjustment;
import com.example.inventory.repository.InventoryAdjustmentRepository;
import com.example.inventory.service.CategoryService;
import com.example.inventory.service.ProductService;
import com.example.inventory.service.SkuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InventoryAdjustmentIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SkuService skuService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InventoryAdjustmentRepository adjustmentRepository;

    @Test
    public void postAdjustment_isAtomicAndIdempotent() {
        // create category
        CategoryCreateDTO c = new CategoryCreateDTO();
        c.setCode("INT-CAT-" + System.currentTimeMillis());
        c.setName("Integration Test Category");
        var cat = categoryService.create(c);

        // create product
        ProductCreateDTO p = new ProductCreateDTO();
        p.setName("IntegrationProduct");
        p.setCategoryId(cat.getId());
        var prod = productService.create(p);

        // create sku
        SkuCreateDTO s = new SkuCreateDTO();
        s.setCode("INT-SKU-1");
        s.setProductId(prod.getId());
        s.setPrice(new BigDecimal("10.00"));
        s.setInventoryCount(5);
        SkuDTO sku = skuService.create(s);

        // prepare adjustment
        InventoryAdjustmentCreateDTO adj = new InventoryAdjustmentCreateDTO();
        adj.setSkuId(sku.getId());
        adj.setDelta(3);
        adj.setReason("integration test");
        adj.setIdempotencyKey("int-key-" + System.currentTimeMillis());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InventoryAdjustmentCreateDTO> request = new HttpEntity<>(adj, headers);

        // first call
        ResponseEntity<SkuDTO> resp1 = restTemplate.postForEntity("/api/inventory-adjustments", request, SkuDTO.class);
        assertThat(resp1.getStatusCode().is2xxSuccessful()).isTrue();
        SkuDTO updated1 = resp1.getBody();
        assertThat(updated1).isNotNull();
        assertThat(updated1.getInventoryCount()).isEqualTo(8);

        // check audit record
        List<InventoryAdjustment> records = adjustmentRepository.findBySkuId(sku.getId());
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getIdempotencyKey()).isEqualTo(adj.getIdempotencyKey());

        // second call (same idempotency key) - should be idempotent
        ResponseEntity<SkuDTO> resp2 = restTemplate.postForEntity("/api/inventory-adjustments", request, SkuDTO.class);
        assertThat(resp2.getStatusCode().is2xxSuccessful()).isTrue();
        SkuDTO updated2 = resp2.getBody();
        assertThat(updated2).isNotNull();
        // inventory should remain 8, not 11
        assertThat(updated2.getInventoryCount()).isEqualTo(8);

        // still only one audit record
        List<InventoryAdjustment> records2 = adjustmentRepository.findBySkuId(sku.getId());
        assertThat(records2).hasSize(1);
    }
}

