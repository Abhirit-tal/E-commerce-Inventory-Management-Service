package com.example.inventory.service;

import com.example.inventory.dto.sku.InventoryAdjustDTO;
import com.example.inventory.dto.sku.SkuDTO;
import com.example.inventory.entity.InventoryAdjustment;
import com.example.inventory.entity.Sku;
import com.example.inventory.repository.InventoryAdjustmentRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.SkuRepository;
import com.example.inventory.service.impl.SkuServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SkuServiceIdempotencyTest {

    private SkuRepository skuRepository;
    private ProductRepository productRepository;
    private InventoryAdjustmentRepository adjustmentRepository;
    private SkuServiceImpl skuService;

    @BeforeEach
    public void setUp() {
        skuRepository = mock(SkuRepository.class);
        productRepository = mock(ProductRepository.class);
        adjustmentRepository = mock(InventoryAdjustmentRepository.class);
        skuService = new SkuServiceImpl(skuRepository, productRepository, adjustmentRepository);
    }

    @Test
    public void adjustInventory_idempotent_whenKeyExists() {
        InventoryAdjustment existing = new InventoryAdjustment();
        existing.setId(5L);
        existing.setDelta(3);
        existing.setIdempotencyKey("key-123");

        when(adjustmentRepository.findBySkuIdAndIdempotencyKey(10L, "key-123")).thenReturn(Optional.of(existing));

        Sku sku = new Sku();
        sku.setId(10L);
        sku.setInventoryCount(7);
        when(skuRepository.findById(10L)).thenReturn(Optional.of(sku));

        InventoryAdjustDTO dto = new InventoryAdjustDTO();
        dto.setDelta(3);
        dto.setIdempotencyKey("key-123");

        SkuDTO result = skuService.adjustInventory(10L, dto);
        assertThat(result.getInventoryCount()).isEqualTo(7);

        verify(adjustmentRepository, times(1)).findBySkuIdAndIdempotencyKey(10L, "key-123");
        verify(skuRepository, times(1)).findById(10L);
        verify(skuRepository, never()).updateInventoryIfNonNegative(anyLong(), anyInt());
    }
}
